package org.wnn.portal.pub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wnn.portal.pub.controller.req.ZipperTableDTO;
import org.wnn.portal.pub.dao.ZipperBusinessTableDao;
import org.wnn.portal.pub.dao.ZipperTableDao;
import org.wnn.portal.zipper.dao.entity.ZipperTableMeta;
import org.wnn.portal.zipper.service.ZipperTableMetaService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author NanNan Wang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ZipperTableService {

    private final ZipperTableMetaService zipperTableMetaService;
    private final ZipperTableDao zipperTableDao;
    private final ZipperBusinessTableDao zipperBusinessTableDao;

    private final static String DEFAULT_EFFECTIVE_DATE_COLUMN = "effectiveDate"; // 拉链配置表中的生效时间字段名称
    private final static String DEFAULT_EFFECTIVE_DATE_END_COLUMN = "effectiveEndDate"; // 生效结束时间字段名称

    private final static String DEFAULT_EFFECTIVE_STATUS_COLUMN = "effectiveStatus"; // 最新生效数据表（业务表）中的数据状态字段名，防止有未来生效/已经失效的数据被使用。生效状态：0-未生效（未来），1-生效中，2-已失效
    private final static int DEFAULT_EFFECTIVE_STATUS_FUTURE = 0; // 业务表中的状态字段值：0-未生效（未来
    private final static int DEFAULT_EFFECTIVE_STATUS_CURRENT = 1; // 业务表中的状态字段值：1-生效中
    private final static int DEFAULT_EFFECTIVE_STATUS_EXPIRED = 2; // 业务表中的状态字段值：2-已失效

    private final static Integer DEFAULT_ZIPPER_BREAK_STRATEGY_LEFT = 0; // 拉链配置表的断裂修复策略：0-删除节点后，前序节点失效时间=删除节点的失效时间
    private final static Integer DEFAULT_ZIPPER_BREAK_STRATEGY_RIGHT = 1; // 拉链配置表的断裂修复策略：1-删除节点后，后序节点生效时间=删除节点的生效时间

    private final static LocalDate MAX_END_DATE = LocalDate.of(9999, 12, 31); // 拉链表生效结束时间最大值


    @Transactional
    public void insert(ZipperTableDTO request) {
        // 1. 获取表元信息
        final ZipperTableMeta meta = zipperTableMetaService.getByTableName(request.getZipperTableName());
        if(meta == null) {
            throw new IllegalArgumentException("拉链表[" + request.getZipperTableName() + "]不存在");
        }
        log.info("拉链表公共服务-插入数据-查询拉链表配置信息:{}",meta.toString());

        // 2. 查询历史版本（带锁）
        final List<Map<String,Object>> historyMap = zipperTableDao.queryHistory(meta.getZipperTableSelectSql(),request.getBusinessKeyValues());
        log.info("拉链表公共服务-插入数据-查询历史信息:{}",historyMap.toString());

        // 3. 定位prev和next
        LocalDate newEffectiveDate = request.getEffectiveDate();

        // 先校验是否存在相同的生效时间
        if (hasDuplicateEffectiveDate(historyMap, newEffectiveDate)) {
            throw new IllegalArgumentException("新增失败：已存在生效时间为" + newEffectiveDate + "的版本，禁止重复插入");
        }

        // 查找上一条和下一条记录
        final NodePair nodePair = findPrevAndNextByDate(historyMap, newEffectiveDate);
        Map<String, Object> prev = nodePair.getPrevRecord();
        Map<String, Object> next = nodePair.getNextRecord();

        // 4. 校验时间合法性（示例：若prev存在，newEffectiveDate必须小于prev的终止时间）
        if (prev != null && newEffectiveDate.compareTo(convertToLocalDate(prev.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN))) >= 0) {
            throw new IllegalArgumentException("新生效时间与历史区间不重叠，无需插入");
        }

        // 5. 计算新记录的终止时间
        LocalDate newEffectiveDateEnd = next != null ? convertToLocalDate(next.get(DEFAULT_EFFECTIVE_DATE_COLUMN)) : MAX_END_DATE;

        // 6. 调整上一条记录的终止时间
        if (prev != null) {
            log.info("拉链表公共服务-插入数据-调整上条记录:{}",prev.toString());
            prev.put(DEFAULT_EFFECTIVE_DATE_END_COLUMN, newEffectiveDate);
            zipperTableDao.updateHistory(meta.getZipperTableUpdateSql(),prev);
        }

        // 7. 调整下一条记录的开始时间
        if (next != null) {
            log.info("拉链表公共服务-插入数据-调整下条记录:{}",next.toString());
            next.put(DEFAULT_EFFECTIVE_DATE_COLUMN, newEffectiveDate);
            zipperTableDao.updateHistory(meta.getZipperTableUpdateSql(),next);
        }

        // 8. 插入新记录
        Map<String, Object> newRecord = new HashMap<>(request.getBusinessKeyValues());
        newRecord.putAll(request.getBusinessKeyValues()); // 携带业务键
        newRecord.put(DEFAULT_EFFECTIVE_DATE_COLUMN, newEffectiveDate);
        newRecord.put(DEFAULT_EFFECTIVE_DATE_END_COLUMN, newEffectiveDateEnd);
        zipperTableDao.insertNewRecord(meta.getZipperTableInsertSql(), newRecord);


        // 9. 重新查询全量历史，并同步业务表
        final List<Map<String, Object>> historyAfter = zipperTableDao.queryHistory(
                meta.getZipperTableSelectSql(), request.getBusinessKeyValues());
        LocalDate now = LocalDate.now();
        Map<String, Object> snapshot = pickBusinessSnapshot(historyAfter, now);

        if (snapshot != null) {
            // 拉链表存在当前生效的数据 或者 未来生效的数据
            LocalDate eff = convertToLocalDate(snapshot.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
            boolean isFuture = eff != null && eff.isAfter(now);
            snapshot.put(DEFAULT_EFFECTIVE_STATUS_COLUMN,
                    isFuture ? DEFAULT_EFFECTIVE_STATUS_FUTURE : DEFAULT_EFFECTIVE_STATUS_CURRENT);
            // 生效业务数据表保存数据
            zipperBusinessTableDao.insertBusinessNewRecord(meta.getBusinessTableInsertSql(), snapshot);
            log.info("插入后同步业务表：生效时间={}, 状态={}", eff, isFuture ? "未来生效" : "当前生效");
        } else {
            // 没有任何记录，删除业务表数据 这种情况应该不存在了
            zipperBusinessTableDao.deleteBusinessRecord(meta.getBusinessTableDeleteSql(), request.getBusinessKeyValues());
            log.info("插入后未找到任何记录，已清理业务表数据");
        }
    }


    @Transactional
    public void delete(ZipperTableDTO request) {
        // 1. 获取表元信息，校验拉链表存在性
        final ZipperTableMeta meta = zipperTableMetaService.getByTableName(request.getZipperTableName());
        if (meta == null) {
            throw new IllegalArgumentException("拉链表[" + request.getZipperTableName() + "]不存在");
        }
        log.info("拉链表公共服务-删除数据-查询拉链表配置信息:{}", meta.toString());

        // 2. 查询该业务键的所有历史版本（带锁，防止并发修改）
        List<Map<String, Object>> historyList = zipperTableDao.queryHistory(meta.getZipperTableSelectSql(), request.getBusinessKeyValues());
        if (historyList.isEmpty()) {
            throw new IllegalArgumentException("未查询到该业务键的历史记录，无法删除");
        }

        // 3. 定位要删除的目标版本 根据主键信息
        String primaryKeyValue = String.valueOf(request.getBusinessKeyValues().get(meta.getZipperTablePrimaryKey()));
        Map<String, Object> targetRecord  = findTargetRecordByPrimaryKey(historyList, meta.getZipperTablePrimaryKey(),primaryKeyValue);
        if (targetRecord == null) {
            throw new IllegalArgumentException("未找到主键[" + primaryKeyValue  + "]的版本，无法删除");
        }

        // 4. 查找目标版本的前序节点（prev）和后序节点（next）
        final NodePair nodePair = findTargetRecordPrevAndNextNodes(historyList, targetRecord);
        Map<String, Object> prevRecord = nodePair.getPrevRecord();
        Map<String, Object> nextRecord = nodePair.getNextRecord();

        // 5. 根据断裂修复策略处理时间区间（防止删除后出现时间间隙）
        if (prevRecord != null && nextRecord != null) {
            // 场景1：删除中间节点（前后均有节点），可能产生间隙，需修复
            final Integer breakStrategy = meta.getBreakStrategy();
            log.info("检测到时间间隙，执行修复策略:{}", breakStrategy);

            if (Objects.equals(breakStrategy, DEFAULT_ZIPPER_BREAK_STRATEGY_LEFT)) {
                 // 策略0：前序节点的终止时间 = 删除节点的终止时间（延伸前序节点）
                final LocalDate targetRecordEffectiveEndDate = convertToLocalDate(targetRecord.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN));
                prevRecord.put(DEFAULT_EFFECTIVE_DATE_END_COLUMN, targetRecordEffectiveEndDate);
                zipperTableDao.updateHistory(meta.getZipperTableUpdateSql(), prevRecord);
                log.info("前序节点修复完成-新终止时间:{}", targetRecordEffectiveEndDate);
            } else {
                 // 策略1：后序节点的生效时间 = 删除节点的终止时间（后序节点前移）
                final LocalDate targetRecordEffectiveEndDate = convertToLocalDate(targetRecord.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN));
                nextRecord.put(DEFAULT_EFFECTIVE_DATE_COLUMN, targetRecordEffectiveEndDate);
                zipperTableDao.updateHistory(meta.getZipperTableUpdateSql(), nextRecord);
                log.info("后序节点修复完成-新生效时间:{}", targetRecordEffectiveEndDate);
            }
        } else if (prevRecord != null && nextRecord == null) {
            // 场景2：删除最后一个节点（只有前序节点，无后序节点）
            // 检查前序节点是否需要更新为最新版本（终止时间设为MAX_END_DATE）
            LocalDate prevEndDate = convertToLocalDate(prevRecord.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN));
            if (!prevEndDate.equals(MAX_END_DATE)) {
                prevRecord.put(DEFAULT_EFFECTIVE_DATE_END_COLUMN, MAX_END_DATE);
                zipperTableDao.updateHistory(meta.getZipperTableUpdateSql(), prevRecord);
                log.info("最后节点删除后，前序节点升级为最新版本，新终止时间[{}]", MAX_END_DATE);
            } else {
                log.info("删除的是最后一个节点，前序节点已为最新版本，无需额外处理");
            }
        } else if (prevRecord == null && nextRecord != null) {
            // 场景3：删除第一个节点（只有后序节点，无前序节点）
            log.info("删除的是第一个节点，后序节点自动成为首个节点，生效时间[{}]",
                    convertToLocalDate(nextRecord.get(DEFAULT_EFFECTIVE_DATE_COLUMN)));
        } else {
            // 场景4：删除唯一节点（前后均无节点）
            log.info("删除的是唯一节点，该业务键在拉链表中已无记录");
        }

        // 6. 执行删除操作（删除目标版本）
        zipperTableDao.deleteRecord(meta.getZipperTableDeleteSql(), targetRecord);
        log.info("删除目标版本完成");


        // 7. 查找最新生效的数据，更新到业务表
        final List<Map<String, Object>> historyAfter = zipperTableDao.queryHistory(
                meta.getZipperTableSelectSql(), request.getBusinessKeyValues());
        LocalDate now = LocalDate.now();
        Map<String, Object> snapshot = pickBusinessSnapshot(historyAfter, now);
        if (snapshot != null) {
            // 拉链表存在当前生效的数据 或者 未来生效的数据
            LocalDate eff = convertToLocalDate(snapshot.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
            boolean isFuture = eff != null && eff.isAfter(now);
            snapshot.put(DEFAULT_EFFECTIVE_STATUS_COLUMN,
                    isFuture ? DEFAULT_EFFECTIVE_STATUS_FUTURE : DEFAULT_EFFECTIVE_STATUS_CURRENT);
            // 生效业务数据表保存数据
            zipperBusinessTableDao.updateBusinessNewRecord(meta.getBusinessTableUpdateSql(), snapshot);
            log.info("插入后同步业务表：生效时间={}, 状态={}", eff, isFuture ? "未来生效" : "当前生效");
        } else {
            // 没有任何记录，删除业务表数据 这种情况应该不存在了
            zipperBusinessTableDao.deleteBusinessRecord(meta.getBusinessTableDeleteSql(), request.getBusinessKeyValues());
            log.info("插入后未找到任何记录，已清理业务表数据");
        }
    }

    @Transactional
    public void update(ZipperTableDTO request) {
        // 1. 获取表元信息，校验拉链表存在性
        final ZipperTableMeta meta = zipperTableMetaService.getByTableName(request.getZipperTableName());
        if (meta == null) {
            throw new IllegalArgumentException("拉链表[" + request.getZipperTableName() + "]不存在");
        }
        log.info("拉链表公共服务-更新数据-查询拉链表配置信息:{}", meta.toString());

        // 2. 查询该业务键的所有历史版本（带锁，防止并发修改）
        final List<Map<String, Object>> historyList = zipperTableDao.queryHistory(meta.getZipperTableSelectSql(), request.getBusinessKeyValues());
        if (historyList.isEmpty()) {
            throw new IllegalArgumentException("更新失败：该业务键无历史版本");
        }
        log.info("拉链表公共服务-更新数据-查询历史信息:{}", historyList.toString());

        // 3. 定位目标版本
        String primaryKeyValue = String.valueOf(request.getBusinessKeyValues().get(meta.getZipperTablePrimaryKey()));
        Map<String, Object> targetRecord  = findTargetRecordByPrimaryKey(historyList, meta.getZipperTablePrimaryKey(),primaryKeyValue);
        if (targetRecord == null) {
            throw new IllegalArgumentException("未找到主键" + primaryKeyValue == null ? "null" : primaryKeyValue.toString()  + "的版本，无法删除");
        }

        // 4. 根据情况更新拉链表
        LocalDate newEffectiveDate = request.getEffectiveDate() == null ? convertToLocalDate(request.getBusinessKeyValues().get(DEFAULT_EFFECTIVE_DATE_COLUMN)) : request.getEffectiveDate();
        LocalDate originalEffectiveDate = convertToLocalDate(targetRecord.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
        boolean isEffectiveChanged = !newEffectiveDate.equals(originalEffectiveDate);
        if (!isEffectiveChanged) {
            // 情况 A：生效时间没变，只更新业务字段
            log.info("拉链表公共服务-更新数据-生效时间不变，直接更新业务字段");
            targetRecord.putAll(request.getBusinessKeyValues());
            zipperTableDao.updateHistory(meta.getZipperTableUpdateSql(), targetRecord);

        } else {
            // 情况 B：生效时间改变，相当于先删除，再插入
            log.info("拉链表公共服务-更新数据-生效时间改变，从 {} -> {}", originalEffectiveDate, newEffectiveDate);

            // 先判断新的生效时间是否有冲突
            if (hasDuplicateEffectiveDate(historyList, newEffectiveDate)) {
                throw new IllegalArgumentException("更新失败：已存在生效时间为" + newEffectiveDate + "的版本");
            }

            //  4.1 查找目标版本的前序节点（prev）和后序节点（next）更新时间，删除当前的信息
            final NodePair nodePair = findTargetRecordPrevAndNextNodes(historyList, targetRecord);
            Map<String, Object> prevRecord = nodePair.getPrevRecord();
            Map<String, Object> nextRecord = nodePair.getNextRecord();

            if (prevRecord != null && nextRecord != null) {
                // 场景1：删除中间节点（前后均有节点），可能产生间隙，需修复
                final Integer breakStrategy = meta.getBreakStrategy();
                log.info("检测到时间间隙，执行修复策略:{}", breakStrategy);

                if (Objects.equals(breakStrategy, DEFAULT_ZIPPER_BREAK_STRATEGY_LEFT)) {
                    // 策略0：前序节点的终止时间 = 删除节点的终止时间（延伸前序节点）
                    final LocalDate targetRecordEffectiveEndDate = convertToLocalDate(targetRecord.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN));
                    prevRecord.put(DEFAULT_EFFECTIVE_DATE_END_COLUMN, targetRecordEffectiveEndDate);
                    zipperTableDao.updateHistory(meta.getZipperTableUpdateSql(), prevRecord);
                    log.info("前序节点修复完成-新终止时间:{}", targetRecordEffectiveEndDate);
                } else {
                    // 策略1：后序节点的生效时间 = 删除节点的终止时间（后序节点前移）
                    final LocalDate targetRecordEffectiveEndDate = convertToLocalDate(targetRecord.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN));
                    nextRecord.put(DEFAULT_EFFECTIVE_DATE_COLUMN, targetRecordEffectiveEndDate);
                    zipperTableDao.updateHistory(meta.getZipperTableUpdateSql(), nextRecord);
                    log.info("后序节点修复完成-新生效时间:{}", targetRecordEffectiveEndDate);
                }
            } else if (prevRecord != null && nextRecord == null) {
                // 场景2：删除最后一个节点（只有前序节点，无后序节点）
                // 检查前序节点是否需要更新为最新版本（终止时间设为MAX_END_DATE）
                LocalDate prevEndDate = convertToLocalDate(prevRecord.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN));
                if (!prevEndDate.equals(MAX_END_DATE)) {
                    prevRecord.put(DEFAULT_EFFECTIVE_DATE_END_COLUMN, MAX_END_DATE);
                    zipperTableDao.updateHistory(meta.getZipperTableUpdateSql(), prevRecord);
                    log.info("最后节点删除后，前序节点升级为最新版本，新终止时间[{}]", MAX_END_DATE);
                } else {
                    log.info("删除的是最后一个节点，前序节点已为最新版本，无需额外处理");
                }
            } else if (prevRecord == null && nextRecord != null) {
                // 场景3：删除第一个节点（只有后序节点，无前序节点）
                log.info("删除的是第一个节点，后序节点自动成为首个节点，生效时间[{}]",
                        convertToLocalDate(nextRecord.get(DEFAULT_EFFECTIVE_DATE_COLUMN)));
            } else {
                // 场景4：删除唯一节点（前后均无节点）
                log.info("删除的是唯一节点，该业务键在拉链表中已无记录");
            }

            // 删除当前的版本
            zipperTableDao.deleteRecord(meta.getZipperTableDeleteSql(), targetRecord);
            log.info("删除目标版本完成");


            //  4.2 插入新的版本信息
            final List<Map<String,Object>> afterDeleteHistoryMap = zipperTableDao.queryHistory(meta.getZipperTableSelectSql(),request.getBusinessKeyValues());
            Map<String, Object> prev = null;
            Map<String, Object> next = null;
            for (Map<String, Object> record : afterDeleteHistoryMap) {
                LocalDate effectiveDate = convertToLocalDate(record.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
                LocalDate effectiveEndDate = convertToLocalDate(record.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN));
                if (effectiveDate.compareTo(newEffectiveDate) <= 0 && effectiveEndDate.compareTo(newEffectiveDate) > 0) {
                    prev = record; // 找到上一条
                }
                if (effectiveDate.compareTo(newEffectiveDate) >= 0) {
                    next = record; // 找到下一条
                    break;
                }
            }

            // 计算新记录的终止时间
            LocalDate newEffectiveDateEnd = next != null ? convertToLocalDate(next.get(DEFAULT_EFFECTIVE_DATE_COLUMN)) : MAX_END_DATE;

            // 调整上一条记录的终止时间
            if (prev != null) {
                log.info("拉链表公共服务-插入数据-调整上条记录:{}",prev.toString());
                prev.put(DEFAULT_EFFECTIVE_DATE_END_COLUMN, newEffectiveDate);
                zipperTableDao.updateHistory(meta.getZipperTableUpdateSql(),prev);
            }

            // 调整下一条记录的开始时间
            if (next != null) {
                log.info("拉链表公共服务-插入数据-调整下条记录:{}",next.toString());
                next.put(DEFAULT_EFFECTIVE_DATE_COLUMN, newEffectiveDate);
                zipperTableDao.updateHistory(meta.getZipperTableUpdateSql(),next);
            }

            // 插入新记录
            Map<String, Object> newRecord = new HashMap<>(request.getBusinessKeyValues());
            newRecord.putAll(request.getBusinessKeyValues()); // 携带业务键
            newRecord.put(DEFAULT_EFFECTIVE_DATE_COLUMN, newEffectiveDate);
            newRecord.put(DEFAULT_EFFECTIVE_DATE_END_COLUMN, newEffectiveDateEnd);
            zipperTableDao.insertNewRecord(meta.getZipperTableInsertSql(), newRecord);

            final List<Map<String, Object>> historyAfter = zipperTableDao.queryHistory(
                    meta.getZipperTableSelectSql(), request.getBusinessKeyValues());
            LocalDate now = LocalDate.now();
            Map<String, Object> snapshot = pickBusinessSnapshot(historyAfter, now);

            if (snapshot != null) {
                // 拉链表存在当前生效的数据 或者 未来生效的数据
                LocalDate eff = convertToLocalDate(snapshot.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
                boolean isFuture = eff != null && eff.isAfter(now);
                snapshot.put(DEFAULT_EFFECTIVE_STATUS_COLUMN,
                        isFuture ? DEFAULT_EFFECTIVE_STATUS_FUTURE : DEFAULT_EFFECTIVE_STATUS_CURRENT);
                // 生效业务数据表保存数据
                zipperBusinessTableDao.insertBusinessNewRecord(meta.getBusinessTableInsertSql(), snapshot);
            } else {
                // 没有任何记录，删除业务表数据 这种情况应该不存在了
                zipperBusinessTableDao.deleteBusinessRecord(meta.getBusinessTableDeleteSql(), request.getBusinessKeyValues());
            }
        }

    }

    /**
     * 将数据库返回的日期对象（java.sql.Date）转换为LocalDate
     * 处理null和类型不匹配的情况
     */
    private static LocalDate convertToLocalDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }
        if (dateObj instanceof Date) {
            // 数据库返回的java.sql.Date转换为LocalDate
            return ((Date) dateObj).toLocalDate();
        } else if (dateObj instanceof LocalDate) {
            // 已转换过的LocalDate直接返回
            return (LocalDate) dateObj;
        } else {
            // 其他类型抛出异常（避免隐性错误）
            throw new IllegalArgumentException("不支持的日期类型：" + dateObj.getClass().getName() + "，无法转换为LocalDate");
        }
    }

    /**
     * 私有静态方法：校验历史记录中是否存在相同的生效时间
     * @param historyMap 历史版本列表
     * @param newEffectiveDate 待校验的新生效时间
     * @return 是否存在重复（true=存在重复，false=不存在）
     */
    private static boolean hasDuplicateEffectiveDate(List<Map<String, Object>> historyMap, LocalDate newEffectiveDate) {
        if (newEffectiveDate == null) {
            throw new IllegalArgumentException("待校验的生效时间不能为空");
        }
        if (historyMap == null || historyMap.isEmpty()) {
            return false; // 无历史记录，自然不存在重复
        }

        for (Map<String, Object> record : historyMap) {
            LocalDate existingEffectiveDate = convertToLocalDate(record.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
            // 若历史记录中存在与新生效时间相同的记录，返回true
            if (newEffectiveDate.equals(existingEffectiveDate)) {
                return true;
            }
        }
        return false;
    }



    /**
     * 根据主键名称和值，在历史记录列表中查找匹配的记录
     * @param historyList 历史记录列表
     * @param primaryKeyName 主键名称
     * @param primaryKeyValue 主键值
     * @return 匹配的记录，如果没有匹配的记录，返回null
     */
    private static Map<String, Object> findTargetRecordByPrimaryKey(List<Map<String, Object>> historyList,String primaryKeyName, String primaryKeyValue){
        Map<String, Object> targetRecord = null;
        for (Map<String, Object> record : historyList) {
            // 获取当前记录的主键值
            Object recordPrimaryValue = record.get(primaryKeyName);
            // 跳过主键值为null的记录（无效数据）
            if (recordPrimaryValue == null) {
                continue;
            }
            // 转换记录值为字符串，与目标值比较
            String recordValueStr = String.valueOf(recordPrimaryValue);
            if (recordValueStr.equals(primaryKeyValue)) {
                targetRecord = record;
                break;
            }
        }
        return targetRecord;
    }

    /**
     * 根据生效时间，在历史记录列表中查找前后节点
     * @param historyMap 历史记录列表（有序）
     * @param newEffectiveDate 新生效时间
     * @return
     */
    private static NodePair findPrevAndNextByDate(
            List<Map<String, Object>> historyMap,
            LocalDate newEffectiveDate) {
        // 校验输入参数
        if (newEffectiveDate == null) {
            throw new IllegalArgumentException("生效时间不能为空");
        }
        if (historyMap == null || historyMap.isEmpty()) {
            return new NodePair(null, null); // 无历史记录，前后节点均为null
        }
        Map<String, Object> prev = null;
        Map<String, Object> next = null;
        for (Map<String, Object> record : historyMap) {
            LocalDate effectiveDate = convertToLocalDate(record.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
            LocalDate effectiveEndDate = convertToLocalDate(record.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN));

            // 过滤无效记录（日期为空或区间无效）
            if (effectiveDate == null || effectiveEndDate == null ||
                    effectiveDate.isAfter(effectiveEndDate)) {
                continue;
            }

            // 查找前序节点：生效时间 <= 新生效时间 且 终止时间 > 新生效时间
            if (effectiveDate.compareTo(newEffectiveDate) <= 0 &&
                    effectiveEndDate.compareTo(newEffectiveDate) > 0) {
                prev = record;
            }

            // 查找后序节点：生效时间 >= 新生效时间（找到第一个即跳出）
            if (effectiveDate.compareTo(newEffectiveDate) >= 0) {
                next = record;
                break;
            }
        }

        return new NodePair(prev, next);

    }


    /**
     * 封装：查找目标节点的前序和后序节点
     * @param historyList 所有历史记录
     * @param targetRecord 目标节点
     * @return 包含前序和后序节点的对象
     */
    private static NodePair findTargetRecordPrevAndNextNodes(
            List<Map<String, Object>> historyList,
            Map<String, Object> targetRecord) {

        // 获取目标节点的时间区间
        LocalDate originEffectiveDate = convertToLocalDate(targetRecord.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
        LocalDate originEffectiveEndDate = convertToLocalDate(targetRecord.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN));

        // 校验目标节点日期有效性
        if (originEffectiveDate == null || originEffectiveEndDate == null) {
            throw new IllegalArgumentException("目标节点的生效时间或终止时间不能为空");
        }

        Map<String, Object> prevRecord = null;
        Map<String, Object> nextRecord = null;

        for (Map<String, Object> record : historyList) {
            // 跳过目标节点自身
            if (record == targetRecord) {
                continue;
            }

            // 获取当前记录的时间区间
            LocalDate effectiveDate = convertToLocalDate(record.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
            LocalDate effectiveEndDate = convertToLocalDate(record.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN));

            // 过滤无效记录
            if (effectiveDate == null || effectiveEndDate == null) {
                continue;
            }

            // 匹配前序节点（前序终止时间 = 目标生效时间）
            if (effectiveEndDate.equals(originEffectiveDate)) {
                prevRecord = record;
            }

            // 匹配后序节点（后序生效时间 = 目标终止时间）
            if (effectiveDate.equals(originEffectiveEndDate)) {
                nextRecord = record;
            }

            // 找到所有需要的节点后提前退出
            if (prevRecord != null && nextRecord != null) {
                break;
            }
        }

        return new NodePair(prevRecord, nextRecord);
    }


    /**
     * 辅助类：用于封装前序和后序节点的结果
     */
    private static class NodePair {
        private final Map<String, Object> prevRecord;
        private final Map<String, Object> nextRecord;

        public NodePair(Map<String, Object> prevRecord, Map<String, Object> nextRecord) {
            this.prevRecord = prevRecord;
            this.nextRecord = nextRecord;
        }

        public Map<String, Object> getPrevRecord() {
            return prevRecord;
        }

        public Map<String, Object> getNextRecord() {
            return nextRecord;
        }
    }


    /**
     * 选择一条用于业务表镜像的记录：
     * 1) 优先：当前生效（eff <= now < end）且 eff 最大；
     * 2) 否则：未来版本（eff > now）且 eff 最小；
     * 3) 否则：返回 null。
     */
    private Map<String, Object> pickBusinessSnapshot(List<Map<String, Object>> historyList, LocalDate now) {
        Map<String, Object> bestCurrent = null;
        Map<String, Object> bestFuture = null;
        for (Map<String, Object> rec : historyList) {
            LocalDate eff = convertToLocalDate(rec.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
            LocalDate end = convertToLocalDate(rec.get(DEFAULT_EFFECTIVE_DATE_END_COLUMN));
            if (eff == null || end == null) continue;

            boolean isCurrent = eff.compareTo(now) <= 0 && end.compareTo(now) > 0;
            if (isCurrent) {
                if (bestCurrent == null) {
                    bestCurrent = rec;
                } else {
                    LocalDate curEff = convertToLocalDate(bestCurrent.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
                    if (eff.isAfter(curEff)) bestCurrent = rec;
                }
            } else if (eff.isAfter(now)) {
                if (bestFuture == null) {
                    bestFuture = rec;
                } else {
                    LocalDate curEff = convertToLocalDate(bestFuture.get(DEFAULT_EFFECTIVE_DATE_COLUMN));
                    if (eff.isBefore(curEff)) bestFuture = rec;
                }
            }
        }
        return bestCurrent != null ? bestCurrent : bestFuture;
    }


}

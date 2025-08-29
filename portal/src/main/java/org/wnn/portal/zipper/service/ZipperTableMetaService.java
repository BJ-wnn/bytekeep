package org.wnn.portal.zipper.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wnn.portal.zipper.dao.ZipperTableMetaDao;
import org.wnn.portal.zipper.dao.entity.ZipperTableMeta;

import java.util.List;

/**
 * @author NanNan Wang
 */
@Service
@RequiredArgsConstructor
public class ZipperTableMetaService {
    private final ZipperTableMetaDao zipperTableMetaDao;

//    @Transactional
//    public Long add(ZipperTableMeta meta, String operator) {
//        // 校验表名唯一性
//        ZipperTableMeta existing = zipperTableMetaDao.selectByTableName(meta.getZipperTableName());
//        if (existing != null) {
//            throw new IllegalArgumentException("表名[" + meta.getZipperTableName() + "]已存在，请更换");
//        }
//
//        // 设置创建人
//        meta.setCreatedBy(operator);
//        // 插入数据库
//        zipperTableMetaDao.insert(meta);
//        return meta.getId(); // 若使用自增主键，插入后ID会自动回填
//    }
//
//    @Transactional
//    public void delete(Long id, String operator) {
//        // 校验是否存在
//        ZipperTableMeta meta = zipperTableMetaDao.selectById(id);
//        if (meta == null) {
//            throw new IllegalArgumentException("元信息ID[" + id + "]不存在");
//        }
//
//        // 逻辑删除（更新状态为禁用）
//        zipperTableMetaDao.deleteById(id, operator);
//    }

//    @Transactional
//    public void update(ZipperTableMeta meta, String operator) {
//        // 校验是否存在
//        ZipperTableMeta existing = zipperTableMetaDao.selectById(meta.getId());
//        if (existing == null) {
//            throw new IllegalArgumentException("元信息ID[" + meta.getId() + "]不存在");
//        }
//
//        // 若表名修改，需校验新表名唯一性
//        if (!existing.getZipperTableName().equals(meta.getZipperTableName())) {
//            ZipperTableMeta sameName = zipperTableMetaDao.selectByTableName(meta.getZipperTableName());
//            if (sameName != null) {
//                throw new IllegalArgumentException("新表名[" + meta.getZipperTableName() + "]已存在，请更换");
//            }
//        }
//
//        // 设置更新人
//        meta.setUpdatedBy(operator);
//        // 执行更新
//        zipperTableMetaDao.update(meta);
//    }

//    public ZipperTableMeta getById(Long id) {
//        return zipperTableMetaDao.selectById(id);
//    }

    public ZipperTableMeta getByTableName(String tableName) {
        return zipperTableMetaDao.selectByTableName(tableName);
    }

//    public List<ZipperTableMeta> getAll() {
//        return zipperTableMetaDao.selectAll();
//    }

//    public List<ZipperTableMeta> getEnabled() {
//        return zipperTableMetaDao.selectEnabled();
//    }

}

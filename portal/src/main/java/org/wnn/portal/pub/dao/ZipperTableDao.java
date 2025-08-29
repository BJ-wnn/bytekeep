package org.wnn.portal.pub.dao;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * @author NanNan Wang
 */
@Mapper
public interface ZipperTableDao {
    
    /**
     * 查询拉链表历史记录
     * @param tableName 拉链表名
     * @param fixedConditions 固定条件（来自配置，如"job_status = 1"）
     * @param params 业务键参数（如{job_code: "DEV001"}）
     * @param effectiveDateColumn 生效日期字段名（如"effective_date"）
     * @return 历史记录列表
     */
    @SelectProvider(type = ZipperTableSqlProvider.class, method = "buildQueryHistory")
    List<Map<String,Object>> queryHistory(@Param("zipperSelectSql")String zipperSelectSql, @Param("params") Map<String, Object> params);

    /**
     * 更新拉链表历史记录
     * @param zipperUpdateSql 更新SQL
     * @param params 业务键参数（如{job_code: "DEV001"}）
     */
    @UpdateProvider(type = ZipperTableSqlProvider.class, method = "buildUpdateHistory")
    void updateHistory(@Param("zipperUpdateSql")String zipperUpdateSql, @Param("params") Map<String, Object> params);

    /**
     * 拉链表插入一条记录
     * @param zipperTableInsertSql 拉链表插入sql
     * @param newRecord 业务记录信息
     */
    @InsertProvider(type = ZipperTableSqlProvider.class, method = "buildInsertNewRecord")
    void insertNewRecord(@Param("zipperTableInsertSql")String zipperTableInsertSql, @Param("params")Map<String, Object> newRecord);

    /**
     * 查询拉链表最新一条记录
     * @param zipperTableSelectLatestSql 拉链表查询最新记录sql
     * @param newRecord 业务记录信息
     * @return
     */
    @SelectProvider(type = ZipperTableSqlProvider.class, method = "buildQueryLatestRecord")
    Map<String, Object> queryLatestRecord(@Param("zipperTableSelectLatestSql") String zipperTableSelectLatestSql,@Param("params") Map<String, Object> newRecord);


    @DeleteProvider(type = ZipperTableSqlProvider.class, method = "buildDeleteRecord")
    void deleteRecord(@Param("zipperTableDeleteSql") String zipperTableDeleteSql, @Param("params") Map<String, Object> params);

}

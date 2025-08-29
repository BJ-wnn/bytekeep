package org.wnn.portal.pub.dao;

import org.apache.ibatis.annotations.*;

import java.util.Map;

/**
 * @author NanNan Wang
 */
@Mapper
public interface ZipperBusinessTableDao {


    @InsertProvider(type = ZipperBusinessTableSqlProvider.class, method = "buildInsertBusinessNewRecord")
    void insertBusinessNewRecord(@Param("businessTableInsertSql") String businessTableInsertSql, @Param("params") Map<String, Object> zipperTableLatestRecord);

    @UpdateProvider(type = ZipperBusinessTableSqlProvider.class, method = "buildUpdateBusinessNewRecord")
    void updateBusinessNewRecord(@Param("businessTableUpdateSql") String businessTableInsertSql,@Param("params") Map<String, Object> latestRecord);

    @DeleteProvider(type = ZipperBusinessTableSqlProvider.class, method = "deleteBusinessRecord")
    void deleteBusinessRecord(@Param("businessTableDeleteSql")String businessTableDeleteSql, @Param("params") Map<String, Object> businessKeyValues);


}

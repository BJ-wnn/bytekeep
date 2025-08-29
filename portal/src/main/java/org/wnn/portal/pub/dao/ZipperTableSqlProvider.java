package org.wnn.portal.pub.dao;

import java.util.Map;

/**
 * @author NanNan Wang
 */
public class ZipperTableSqlProvider {

    public String buildQueryHistory(Map<String, Object> paramMap) {
        final String zipperSelectSql = (String) paramMap.get("zipperSelectSql");
        return zipperSelectSql;
    }


    public String buildUpdateHistory(Map<String, Object> paramMap) {
        final String zipperUpdateSql = (String) paramMap.get("zipperUpdateSql");
        return zipperUpdateSql;
    }

    public String buildInsertNewRecord(Map<String, Object> paramMap) {
        final String zipperTableInsertSql = (String) paramMap.get("zipperTableInsertSql");
        return zipperTableInsertSql;
    }

    public String buildQueryNewestRecord(Map<String, Object> paramMap) {
        final String zipperNewestSelectSql = (String) paramMap.get("zipperNewestSelectSql");
        return zipperNewestSelectSql;
    }

    public String buildUpdateBusinessNewestRecord(Map<String, Object> paramMap) {
        final String businessTableUpdateSql = (String) paramMap.get("businessTableUpdateSql");
        return businessTableUpdateSql;
    }
    public String buildInsertBusinessNewRecord(Map<String, Object> paramMap) {
        final String businessTableInsertSql = (String) paramMap.get("businessTableInsertSql");
        return businessTableInsertSql;
    }

    public String buildQueryLatestRecord(Map<String, Object> paramMap) {
        final String zipperTableSelectLatestSql = (String) paramMap.get("zipperTableSelectLatestSql");
        return zipperTableSelectLatestSql;
    }

    public String buildDeleteRecord(Map<String, Object> paramMap) {
        final String zipperTableDeleteSql = (String) paramMap.get("zipperTableDeleteSql");
        return zipperTableDeleteSql;
    }
}

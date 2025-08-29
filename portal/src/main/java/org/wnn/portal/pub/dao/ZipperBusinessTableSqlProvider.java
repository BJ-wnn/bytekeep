package org.wnn.portal.pub.dao;

import java.util.Map;

/**
 * @author NanNan Wang
 */
public class ZipperBusinessTableSqlProvider {

    public String buildInsertBusinessNewRecord(Map<String, Object> paramMap) {
        final String businessTableInsertSql = (String) paramMap.get("businessTableInsertSql");
        return businessTableInsertSql;
    }

    public String buildUpdateBusinessNewRecord(Map<String, Object> paramMap) {
        final String businessTableUpdateSql = (String) paramMap.get("businessTableUpdateSql");
        return businessTableUpdateSql;
    }

    public String deleteBusinessRecord(Map<String, Object> paramMap) {
        final String businessTableDeleteSql = (String) paramMap.get("businessTableDeleteSql");
        return businessTableDeleteSql;
    }
}

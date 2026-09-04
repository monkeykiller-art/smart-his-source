package com.smarthis.common.datasource;

public final class SqlCompatSupport {

    private SqlCompatSupport() {}

    public static String nowExpression(String dbType) {
        return "CURRENT_TIMESTAMP";
    }

    public static String ifNull(String dbType, String column, String defaultValue) {
        return "COALESCE(" + column + ", " + defaultValue + ")";
    }

    public static String concat(String dbType, String... parts) {
        return String.join(" || ", parts);
    }

    public static String limitClause(String dbType, int limit, int offset) {
        return "LIMIT " + limit + " OFFSET " + offset;
    }
}

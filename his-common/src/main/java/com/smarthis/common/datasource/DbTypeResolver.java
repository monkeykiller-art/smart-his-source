package com.smarthis.common.datasource;

import com.baomidou.mybatisplus.annotation.DbType;

public final class DbTypeResolver {

    private DbTypeResolver() {}

    public static DbType resolve(HisDataSourceProperties props) {
        if (props == null || props.getDbType() == null) {
            return DbType.POSTGRE_SQL;
        }
        return switch (props.getDbType().toUpperCase()) {
            case "DM" -> DbType.DM;
            case "KINGBASE_ES", "KINGBASE" -> DbType.KINGBASE_ES;
            case "MYSQL" -> DbType.MYSQL;
            case "OSCAR" -> DbType.POSTGRE_SQL;
            default -> DbType.POSTGRE_SQL;
        };
    }
}

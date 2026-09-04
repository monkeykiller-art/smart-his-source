package com.smarthis.common.datasource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "his.datasource")
public class HisDataSourceProperties {
    private String dbType = "POSTGRE_SQL";
}

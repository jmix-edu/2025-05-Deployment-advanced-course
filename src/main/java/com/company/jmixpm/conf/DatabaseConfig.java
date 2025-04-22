package com.company.jmixpm.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.vault.core.VaultTemplate;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    private final VaultTemplate vaultTemplate;

    public DatabaseConfig(@Lazy VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    @Bean("dataSourcePropertiesDev")
    @Profile("default")
    @Primary
    @ConfigurationProperties("main.datasource")
    DataSourceProperties dataSourcePropertiesDev() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("main.datasource.hikari")
    DataSource dataSource(final DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean("dataSourcePropertiesProd")
    @Profile("prod")
    @Primary
    DataSourceProperties dataSourcePropertiesProd() {
        DataSourceProperties properties = new DataSourceProperties();
        DbConfigWrapper wrapper = new ObjectMapper().convertValue(
                vaultTemplate.read("secret/data/application/database/credentials").getData().get("data"),
                DbConfigWrapper.class);

        properties.setUrl(wrapper.getUrl());
        properties.setUsername(wrapper.getUsername());
        properties.setPassword(wrapper.getPassword());

        return properties;
    }
}

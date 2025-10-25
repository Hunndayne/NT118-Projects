package com.finalproject.backend.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass(HikariDataSource.class)
public class DataSourceConfig {

	@Bean
	@Primary
	public DataSource dataSource(DataSourceProperties properties) {
		HikariDataSource dataSource = properties.initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
		dataSource.addDataSourceProperty("TimeZone", "UTC");
		return dataSource;
	}
}

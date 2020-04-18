package com.example;

import com.informix.jdbcx.IfxDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
class AppConfig {

    @Bean
    DataSource dataSource(@Value("${spring.datasource.url}") String url,
                          @Value("${spring.datasource.username}") String user,
                          @Value("${spring.datasource.password}") String password) throws SQLException {
        IfxDataSource ds = new IfxDataSource(url);
        ds.setUser(user);
        ds.setPassword(password);
        return ds;
    }

    @Bean
    TableWatcher tableWatcher(DataSource dataSource, EventPublisher eventPublisher) {
        return new TableWatcher(dataSource, eventPublisher);
    }

    @Bean
    EventPublisher eventPublisher() {
        return new EventPublisher();
    }

}

package com.art.art;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Hikari数据源加载器
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Component("hikariLoader")
public class HikariLoader {
    private final HikariDataSource hikariDataSource;

    public HikariLoader(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    @PostConstruct
    public void load() throws Exception {
        hikariDataSource.getConnection();
    }
}

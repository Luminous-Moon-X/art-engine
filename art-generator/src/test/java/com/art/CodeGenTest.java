package com.art;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

public class CodeGenTest {
    @Test
    public void testGen() {
        String driverName = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://47.95.206.93:3306/aether?serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false";
        String username = "root";
        String password = "xuaihao1234A!";
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverName);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        try (HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig)) {
            CodeGenerator codeGenerator = new CodeGenerator(hikariDataSource);
//            codeGenerator.generate("art-business", "com.art", "")
        }
        System.out.println("1111");
    }
}

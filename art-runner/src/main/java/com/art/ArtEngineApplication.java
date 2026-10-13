package com.art;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.art.**"})
@MapperScan("com.art.mapper")
public class ArtEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtEngineApplication.class, args);
    }

}

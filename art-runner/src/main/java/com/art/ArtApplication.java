package com.art;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.art.**"})
@MapperScan("com.art.mapper")
public class ArtApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtApplication.class, args);
    }

}

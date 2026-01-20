package com.art;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 服务启动成功后打印
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Component
public class BannerRunner implements ApplicationRunner {
    /**
     * 构建信息
     */
    private final BuildProperties buildProperties;

    /**
     * 构造方法
     *
     * @param buildProperties 构建信息
     */
    public BannerRunner(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String version = buildProperties.getVersion();
        ClassPathResource resource = new ClassPathResource("started-banner.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            String bannerContent = contentBuilder.toString();
            System.out.print(bannerContent);
        }
        System.out.printf("启动成功:)        - version：%s%n", version);
    }

}
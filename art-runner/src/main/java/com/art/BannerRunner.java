package com.art;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
        Path bannerPath = new ClassPathResource("started-banner.txt").getFile().toPath();
        String bannerContent = Files.readString(bannerPath, StandardCharsets.UTF_8);
        System.out.println(bannerContent);
        System.out.printf("Art Engine服务启动成功(*^_^*)        当前版本：%s%n", version);
    }

}
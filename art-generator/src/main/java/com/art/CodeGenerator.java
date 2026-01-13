package com.art;

import com.art.common.BaseEntity;
import com.art.exception.ArtException;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 代码生成器
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
public class CodeGenerator {
    /**
     * 数据源
     */
    private final HikariDataSource dataSource;

    /**
     * 构造方法
     *
     * @param dataSource 数据源
     */
    public CodeGenerator(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 生成代码
     *
     * @param moduleName   模块名称
     * @param packageName  包名
     * @param tables       表名
     * @param authorName   作者名称
     * @param generateType 生成类型
     * @return 生成结果
     */
    public Boolean generate(String moduleName, String packageName, String[] tables, String authorName, String[] generateType) {
        try {
            // 创建配置内容
            GlobalConfig globalConfig = this.createGlobalConfig(moduleName, packageName, tables, authorName, generateType);
            // 通过 datasource 和 globalConfig 创建代码生成器
            Generator generator = new Generator(dataSource, globalConfig);
            // 生成代码
            generator.generate();
        } catch (Exception e) {
            log.error("An error has occurred: ", e);
            throw new ArtException("代码生成失败:{}", e.getMessage());
        }
        return true;
    }

    /**
     * 代码生成器配置内容
     *
     * @return 配置内容
     */
    private GlobalConfig createGlobalConfig(String moduleName, String packageName, String[] tables, String authorName, String[] generateType) {
        // 创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 设置根包
        globalConfig.getPackageConfig()
                .setSourceDir(System.getProperty("user.dir") + "/" + moduleName + "/src/main/java")
                .setBasePackage(packageName);
        // 设置Java Doc
        globalConfig.getJavadocConfig().setAuthor(authorName);
        // 设置生成表
        globalConfig.getStrategyConfig()
                .setGenerateTable(tables);

        // 设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setSuperClass(BaseEntity.class)
                .setJdkVersion(21);

        // 设置生成 mapper
        globalConfig.enableMapper();
        // 设置生成Controller
        globalConfig.enableController();

        //可以单独配置某个列
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setColumnName("tenant_id");
        columnConfig.setLarge(true);
        columnConfig.setVersion(true);
        globalConfig.getStrategyConfig()
                .setColumnConfig("tb_account", columnConfig);

        return globalConfig;
    }
}

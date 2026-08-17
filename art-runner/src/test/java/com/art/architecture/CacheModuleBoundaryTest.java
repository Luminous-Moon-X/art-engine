package com.art.architecture;

import com.art.cache.support.ArtCache;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 二级缓存模块边界测试：
 * 固化缓存实现的模块归属与生命周期调用规则，防止模块边界再次被模糊
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@SuppressWarnings("unused")
@AnalyzeClasses(packages = "com.art")
public class CacheModuleBoundaryTest {

    /**
     * 缓存实现类只能位于缓存包内：
     * 业务缓存 com.art.cache（art-system）、认证授权缓存 com.art.auth.cache（art-core）
     */
    @ArchTest
    static final ArchRule CACHE_IMPL_ONLY_IN_CACHE_PACKAGES = classes()
            .that().areAssignableTo(ArtCache.class)
            .and().doNotHaveFullyQualifiedName(ArtCache.class.getName())
            .should().resideInAnyPackage("com.art.cache", "com.art.auth.cache");

    /**
     * 启动预热仅允许缓存预热器调用，业务代码不得调用 warmUp
     */
    @ArchTest
    static final ArchRule ONLY_RUNNER_CAN_WARM_UP = noClasses()
            .that().resideOutsideOfPackage("com.art.cache.support..")
            .should().callMethod(ArtCache.class, "warmUp");

    /**
     * 缓存机制包（support）只做机制，不允许依赖业务缓存实现（com.art.cache 下除 support 外）
     */
    @ArchTest
    static final ArchRule SUPPORT_MUST_NOT_DEPEND_ON_BUSINESS_CACHES = noClasses()
            .that().resideInAPackage("com.art.cache.support..")
            .should().dependOnClassesThat().resideInAPackage("com.art.cache")
            .andShould().resideOutsideOfPackage("com.art.cache.support..");

    /**
     * 缓存机制包（support）不允许依赖认证授权缓存实现
     */
    @ArchTest
    static final ArchRule SUPPORT_MUST_NOT_DEPEND_ON_AUTH_CACHES = noClasses()
            .that().resideInAPackage("com.art.cache.support..")
            .should().dependOnClassesThat().resideInAPackage("com.art.auth.cache..");

    /**
     * 业务代码不得直接操纵缓存生命周期（reload/evict/clearLocal），
     * 必须通过 CacheRefreshService 统一入口
     */
    @ArchTest
    static final ArchRule BUSINESS_MUST_NOT_MANIPULATE_CACHE_LIFECYCLE = noClasses()
            .that().resideOutsideOfPackage("com.art.cache.support..")
            .should().callMethod(ArtCache.class, "reload")
            .orShould().callMethod(ArtCache.class, "evict")
            .orShould().callMethod(ArtCache.class, "clearLocal");
}

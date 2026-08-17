package com.art.cache.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 缓存刷新服务：业务模块唯一的缓存失效/刷新入口<br/>
 * 业务写操作后调用 {@link #refreshAfterCommit(ArtCache)} / {@link #evictAfterCommit(ArtCache)}，
 * 统一在事务提交后异步执行并广播通知其他实例，业务代码不再直接操纵缓存生命周期
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Slf4j
@Service
public class CacheRefreshService {
    /**
     * 缓存刷新线程池（虚拟线程，每任务一线程）
     */
    private static final ExecutorService REFRESH_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 缓存注册表
     */
    private final ArtCacheRegistry registry;
    /**
     * 失效广播器
     */
    private final CacheInvalidationBroadcaster broadcaster;

    /**
     * 构造函数
     *
     * @param registry    缓存注册表
     * @param broadcaster 失效广播器
     */
    public CacheRefreshService(ArtCacheRegistry registry, CacheInvalidationBroadcaster broadcaster) {
        this.registry = registry;
        this.broadcaster = broadcaster;
    }

    /**
     * 事务提交后异步刷新缓存（重载 DB → L2 → L1），并广播通知其他实例清除本地 L1<br/>
     * 无事务环境（或事务回滚）时不执行
     *
     * @param cache 需要刷新的缓存
     */
    public void refreshAfterCommit(ArtCache<?> cache) {
        if (cache == null) {
            return;
        }
        schedule(cache, false);
    }

    /**
     * 事务提交后异步失效缓存（删除 L2 + 清空本地 L1），并广播通知其他实例清除本地 L1<br/>
     * 无事务环境（或事务回滚）时不执行
     *
     * @param cache 需要失效的缓存
     */
    public void evictAfterCommit(ArtCache<?> cache) {
        if (cache == null) {
            return;
        }
        schedule(cache, true);
    }

    /**
     * 安排刷新任务：事务内注册 afterCommit 回调，无事务则直接异步执行
     *
     * @param cache 缓存实例
     * @param evict 是否为失效操作
     */
    private void schedule(ArtCache<?> cache, boolean evict) {
        Runnable task = () -> execute(cache, evict);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    REFRESH_EXECUTOR.submit(task);
                }
            });
        } else {
            REFRESH_EXECUTOR.submit(task);
        }
    }

    /**
     * 执行刷新/失效并广播
     *
     * @param cache 缓存实例
     * @param evict 是否为失效操作
     */
    private void execute(ArtCache<?> cache, boolean evict) {
        try {
            ArtCache<?> registered = registry.findByKey(cache.redisKey());
            if (registered == null) {
                log.warn("缓存未注册，跳过刷新：{}", cache.redisKey());
                return;
            }
            if (evict) {
                registered.evict();
            } else {
                registered.reload();
            }
            broadcaster.broadcast(registered.redisKey());
        } catch (Exception e) {
            log.error("缓存刷新失败：{}", cache.redisKey(), e);
        }
    }
}

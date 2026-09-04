package com.art.cache;

import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.context.IgnoreSqlLogContextHolder;
import com.art.domain.Dict;
import com.art.domain.DictValue;
import com.art.domain.vo.DictItemVO;
import com.art.mapper.DictMapper;
import com.art.mapper.DictValueMapper;
import com.art.tenant.TenantSupport;
import com.art.utils.ConvertUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据字典二级缓存实现（业务缓存，归属 art-system）
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Component
public class DictCache extends ArtCache<Map<String, List<DictItemVO>>> {

    /**
     * 数据字典值Mapper
     */
    private final DictValueMapper dictValueMapper;

    /**
     * 数据字典Mapper
     */
    private final DictMapper dictMapper;

    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 构造函数
     *
     * @param redisTemplate   Redis客户端
     * @param properties      缓存配置
     * @param dictValueMapper 数据字典值Mapper
     * @param dictMapper      数据字典Mapper
     * @param tenantSupport   多租户支持
     */
    public DictCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties, DictValueMapper dictValueMapper, DictMapper dictMapper,
                     TenantSupport tenantSupport) {
        super(redisTemplate, properties);
        this.dictValueMapper = dictValueMapper;
        this.dictMapper = dictMapper;
        this.tenantSupport = tenantSupport;
    }

    @Override
    public String cacheName() {
        return "数据字典";
    }

    @Override
    public String redisKey() {
        return "dict";
    }

    @SneakyThrows
    @Override
    protected Map<String, List<DictItemVO>> loadFromDb() {
        // 字典为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            Map<String, List<DictItemVO>> dictMap = new ConcurrentHashMap<>();
            List<Dict> allDictList = dictMapper.selectListByQuery(QueryWrapper.create().eq(Dict::getEnableFlag, 1));
            List<Thread> threads = new ArrayList<>();
            for (Dict dict : allDictList) {
                Thread thread = Thread.ofVirtual().start(() -> {
                    Long dictId = dict.getId();
                    // IgnoreSqlLogContextHolder 为普通 ThreadLocal，虚拟线程内需自行开启
                    IgnoreSqlLogContextHolder.enable();
                    // TenantManager 为普通 ThreadLocal，虚拟线程内需自行忽略租户条件
                    TenantManager.ignoreTenantCondition();
                    try {
                        List<DictValue> dictValues = dictValueMapper.selectListByQuery(QueryWrapper.create().eq(DictValue::getDictId, dictId));
                        List<DictItemVO> dictItemList = ConvertUtil.convertList(dictValues, DictItemVO.class);
                        dictMap.put(dict.getDictCode(), dictItemList);
                    } finally {
                        IgnoreSqlLogContextHolder.disable();
                        TenantManager.restoreTenantCondition();
                    }
                });
                threads.add(thread);
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("字典缓存加载被中断", e);
                }
            }
            return dictMap;
        });
    }

    /**
     * 根据字典编码获取字典项
     *
     * @param dictCode 字典编码
     * @return 字典项
     */
    public List<DictItemVO> getDictByCode(String dictCode) {
        return this.get().get(dictCode);
    }
}

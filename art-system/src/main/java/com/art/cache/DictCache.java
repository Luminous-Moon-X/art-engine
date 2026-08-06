package com.art.cache;

import com.art.ArtCache;
import com.art.context.IgnoreSqlLogContextHolder;
import com.art.domain.Dict;
import com.art.domain.DictValue;
import com.art.domain.vo.DictItemVO;
import com.art.mapper.DictMapper;
import com.art.mapper.DictValueMapper;
import com.art.utils.ConvertUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DictCache extends ArtCache<String, Map<String, List<DictItemVO>>> {

    /**
     * 数据字典值Mapper
     */
    private final DictValueMapper dictValueMapper;

    /**
     * 数据字典Mapper
     */
    private final DictMapper dictMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate   Redis客户端
     * @param dictValueMapper 数据字典值Mapper
     * @param dictMapper      数据字典Mapper
     */
    public DictCache(RedisTemplate<String, Object> redisTemplate, DictValueMapper dictValueMapper, DictMapper dictMapper) {
        super(redisTemplate);
        this.dictValueMapper = dictValueMapper;
        this.dictMapper = dictMapper;
    }

    @Override
    protected String getCacheName() {
        return "数据字典";
    }

    @Override
    protected String getRedisKey() {
        return "dict";
    }

    @SneakyThrows
    @Override
    protected Map<String, List<DictItemVO>> getCacheData() {
        Map<String, List<DictItemVO>> dictMap = new ConcurrentHashMap<>();
        List<Dict> allDictList = dictMapper.selectListByQuery(QueryWrapper.create().eq(Dict::getEnableFlag, 1));
        List<Thread> threads = new ArrayList<>();
        for (Dict dict : allDictList) {
            Thread thread = Thread.ofVirtual().start(() -> {
                Long dictId = dict.getId();
                IgnoreSqlLogContextHolder.enable();
                List<DictValue> dictValues = dictValueMapper.selectListByQuery(QueryWrapper.create().eq(DictValue::getDictId, dictId));
                IgnoreSqlLogContextHolder.disable();
                List<DictItemVO> dictItemList = ConvertUtil.convertList(dictValues, DictItemVO.class);
                dictMap.put(dict.getDictCode(), dictItemList);
            });
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.join();
        }
        return dictMap;
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

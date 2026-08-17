package com.art.cache;

import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.domain.Rule;
import com.art.domain.vo.RuleItemVO;
import com.art.mapper.RuleMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 规则数据二级缓存实现（业务缓存，归属 art-system）
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Component
public class RuleCache extends ArtCache<List<RuleItemVO>> {
    /**
     * 规则Mapper
     */
    private final RuleMapper ruleMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis客户端
     * @param properties    缓存配置
     * @param ruleMapper    规则Mapper
     */
    public RuleCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties, RuleMapper ruleMapper) {
        super(redisTemplate, properties);
        this.ruleMapper = ruleMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    public String cacheName() {
        return "规则数据";
    }

    /**
     * 获取缓存Key
     *
     * @return 缓存Key
     */
    @Override
    public String redisKey() {
        return "rule";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<RuleItemVO> loadFromDb() {
        return this.ruleMapper
                .selectListByQueryAs(QueryWrapper.create().eq(Rule::getEnableFlag, 1), RuleItemVO.class);
    }

    /**
     * 根据规则编码获取规则信息
     *
     * @param ruleCode 规则编码
     * @return 规则信息
     */
    public RuleItemVO getByRuleCode(String ruleCode) {
        List<RuleItemVO> ruleList = get();
        return ruleList.stream()
                .filter(rule -> rule.getRuleCode().equals(ruleCode))
                .findFirst().orElse(null);
    }
}

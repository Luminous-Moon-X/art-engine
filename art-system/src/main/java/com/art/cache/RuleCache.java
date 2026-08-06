package com.art.cache;

import com.art.ArtCache;
import com.art.domain.Rule;
import com.art.domain.vo.RuleItemVO;
import com.art.mapper.RuleMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 规则数据二级缓存实现
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Component
public class RuleCache extends ArtCache<String, List<RuleItemVO>> {
    /**
     * 规则服务
     */
    private final RuleMapper ruleMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis客户端
     * @param ruleMapper    规则服务
     */
    public RuleCache(RedisTemplate<String, Object> redisTemplate, RuleMapper ruleMapper) {
        super(redisTemplate);
        this.ruleMapper = ruleMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    protected String getCacheName() {
        return "规则数据";
    }

    /**
     * 获取缓存Key
     *
     * @return 缓存Key
     */
    @Override
    protected String getRedisKey() {
        return "rule";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<RuleItemVO> getCacheData() {
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

package com.art.memory;

import com.agentsflex.core.memory.ChatMemory;
import com.agentsflex.core.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Redis AI对话记忆实现类
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Slf4j
public final class RedisChatMemory implements ChatMemory {

    /**
     * Redis客户端
     */
    private final RedisTemplate<String, Message> redisTemplate;

    /**
     * 对话 ID
     */
    private final String conversationId;

    /**
     * 构造函数
     *
     * @param conversationId 对话 ID
     * @param redisTemplate  Redis客户端
     */
    public RedisChatMemory(String conversationId, RedisTemplate<String, Message> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.conversationId = conversationId;
    }

    /**
     * 获取对话消息
     *
     * @param count 最新的X条对话
     * @return 对话消息列表
     */
    @Override
    public List<Message> getMessages(int count) {
        Long size = redisTemplate.opsForList().size(conversationId);
        if (size == null || size == 0) {
            return Collections.emptyList();
        }
        if (count >= size) {
            return redisTemplate.opsForList().range(conversationId, 0, size - 1);
        }
        return redisTemplate.opsForList().range(conversationId, size - count, size - 1);
    }

    /**
     * 新增对话消息
     *
     * @param message 对话消息
     */
    @Override
    public void addMessage(Message message) {
        redisTemplate.opsForList().rightPush(conversationId, message);
    }

    /**
     * 清除对话记忆
     */
    @Override
    public void clear() {
        redisTemplate.delete(conversationId);
    }

    /**
     * 对话ID
     *
     * @return 对话ID
     */
    @Override
    public Object id() {
        return conversationId;
    }
}

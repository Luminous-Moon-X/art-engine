package com.art.log.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口日志查询VO
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class ApiLogVO {
    /**
     * 请求用户名
     */
    @Query(type = Query.Type.LIKE)
    private String userName;
    /**
     * 请求用户昵称
     */
    @Query(type = Query.Type.LIKE)
    private String nickName;
    /**
     * 请求URL
     */
    @Query(type = Query.Type.LIKE)
    private String requestUrl;
    /**
     * 请求方法
     */
    private String requestMethod;
    /**
     * 响应码
     */
    private Integer responseCode;
    /**
     * 请求时间-开始
     */
    private LocalDateTime requestTimeStart;
    /**
     * 请求时间-结束
     */
    private LocalDateTime requestTimeEnd;
}

package com.art.log.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 接口日志实体类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_sys_api_log")
public class ApiLog extends BaseEntity {
    /**
     * 请求用户ID
     */
    @Column("user_id")
    private Long userId;
    /**
     * 请求用户名
     */
    @Column("user_name")
    private String userName;
    /**
     * 请求用户昵称
     */
    @Column("nick_name")
    private String nickName;
    /**
     * 请求URL
     */
    @Column("request_url")
    private String requestUrl;
    /**
     * 请求方法
     */
    @Column("request_method")
    private String requestMethod;
    /**
     * 请求时间
     */
    @Column("request_time")
    private LocalDateTime requestTime;
    /**
     * 响应码
     */
    @Column("response_code")
    private Integer responseCode;
    /**
     * 请求参数
     */
    @Column("request_params")
    private String requestParams;
    /**
     * 返回值
     */
    @Column("response_result")
    private String responseResult;
    /**
     * 耗时(ms)
     */
    @Column("cost_time")
    private Long costTime;
    /**
     * 请求IP
     */
    @Column("ip")
    private String ip;
    /**
     * 操作描述
     */
    @Column("description")
    private String description;
}

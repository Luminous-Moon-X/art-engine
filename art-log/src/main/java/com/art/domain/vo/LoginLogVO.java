package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志查询VO
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class LoginLogVO {
    /**
     * 用户名
     */
    @Query(type = Query.Type.LIKE)
    private String userName;
    /**
     * 用户昵称
     */
    @Query(type = Query.Type.LIKE)
    private String nickName;
    /**
     * 登录IP
     */
    @Query(type = Query.Type.LIKE)
    private String loginIp;
    /**
     * 登录状态(1成功 0失败)
     */
    private Integer status;
    /**
     * 登录时间-开始
     */
    private LocalDateTime loginTimeStart;
    /**
     * 登录时间-结束
     */
    private LocalDateTime loginTimeEnd;
}

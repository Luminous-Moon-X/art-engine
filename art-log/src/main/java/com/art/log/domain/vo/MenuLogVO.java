package com.art.log.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单日志查询VO
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class MenuLogVO {
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
     * 菜单名称
     */
    @Query(type = Query.Type.LIKE)
    private String menuName;
    /**
     * 菜单路径
     */
    @Query(type = Query.Type.LIKE)
    private String menuPath;
    /**
     * 点击时间-开始
     */
    private LocalDateTime clickTimeStart;
    /**
     * 点击时间-结束
     */
    private LocalDateTime clickTimeEnd;
}

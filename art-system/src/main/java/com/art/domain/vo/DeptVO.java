package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.util.List;

/**
 * 部门信息VO类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class DeptVO {
    /**
     * 部门ID
     */
    private Long id;
    /**
     * 部门名称
     */
    @Query(type = Query.Type.LIKE)
    private String deptName;
    /**
     * 排序
     */
    private Integer orderNum;
    /**
     * 负责人
     */
    @Query(type = Query.Type.LIKE)
    private String chargePerson;
    /**
     * 负责人电话
     */
    private String chargePersonTel;
    /**
     * 负责人邮箱
     */
    private String chargePersonEmail;
    /**
     * 启用状态
     */
    private Boolean enableFlag;
    /**
     * 上级部门ID
     */
    private Long parentId;
    /**
     * 子部门列表
     */
    private List<DeptVO> children;
}

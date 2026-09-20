package com.art.service;

import com.art.domain.vo.PermissionTableColumnVO;
import com.art.domain.vo.PermissionTableVO;

import java.util.List;

/**
 * 数据库表元数据服务<br/>
 * 提供数据权限配置所需的数据库表/字段元数据，供「授权客体」「权限字段」下拉使用
 *
 * @author Luminous.X
 * @since 2.1.0
 */
public interface TableMetadataService {

    /**
     * 查询当前数据库中的所有表（含表备注）
     *
     * @return 表信息列表
     */
    List<PermissionTableVO> selectTableList();

    /**
     * 查询指定表的字段列表（含字段备注）
     *
     * @param tableName 表名
     * @return 字段信息列表，表不存在时返回空列表
     */
    List<PermissionTableColumnVO> selectTableColumnList(String tableName);
}

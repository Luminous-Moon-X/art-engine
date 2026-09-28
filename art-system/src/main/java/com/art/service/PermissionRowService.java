package com.art.service;

import com.art.domain.PermissionRow;
import com.art.domain.vo.PermissionRowVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 数据行权限服务接口类
 *
 * <p>数据权限为租户级数据：多租户开启后按当前生效租户自动过滤，
 * 各租户拥有各自独立的数据权限规则，互不可见、互不影响。</p>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
public interface PermissionRowService extends IService<PermissionRow> {

    /**
     * 分页查询数据权限信息（自动限定当前租户）
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 数据权限信息
     */
    Page<PermissionRowVO> queryPage(Page<PermissionRowVO> page, PermissionRowVO vo);

    /**
     * 新增数据权限（自动归属当前租户）
     *
     * @param vo 数据权限信息
     * @return 新增结果
     */
    Boolean add(PermissionRowVO vo);

    /**
     * 编辑数据权限（自动限定当前租户）
     *
     * @param vo 数据权限信息
     * @return 编辑结果
     */
    Boolean edit(PermissionRowVO vo);

    /**
     * 删除数据权限（自动限定当前租户）
     *
     * @param idList 数据权限ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);

    /**
     * 启用/禁用数据权限（自动限定当前租户）
     *
     * @param id         数据权限ID
     * @param enableFlag 是否启用
     * @return 操作结果
     */
    Boolean updateStatus(Long id, Boolean enableFlag);
}

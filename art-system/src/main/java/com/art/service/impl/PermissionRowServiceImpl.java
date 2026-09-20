package com.art.service.impl;

import cn.hutool.core.util.StrUtil;
import com.art.domain.PermissionRow;
import com.art.domain.vo.PermissionRowVO;
import com.art.exception.ArtException;
import com.art.mapper.PermissionRowMapper;
import com.art.service.PermissionRowService;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 数据行权限服务实现类
 *
 * <p>数据权限为租户级数据，全部操作不进入系统级作用域，
 * 由 MyBatis-Flex 全局租户插件自动追加 tenant_id 过滤条件。</p>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@Service
public class PermissionRowServiceImpl extends ServiceImpl<PermissionRowMapper, PermissionRow>
        implements PermissionRowService {

    /**
     * 授权范围：自定义部门范围
     */
    private static final String PERMISSION_SCOPE_CUSTOM_DEPT = "4";

    /**
     * 授权范围：自定义字段
     */
    private static final String PERMISSION_SCOPE_CUSTOM_COLUMN = "5";

    /**
     * 字段关系：为空（无需填写字段值）
     */
    private static final String COLUMN_RELATION_NULL = "null";

    /**
     * 字段关系：不为空（无需填写字段值）
     */
    private static final String COLUMN_RELATION_NOT_NULL = "not_null";

    /**
     * 分页查询数据权限信息（自动限定当前租户）
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 数据权限信息
     */
    @Override
    public Page<PermissionRowVO> queryPage(Page<PermissionRowVO> page, PermissionRowVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        wrapper.orderBy(PermissionRow::getId, false);
        return this.getMapper().paginateAs(page, wrapper, PermissionRowVO.class);
    }

    /**
     * 新增数据权限（自动归属当前租户）
     *
     * @param vo 数据权限信息
     * @return 新增结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(PermissionRowVO vo) {
        this.validate(vo);
        PermissionRow entity = ConvertUtil.convert(vo, PermissionRow.class);
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(Boolean.TRUE);
        }
        return this.save(entity);
    }

    /**
     * 编辑数据权限（自动限定当前租户）
     *
     * @param vo 数据权限信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(PermissionRowVO vo) {
        if (vo == null || vo.getId() == null) {
            throw new ArtException("数据为空，请检查！");
        }
        PermissionRow existing = this.getById(vo.getId());
        if (existing == null) {
            throw new ArtException("数据权限规则不存在");
        }
        this.validate(vo);
        PermissionRow entity = ConvertUtil.convert(vo, PermissionRow.class);
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(existing.getEnableFlag());
        }
        return this.updateById(entity);
    }

    /**
     * 删除数据权限（自动限定当前租户）
     *
     * @param idList 数据权限ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return false;
        }
        List<Long> distinctIds = idList.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return false;
        }
        // 先校验归属当前生效租户，避免按ID误删他租户数据
        long count = this.count(QueryWrapper.create().in(PermissionRow::getId, distinctIds));
        if (count != distinctIds.size()) {
            throw new ArtException("包含非本租户的数据权限规则，操作失败！");
        }
        return this.removeByIds(distinctIds);
    }

    /**
     * 启用/禁用数据权限（自动限定当前租户）
     *
     * @param id         数据权限ID
     * @param enableFlag 是否启用
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, Boolean enableFlag) {
        if (id == null || enableFlag == null) {
            throw new ArtException("参数不完整，请检查！");
        }
        PermissionRow entity = this.getById(id);
        if (entity == null) {
            throw new ArtException("数据权限规则不存在");
        }
        // 状态未变化时直接返回成功，避免无意义的更新
        if (enableFlag.equals(entity.getEnableFlag())) {
            return true;
        }
        entity.setEnableFlag(enableFlag);
        return this.updateById(entity);
    }

    /**
     * 校验数据权限必填项
     *
     * @param vo 数据权限信息
     */
    private void validate(PermissionRowVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        if (StrUtil.isBlank(vo.getSubjectType())) {
            throw new ArtException("授权主体类型不能为空");
        }
        if (StrUtil.isBlank(vo.getPermissionSubject())) {
            throw new ArtException("授权主体不能为空");
        }
        if (StrUtil.isBlank(vo.getPermissionScope())) {
            throw new ArtException("授权范围不能为空");
        }
        // 授权范围为自定义部门范围时，必须指定部门范围
        if (PERMISSION_SCOPE_CUSTOM_DEPT.equals(vo.getPermissionScope())
                && StrUtil.isBlank(vo.getCustomDeptScope())) {
            throw new ArtException("自定义部门权限不能为空");
        }
        // 授权范围为自定义字段时，必须指定字段、字段关系，并按关系决定字段值是否必填
        if (PERMISSION_SCOPE_CUSTOM_COLUMN.equals(vo.getPermissionScope())) {
            if (StrUtil.isBlank(vo.getColumnCondition())) {
                throw new ArtException("权限字段不能为空");
            }
            if (StrUtil.isBlank(vo.getColumnRelation())) {
                throw new ArtException("字段关系不能为空");
            }
            if (this.needColumnValue(vo.getColumnRelation()) && StrUtil.isBlank(vo.getColumnValue())) {
                throw new ArtException("字段值不能为空");
            }
        }
    }

    /**
     * 判断字段关系是否需要填写字段值<br/>
     * 「为空」「不为空」两种关系不需要字段值
     *
     * @param columnRelation 字段关系
     * @return 是否需要字段值
     */
    private boolean needColumnValue(String columnRelation) {
        return StrUtil.isNotBlank(columnRelation)
                && !COLUMN_RELATION_NULL.equals(columnRelation)
                && !COLUMN_RELATION_NOT_NULL.equals(columnRelation);
    }
}

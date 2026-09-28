package com.art.service.impl;

import com.art.auth.cache.PermissionRowCache;
import com.art.cache.support.CacheRefreshService;
import com.art.domain.PermissionRow;
import com.art.domain.vo.PermissionRowVO;
import com.art.exception.ArtException;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * 数据行权限规则管理测试
 *
 * <p>覆盖规则的必填校验、租户归属保护，以及「规则变更后必须刷新数据行权限缓存」这一约定：
 * 方言在构建每条 SQL 时都会读取 {@link PermissionRowCache}，规则变更若不刷新缓存，
 * 新增/编辑/启停/删除都要等到缓存过期才生效，且其他实例不会收到失效通知。</p>
 *
 * <p>被测服务继承 MyBatis-Flex 的 {@code ServiceImpl}，Mapper 访问以桩替代（数据库不参与）。</p>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
class PermissionRowServiceImplTest {

    /**
     * 数据行权限缓存
     */
    private PermissionRowCache permissionRowCache;
    /**
     * 缓存刷新服务
     */
    private CacheRefreshService cacheRefreshService;
    /**
     * 被测试的服务
     */
    private PermissionRowServiceImpl service;

    /**
     * 初始化被测试服务
     */
    @BeforeEach
    void setUp() {
        this.permissionRowCache = mock(PermissionRowCache.class);
        this.cacheRefreshService = mock(CacheRefreshService.class);
        this.service = spy(new PermissionRowServiceImpl(this.permissionRowCache, this.cacheRefreshService));
    }

    /**
     * 新增规则：落库成功、默认启用并刷新缓存
     */
    @Test
    @DisplayName("新增规则：默认启用并刷新缓存")
    void shouldSaveAndRefreshCacheOnAdd() {
        doReturn(true).when(this.service).save(any(PermissionRow.class));

        assertThat(this.service.add(this.vo("role", "1", "2"))).isTrue();

        ArgumentCaptor<PermissionRow> captor = ArgumentCaptor.forClass(PermissionRow.class);
        verify(this.service).save(captor.capture());
        assertThat(captor.getValue().getSubjectType()).isEqualTo("role");
        assertThat(captor.getValue().getPermissionSubject()).isEqualTo("1");
        assertThat(captor.getValue().getPermissionScope()).isEqualTo("2");
        // 未指定启用标识时默认启用
        assertThat(captor.getValue().getEnableFlag()).isTrue();
        verify(this.cacheRefreshService).refreshAfterCommit(this.permissionRowCache);
    }

    /**
     * 新增规则：显式禁用时保留禁用状态
     */
    @Test
    @DisplayName("新增规则：显式禁用时保留禁用状态")
    void shouldKeepDisabledFlagOnAdd() {
        doReturn(true).when(this.service).save(any(PermissionRow.class));
        PermissionRowVO vo = this.vo("role", "1", "1");
        vo.setEnableFlag(Boolean.FALSE);

        this.service.add(vo);

        ArgumentCaptor<PermissionRow> captor = ArgumentCaptor.forClass(PermissionRow.class);
        verify(this.service).save(captor.capture());
        assertThat(captor.getValue().getEnableFlag()).isFalse();
    }

    /**
     * 新增规则：数据为空时拒绝
     */
    @Test
    @DisplayName("新增规则：数据为空")
    void shouldRejectNullVoOnAdd() {
        assertThatThrownBy(() -> this.service.add(null))
                .isInstanceOf(ArtException.class)
                .hasMessage("数据为空，请检查！");
        verify(this.service, never()).save(any(PermissionRow.class));
        verify(this.cacheRefreshService, never()).refreshAfterCommit(any());
    }

    /**
     * 新增规则：缺少授权主体类型时拒绝
     */
    @Test
    @DisplayName("新增规则：授权主体类型为空")
    void shouldRejectBlankSubjectTypeOnAdd() {
        assertThatThrownBy(() -> this.service.add(this.vo(" ", "1", "1")))
                .isInstanceOf(ArtException.class)
                .hasMessage("授权主体类型不能为空");
        verify(this.service, never()).save(any(PermissionRow.class));
    }

    /**
     * 新增规则：缺少授权主体时拒绝
     */
    @Test
    @DisplayName("新增规则：授权主体为空")
    void shouldRejectBlankPermissionSubjectOnAdd() {
        assertThatThrownBy(() -> this.service.add(this.vo("role", " ", "1")))
                .isInstanceOf(ArtException.class)
                .hasMessage("授权主体不能为空");
    }

    /**
     * 新增规则：缺少授权范围时拒绝
     */
    @Test
    @DisplayName("新增规则：授权范围为空")
    void shouldRejectBlankPermissionScopeOnAdd() {
        assertThatThrownBy(() -> this.service.add(this.vo("role", "1", " ")))
                .isInstanceOf(ArtException.class)
                .hasMessage("授权范围不能为空");
    }

    /**
     * 新增规则：自定义部门范围缺少部门时拒绝
     */
    @Test
    @DisplayName("新增规则：自定义部门范围为空")
    void shouldRejectBlankCustomDeptScopeOnAdd() {
        PermissionRowVO vo = this.vo("role", "1", "4");
        vo.setCustomDeptScope(" ");
        assertThatThrownBy(() -> this.service.add(vo))
                .isInstanceOf(ArtException.class)
                .hasMessage("自定义部门权限不能为空");
    }

    /**
     * 新增规则：自定义字段缺少字段名时拒绝
     */
    @Test
    @DisplayName("新增规则：自定义字段的权限字段为空")
    void shouldRejectBlankColumnConditionOnAdd() {
        PermissionRowVO vo = this.vo("role", "1", "5");
        vo.setColumnRelation("eq");
        vo.setColumnValue("1");
        assertThatThrownBy(() -> this.service.add(vo))
                .isInstanceOf(ArtException.class)
                .hasMessage("权限字段不能为空");
    }

    /**
     * 新增规则：自定义字段缺少字段关系时拒绝
     */
    @Test
    @DisplayName("新增规则：自定义字段的字段关系为空")
    void shouldRejectBlankColumnRelationOnAdd() {
        PermissionRowVO vo = this.vo("role", "1", "5");
        vo.setColumnCondition("amount");
        vo.setColumnValue("1");
        assertThatThrownBy(() -> this.service.add(vo))
                .isInstanceOf(ArtException.class)
                .hasMessage("字段关系不能为空");
    }

    /**
     * 新增规则：需要字段值的字段关系缺少字段值时拒绝
     */
    @Test
    @DisplayName("新增规则：自定义字段的字段值为空")
    void shouldRejectBlankColumnValueOnAdd() {
        PermissionRowVO vo = this.vo("role", "1", "5");
        vo.setColumnCondition("amount");
        vo.setColumnRelation("eq");
        assertThatThrownBy(() -> this.service.add(vo))
                .isInstanceOf(ArtException.class)
                .hasMessage("字段值不能为空");
    }

    /**
     * 新增规则：「为空」「不为空」两种字段关系无需字段值
     */
    @Test
    @DisplayName("新增规则：为空/不为空无需字段值")
    void shouldAllowNullRelationWithoutColumnValueOnAdd() {
        doReturn(true).when(this.service).save(any(PermissionRow.class));
        for (String relation : List.of("null", "not_null")) {
            PermissionRowVO vo = this.vo("role", "1", "5");
            vo.setColumnCondition("remark");
            vo.setColumnRelation(relation);
            assertThat(this.service.add(vo)).as("字段关系[%s]", relation).isTrue();
        }
    }

    /**
     * 编辑规则：主键为空时拒绝
     */
    @Test
    @DisplayName("编辑规则：主键为空")
    void shouldRejectNullIdOnEdit() {
        assertThatThrownBy(() -> this.service.edit(this.vo("role", "1", "1")))
                .isInstanceOf(ArtException.class)
                .hasMessage("数据为空，请检查！");
    }

    /**
     * 编辑规则：规则不存在时拒绝
     */
    @Test
    @DisplayName("编辑规则：规则不存在")
    void shouldRejectNotExistingRuleOnEdit() {
        PermissionRowVO vo = this.vo("role", "1", "1");
        vo.setId(100L);
        doReturn(null).when(this.service).getById(100L);

        assertThatThrownBy(() -> this.service.edit(vo))
                .isInstanceOf(ArtException.class)
                .hasMessage("数据权限规则不存在");
        verify(this.cacheRefreshService, never()).refreshAfterCommit(any());
    }

    /**
     * 编辑规则：成功更新、沿用原有启用标识并刷新缓存
     */
    @Test
    @DisplayName("编辑规则：沿用启用标识并刷新缓存")
    void shouldUpdateAndRefreshCacheOnEdit() {
        PermissionRow existing = new PermissionRow();
        existing.setId(100L);
        existing.setEnableFlag(Boolean.FALSE);
        doReturn(existing).when(this.service).getById(100L);
        doReturn(true).when(this.service).updateById(any(PermissionRow.class));

        PermissionRowVO vo = this.vo("role", "1", "3");
        vo.setId(100L);
        assertThat(this.service.edit(vo)).isTrue();

        ArgumentCaptor<PermissionRow> captor = ArgumentCaptor.forClass(PermissionRow.class);
        verify(this.service).updateById(captor.capture());
        assertThat(captor.getValue().getEnableFlag()).isFalse();
        verify(this.cacheRefreshService).refreshAfterCommit(this.permissionRowCache);
    }

    /**
     * 删除规则：空集合直接返回失败
     */
    @Test
    @DisplayName("删除规则：空集合")
    void shouldReturnFalseForEmptyIdListOnDelete() {
        assertThat(this.service.delete(List.of())).isFalse();
        assertThat(this.service.delete(null)).isFalse();
        verify(this.service, never()).removeByIds(anyList());
        verify(this.cacheRefreshService, never()).refreshAfterCommit(any());
    }

    /**
     * 删除规则：包含他租户规则时拒绝
     */
    @Test
    @DisplayName("删除规则：包含非本租户规则")
    void shouldRejectCrossTenantDelete() {
        doReturn(1L).when(this.service).count(any(QueryWrapper.class));

        assertThatThrownBy(() -> this.service.delete(List.of(1L, 2L)))
                .isInstanceOf(ArtException.class)
                .hasMessage("包含非本租户的数据权限规则，操作失败！");
        verify(this.service, never()).removeByIds(anyList());
        verify(this.cacheRefreshService, never()).refreshAfterCommit(any());
    }

    /**
     * 删除规则：成功删除并刷新缓存
     */
    @Test
    @DisplayName("删除规则：成功并刷新缓存")
    void shouldDeleteAndRefreshCache() {
        doReturn(1L).when(this.service).count(any(QueryWrapper.class));
        doReturn(true).when(this.service).removeByIds(anyList());

        assertThat(this.service.delete(List.of(1L))).isTrue();
        verify(this.cacheRefreshService).refreshAfterCommit(this.permissionRowCache);
    }

    /**
     * 启停规则：参数不完整时拒绝
     */
    @Test
    @DisplayName("启停规则：参数不完整")
    void shouldRejectNullParameterOnUpdateStatus() {
        assertThatThrownBy(() -> this.service.updateStatus(null, Boolean.TRUE))
                .isInstanceOf(ArtException.class)
                .hasMessage("参数不完整，请检查！");
        assertThatThrownBy(() -> this.service.updateStatus(1L, null))
                .isInstanceOf(ArtException.class)
                .hasMessage("参数不完整，请检查！");
    }

    /**
     * 启停规则：规则不存在时拒绝
     */
    @Test
    @DisplayName("启停规则：规则不存在")
    void shouldRejectNotExistingRuleOnUpdateStatus() {
        doReturn(null).when(this.service).getById(100L);

        assertThatThrownBy(() -> this.service.updateStatus(100L, Boolean.FALSE))
                .isInstanceOf(ArtException.class)
                .hasMessage("数据权限规则不存在");
    }

    /**
     * 启停规则：状态未变化时不更新、不刷新缓存
     */
    @Test
    @DisplayName("启停规则：状态未变化时不做更新")
    void shouldSkipUpdateWhenStatusUnchanged() {
        PermissionRow existing = new PermissionRow();
        existing.setId(100L);
        existing.setEnableFlag(Boolean.TRUE);
        doReturn(existing).when(this.service).getById(100L);

        assertThat(this.service.updateStatus(100L, Boolean.TRUE)).isTrue();
        verify(this.service, never()).updateById(any(PermissionRow.class));
        verify(this.cacheRefreshService, never()).refreshAfterCommit(any());
    }

    /**
     * 启停规则：状态变化时更新并刷新缓存
     */
    @Test
    @DisplayName("启停规则：状态变化时刷新缓存")
    void shouldRefreshCacheWhenStatusChanged() {
        PermissionRow existing = new PermissionRow();
        existing.setId(100L);
        existing.setEnableFlag(Boolean.TRUE);
        doReturn(existing).when(this.service).getById(100L);
        doReturn(true).when(this.service).updateById(any(PermissionRow.class));

        assertThat(this.service.updateStatus(100L, Boolean.FALSE)).isTrue();

        ArgumentCaptor<PermissionRow> captor = ArgumentCaptor.forClass(PermissionRow.class);
        verify(this.service).updateById(captor.capture());
        assertThat(captor.getValue().getEnableFlag()).isFalse();
        verify(this.cacheRefreshService).refreshAfterCommit(this.permissionRowCache);
    }

    /**
     * 构建数据权限VO
     *
     * @param subjectType       授权主体类型
     * @param permissionSubject 授权主体
     * @param permissionScope   授权范围
     * @return 数据权限VO
     */
    private PermissionRowVO vo(String subjectType, String permissionSubject, String permissionScope) {
        PermissionRowVO vo = new PermissionRowVO();
        vo.setSubjectType(subjectType);
        vo.setPermissionSubject(permissionSubject);
        vo.setPermissionScope(permissionScope);
        return vo;
    }
}

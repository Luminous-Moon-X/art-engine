package com.art.service.impl;

import cn.hutool.crypto.digest.MD5;
import com.art.cache.RuleCache;
import com.art.constants.TenantConstants;
import com.art.domain.Dept;
import com.art.domain.Role;
import com.art.domain.Tenant;
import com.art.domain.TenantPackage;
import com.art.domain.User;
import com.art.domain.vo.RuleItemVO;
import com.art.domain.vo.TenantVO;
import com.art.exception.ArtException;
import com.art.mapper.DeptMapper;
import com.art.mapper.RoleMapper;
import com.art.mapper.TenantMapper;
import com.art.mapper.TenantPackageMapper;
import com.art.mapper.UserMapper;
import com.art.service.TenantService;
import com.art.tenant.TenantSubjectValidator;
import com.art.tenant.TenantSupport;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.art.utils.SecurityUtil;
import com.art.utils.StringUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户服务实现类（租户为系统级数据，操作不受租户过滤）
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService, TenantSubjectValidator {
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;
    /**
     * 租户套餐Mapper
     */
    private final TenantPackageMapper tenantPackageMapper;
    /**
     * 用户Mapper
     */
    private final UserMapper userMapper;
    /**
     * 角色Mapper（角色为租户级数据）
     */
    private final RoleMapper roleMapper;
    /**
     * 部门Mapper（部门为租户级数据）
     */
    private final DeptMapper deptMapper;
    /**
     * 规则缓存（默认密码）
     */
    private final RuleCache ruleCache;
    /**
     * Redis客户端
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 构造函数
     *
     * @param tenantSupport       多租户支持
     * @param tenantPackageMapper 租户套餐Mapper
     * @param userMapper          用户Mapper
     * @param roleMapper          角色Mapper
     * @param deptMapper          部门Mapper
     * @param ruleCache           规则缓存
     * @param redisTemplate       Redis客户端
     */
    public TenantServiceImpl(TenantSupport tenantSupport, TenantPackageMapper tenantPackageMapper, UserMapper userMapper,
                             RoleMapper roleMapper, DeptMapper deptMapper,
                             RuleCache ruleCache, RedisTemplate<String, String> redisTemplate) {
        this.tenantSupport = tenantSupport;
        this.tenantPackageMapper = tenantPackageMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.deptMapper = deptMapper;
        this.ruleCache = ruleCache;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 按ID查询启用中的租户
     *
     * @param tenantId 租户ID
     * @return 租户（不存在或已禁用返回null）
     */
    @Override
    public Tenant selectEnabledById(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        // 租户为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> this.getOne(QueryWrapper.create()
                .eq(Tenant::getId, tenantId)
                .eq(Tenant::getEnableFlag, 1)));
    }

    /**
     * 分页查询租户信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 租户信息
     */
    @Override
    public Page<TenantVO> queryPage(Page<TenantVO> page, TenantVO vo) {
        // 租户为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
            wrapper.orderBy(Tenant::getCreateTime, false);
            Page<Tenant> entityPage = this.getMapper().paginate(
                    new Page<>(page.getPageNumber(), page.getPageSize()), wrapper);
            List<TenantVO> records = new ArrayList<>();
            for (Tenant tenant : entityPage.getRecords()) {
                records.add(convertToVO(tenant));
            }
            fillPackageNameAndAdminName(records);
            return new Page<>(records, entityPage.getPageNumber(), entityPage.getPageSize(), entityPage.getTotalRow());
        });
    }

    /**
     * 批量填充套餐名称与管理员用户名
     *
     * @param records 租户VO列表
     */
    private void fillPackageNameAndAdminName(List<TenantVO> records) {
        if (records.isEmpty()) {
            return;
        }
        // 套餐名称
        List<Long> packageIds = records.stream().map(TenantVO::getPackageId).distinct().toList();
        Map<Long, String> packageNameMap = tenantPackageMapper.selectListByIds(packageIds).stream()
                .collect(Collectors.toMap(TenantPackage::getId, TenantPackage::getPackageName, (a, b) -> a));
        // 管理员用户名（租户下第一个 admin 类型用户）
        List<Long> tenantIds = records.stream().map(TenantVO::getId).toList();
        Map<Long, String> adminNameMap = userMapper.selectListByQuery(QueryWrapper.create()
                        .in(User::getTenantId, tenantIds)
                        .eq(User::getUserType, TenantConstants.USER_TYPE_ADMIN))
                .stream()
                .collect(Collectors.toMap(User::getTenantId, User::getUserName, (a, b) -> a));
        for (TenantVO vo : records) {
            vo.setPackageName(packageNameMap.get(vo.getPackageId()));
            vo.setAdminUsername(adminNameMap.get(vo.getId()));
        }
    }

    /**
     * 添加租户（同时创建该租户的管理员账号，密码使用规则表默认密码）
     *
     * @param vo 租户信息
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(TenantVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        if (StringUtil.isBlank(vo.getTenantCode())) {
            throw new ArtException("租户编码不能为空！");
        }
        if (StringUtil.isBlank(vo.getTenantName())) {
            throw new ArtException("租户名称不能为空！");
        }
        if (vo.getPackageId() == null) {
            throw new ArtException("请选择租户套餐！");
        }
        if (StringUtil.isBlank(vo.getAdminUsername())) {
            throw new ArtException("请输入租户管理员用户名！");
        }
        // 租户为系统级数据，校验与落库不受租户过滤
        return tenantSupport.systemScope(() -> {
            // 租户编码唯一
            long tenantCount = this.count(QueryWrapper.create().eq(Tenant::getTenantCode, vo.getTenantCode()));
            if (tenantCount > 0) {
                throw new ArtException("租户编码已存在！");
            }
            // 套餐存在且启用
            TenantPackage tenantPackage = tenantPackageMapper.selectOneByQuery(QueryWrapper.create()
                    .eq(TenantPackage::getId, vo.getPackageId())
                    .eq(TenantPackage::getEnableFlag, 1));
            if (tenantPackage == null) {
                throw new ArtException("租户套餐不存在或已禁用！");
            }
            // 管理员用户名全局唯一
            long adminCount = userMapper.selectCountByQuery(QueryWrapper.create().eq(User::getUserName, vo.getAdminUsername()));
            if (adminCount > 0) {
                throw new ArtException("管理员用户名已存在！");
            }
            // 保存租户
            Tenant tenant = new Tenant();
            tenant.setTenantCode(vo.getTenantCode());
            tenant.setTenantName(vo.getTenantName());
            tenant.setPackageId(vo.getPackageId());
            tenant.setEnableFlag(vo.getEnableFlag() == null ? 1 : vo.getEnableFlag());
            tenant.setExpireDate(vo.getExpireDate());
            tenant.setRemark(vo.getRemark());
            if (!this.save(tenant)) {
                throw new ArtException("租户保存失败，请联系管理员！");
            }
            // 创建租户管理员账号
            this.createTenantAdminUser(tenant, vo.getAdminUsername());
            return true;
        });
    }

    /**
     * 创建租户管理员账号（userType=admin，密码使用规则表默认密码）
     *
     * @param tenant       租户
     * @param adminUsername 管理员用户名
     */
    private void createTenantAdminUser(Tenant tenant, String adminUsername) {
        RuleItemVO systemDefaultPwd = ruleCache.getByRuleCode("system_default_pwd");
        if (systemDefaultPwd == null || StringUtil.isBlank(systemDefaultPwd.getRuleValue())) {
            throw new ArtException("未配置默认密码规则，请联系管理员！");
        }
        String password = systemDefaultPwd.getRuleValue();
        String md5Pwd = MD5.create().digestHex(password);
        String encodePwd = new BCryptPasswordEncoder().encode(md5Pwd);

        User user = new User();
        // 系统级作用域内手动指定租户ID（避免租户插件覆盖）
        user.setTenantId(tenant.getId());
        user.setUserName(adminUsername);
        user.setNickName(tenant.getTenantName());
        user.setPassword(encodePwd);
        user.setUserType(TenantConstants.USER_TYPE_ADMIN);
        user.setEnableFlag(1);
        user.setFirstLoginFlag(1);
        if (userMapper.insert(user) <= 0) {
            throw new ArtException("租户管理员创建失败，请联系管理员！");
        }
    }

    /**
     * 编辑租户信息
     *
     * @param vo 租户信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(TenantVO vo) {
        if (vo == null || vo.getId() == null) {
            throw new ArtException("数据为空，请检查！");
        }
        // 租户为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            Tenant oldTenant = this.getById(vo.getId());
            if (oldTenant == null) {
                throw new ArtException("租户不存在！");
            }
            // 租户编码唯一（排除自身）
            if (StringUtil.isNotBlank(vo.getTenantCode()) && !vo.getTenantCode().equals(oldTenant.getTenantCode())) {
                long count = this.count(QueryWrapper.create()
                        .eq(Tenant::getTenantCode, vo.getTenantCode())
                        .ne(Tenant::getId, vo.getId()));
                if (count > 0) {
                    throw new ArtException("租户编码已存在！");
                }
            }
            // 套餐存在且启用
            if (vo.getPackageId() != null && !vo.getPackageId().equals(oldTenant.getPackageId())) {
                TenantPackage tenantPackage = tenantPackageMapper.selectOneByQuery(QueryWrapper.create()
                        .eq(TenantPackage::getId, vo.getPackageId())
                        .eq(TenantPackage::getEnableFlag, 1));
                if (tenantPackage == null) {
                    throw new ArtException("租户套餐不存在或已禁用！");
                }
            }
            Tenant entity = ConvertUtil.convert(vo, Tenant.class);
            return this.updateById(entity);
        });
    }

    /**
     * 删除租户信息
     *
     * @param idList 租户ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            throw new ArtException("请选择要删除的租户！");
        }
        // 租户为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            for (Long tenantId : idList) {
                if (tenantSupport.getDefaultTenantId().equals(tenantId)) {
                    throw new ArtException("默认租户不允许删除！");
                }
                long userCount = userMapper.selectCountByQuery(QueryWrapper.create().eq(User::getTenantId, tenantId));
                if (userCount > 0) {
                    throw new ArtException("租户下存在用户，无法删除！");
                }
            }
            return this.removeByIds(idList);
        });
    }

    /**
     * 查询启用中的租户列表（登录页租户下拉框、超级管理员切换租户使用）
     *
     * @return 租户列表
     */
    @Override
    public List<TenantVO> listEnabled() {
        // 租户为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            List<Tenant> tenantList = this.list(QueryWrapper.create()
                    .eq(Tenant::getEnableFlag, 1)
                    .orderBy(Tenant::getId, true));
            return tenantList.stream().map(this::convertToVO).toList();
        });
    }

    /**
     * 切换当前登录用户的生效租户（仅超级管理员）
     *
     * @param tenantId 目标租户ID
     * @return 切换结果
     */
    @Override
    public Boolean switchTenant(Long tenantId) {
        String userType = SecurityUtil.getUserType();
        if (!tenantSupport.isSuperAdmin(userType)) {
            throw new ArtException("仅超级管理员可以切换租户！");
        }
        if (tenantId == null) {
            throw new ArtException("租户ID不能为空！");
        }
        Tenant tenant = tenantSupport.systemScope(() -> this.getById(tenantId));
        if (tenant == null || !Integer.valueOf(1).equals(tenant.getEnableFlag())) {
            throw new ArtException("租户不存在或已禁用！");
        }
        String token = SecurityUtil.getToken();
        if (StringUtil.isBlank(token)) {
            throw new ArtException("登录状态已失效，请重新登录！");
        }
        redisTemplate.opsForValue().set(TenantConstants.TENANT_CONTEXT_KEY_PREFIX + token, String.valueOf(tenantId));
        return true;
    }

    /**
     * 获取当前生效租户可用的菜单权限标识集合（空=不限制）
     *
     * @return 菜单权限标识集合
     */
    @Override
    public List<String> getCurrentTenantPermissionSigns() {
        return this.getTenantPermissionSigns(SecurityUtil.getTenantId());
    }

    /**
     * 根据租户ID获取其套餐菜单权限标识集合（空=不限制）
     *
     * @param tenantId 租户ID
     * @return 菜单权限标识集合
     */
    private List<String> getTenantPermissionSigns(Long tenantId) {
        if (tenantId == null) {
            return Collections.emptyList();
        }
        // 租户/套餐为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            Tenant tenant = this.getById(tenantId);
            if (tenant == null || tenant.getPackageId() == null) {
                return Collections.emptyList();
            }
            TenantPackage tenantPackage = tenantPackageMapper.selectOneByQuery(QueryWrapper.create()
                    .eq(TenantPackage::getId, tenant.getPackageId())
                    .eq(TenantPackage::getEnableFlag, 1));
            if (tenantPackage == null) {
                return Collections.emptyList();
            }
            return tenantSupport.splitSigns(tenantPackage.getPermissionSigns());
        });
    }

    /**
     * 根据租户ID获取租户名称
     *
     * @param tenantId 租户ID
     * @return 租户名称
     */
    @Override
    public String getTenantName(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        // 租户为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            Tenant tenant = this.getById(tenantId);
            return tenant == null ? null : tenant.getTenantName();
        });
    }

    /**
     * 校验授权主体（角色/用户/部门）属于当前生效租户
     *
     * <p>注意：此处必须在当前租户上下文内查询（角色/用户/部门均为租户级数据，
     * 自动按当前租户过滤），防止权限关系写入越权到其他租户。</p>
     *
     * @param type 主体类型：role / user / dept
     * @param id   主体ID
     */
    @Override
    public void validateAssignable(String type, Long id) {
        if (id == null) {
            throw new ArtException("授权对象ID不能为空！");
        }
        long count;
        switch (type) {
            case "role" -> count = roleMapper.selectCountByQuery(QueryWrapper.create().eq(Role::getId, id));
            case "user" -> count = userMapper.selectCountByQuery(QueryWrapper.create().eq(User::getId, id));
            case "dept" -> count = deptMapper.selectCountByQuery(QueryWrapper.create().eq(Dept::getId, id));
            default -> throw new ArtException("权限类型错误，请联系管理员！");
        }
        if (count <= 0) {
            throw new ArtException("授权对象不存在或不属于当前租户，请重新选择！");
        }
    }

    /**
     * 实体转VO（仅基础字段，不含套餐名称/管理员用户名）
     *
     * @param tenant 租户实体
     * @return 租户VO
     */
    private TenantVO convertToVO(Tenant tenant) {
        TenantVO vo = new TenantVO();
        vo.setId(tenant.getId());
        vo.setTenantCode(tenant.getTenantCode());
        vo.setTenantName(tenant.getTenantName());
        vo.setPackageId(tenant.getPackageId());
        vo.setEnableFlag(tenant.getEnableFlag());
        vo.setExpireDate(tenant.getExpireDate());
        vo.setRemark(tenant.getRemark());
        vo.setCreateTime(tenant.getCreateTime());
        return vo;
    }
}
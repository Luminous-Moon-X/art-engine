package com.art.service.impl;

import cn.hutool.crypto.digest.MD5;
import com.art.domain.User;
import com.art.domain.UserRole;
import com.art.domain.vo.UserVO;
import com.art.exception.ArtException;
import com.art.mapper.UserMapper;
import com.art.mapper.UserRoleMapper;
import com.art.service.UserService;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 用户服务实现类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    /**
     * 用户角色关系Mapper
     */
    private final UserRoleMapper userRoleMapper;

    /**
     * 构造函数
     *
     * @param userRoleMapper 用户角色Mapper
     */
    public UserServiceImpl(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    /**
     * 根据ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    public User selectById(Long id) {
        return this.getById(id);
    }

    /**
     * 分页查询用户信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 用户信息
     */
    @Override
    public Page<UserVO> queryPage(Page<UserVO> page, UserVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        Page<UserVO> pageResult = this.getMapper().paginateAs(page, wrapper, UserVO.class);
        List<UserVO> records = pageResult.getRecords();
        for (UserVO user : records) {
            Long userId = user.getId();
            List<UserRole> userRoles = userRoleMapper.selectListByQuery(QueryWrapper.create().eq(UserRole::getUserId, userId));
            Long[] roleIds = userRoles.stream().map(UserRole::getRoleId).toArray(Long[]::new);
            user.setRoleIds(roleIds);
        }
        pageResult.setRecords(records);
        return pageResult;
    }

    /**
     * 查询所有用户信息
     *
     * @return 用户信息
     */
    @Override
    public List<User> selectList() {
        return this.list();
    }

    /**
     * 添加用户信息
     *
     * @param vo 用户信息
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(UserVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        User entity = ConvertUtil.convert(vo, User.class);
        // 默认密码
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String md5Pwd = MD5.create().digestHex(password);
        String encodePwd = encoder.encode(md5Pwd);
        entity.setPassword(encodePwd);
        boolean result = this.save(entity);
        // 保存角色关系
        Long[] roleIds = vo.getRoleIds();
        return userRoleRelation(entity, result, roleIds);
    }

    /**
     * 编辑用户信息
     *
     * @param vo 用户信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(UserVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        User entity = ConvertUtil.convert(vo, User.class);
        boolean result = this.updateById(entity);
        // 刷新角色关系
        Long[] roleIds = vo.getRoleIds();
        this.userRoleMapper.deleteByQuery(QueryWrapper.create().eq(UserRole::getUserId, entity.getId()));
        return userRoleRelation(entity, result, roleIds);
    }

    /**
     * 保存用户角色关系
     *
     * @param entity  用户信息
     * @param result  保存结果
     * @param roleIds 角色ID列表
     * @return 保存结果
     */
    private Boolean userRoleRelation(User entity, boolean result, Long[] roleIds) {
        List<UserRole> userRoleList = Arrays.stream(roleIds).map(roleId -> {
            Long userId = entity.getId();
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            return userRole;
        }).toList();
        userRoleMapper.insertBatch(userRoleList);
        return result;
    }

    /**
     * 删除用户信息
     *
     * @param idList 用户ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        this.userRoleMapper.deleteByQuery(QueryWrapper.create().in(UserRole::getUserId, idList));
        return this.removeByIds(idList);
    }

}

package com.art.service;

import com.art.common.TreeSelectVO;
import com.art.domain.User;
import com.art.domain.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 用户服务
 *
 * @author Luminous.X
 * @since 1.0.0
 */
public interface UserService extends IService<User> {
    /**
     * 根据ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User selectById(Long id);

    /**
     * 分页查询用户信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 用户信息
     */
    Page<UserVO> queryPage(Page<UserVO> page, UserVO vo);

    /**
     * 查询所有用户信息
     *
     * @return 用户信息
     */
    List<User> selectList();

    /**
     * 添加用户信息
     *
     * @param vo 用户信息
     * @return 添加结果
     */
    Boolean add(UserVO vo);

    /**
     * 编辑用户信息
     *
     * @param vo 用户信息
     * @return 编辑结果
     */
    Boolean edit(UserVO vo);

    /**
     * 删除用户信息
     *
     * @param idList 用户ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);

    /**
     * 查询部门用户树
     *
     * @return 部门用户树
     */
    List<TreeSelectVO> deptUserTree();

    /**
     * 查询用户树
     *
     * @return 用户树
     */
    List<TreeSelectVO> userTree();
}

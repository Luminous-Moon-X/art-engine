package com.art.service.impl;

import com.art.domain.User;
import com.art.domain.vo.UserVO;
import com.art.exception.ArtException;
import com.art.mapper.UserMapper;
import com.art.service.UserService;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return this.getMapper().paginateAs(page, wrapper, UserVO.class);
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
        return this.save(entity);
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
        return this.updateById(entity);
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
        return this.removeByIds(idList);
    }

}

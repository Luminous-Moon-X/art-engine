/*
 *    Copyright 2024-2025, Warm-Flow (290631660@qq.com).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
// Modified by Luminous.X on 2026.10.09
package org.dromara.warm.flow.orm.dao;

import com.mybatisflex.core.query.QueryWrapper;
import org.dromara.warm.flow.core.entity.RootEntity;
import org.dromara.warm.flow.core.orm.agent.WarmQuery;
import org.dromara.warm.flow.core.orm.dao.WarmDao;
import org.dromara.warm.flow.core.utils.ObjectUtil;
import org.dromara.warm.flow.core.utils.StringUtils;
import org.dromara.warm.flow.core.utils.page.OrderBy;
import org.dromara.warm.flow.core.utils.page.Page;
import org.dromara.warm.flow.orm.mapper.WarmMapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * BaseMapper接口
 *
 * @author warm
 * @since 2023-03-17
 */
public abstract class WarmDaoImpl<T extends RootEntity> implements WarmDao<T> {

    public abstract WarmMapper<T> getMapper();

    /**
     * 根据id查询
     *
     * @param id 主键
     * @return 实体
     */
    @Override
    public T selectById(Serializable id) {
        return getMapper().selectOneById(id);
    }

    /**
     * 根据ids查询
     *
     * @param ids 主键
     * @return 实体
     */
    @Override
    public List<T> selectByIds(Collection<? extends Serializable> ids) {
        return getMapper().selectListByIds(ids);
    }

    @Override
    public Page<T> selectPage(T entity, Page<T> page) {
        QueryWrapper queryWrapper = buildQueryWrapper(entity);
        if (StringUtils.isNotEmpty(page.getOrderBy())) {
            queryWrapper.orderBy(page.getOrderBy(), isAsc(page.getIsAsc()));
        }

        com.mybatisflex.core.paginate.Page<T> flexPage =
            getMapper().paginate(page.getPageNum(), page.getPageSize(), queryWrapper);

        if (ObjectUtil.isNotNull(flexPage)) {
            Page<T> rPage = new Page<>(flexPage.getRecords(), flexPage.getTotalRow());
            rPage.setPageNum(page.getPageNum());
            rPage.setPageSize(page.getPageSize());
            return rPage;
        }
        return Page.empty();
    }

    @Override
    public List<T> selectList(T entity, WarmQuery<T> query) {
        QueryWrapper queryWrapper = buildQueryWrapper(entity);
        if (ObjectUtil.isNotNull(query) && StringUtils.isNotEmpty(query.getOrderBy())) {
            queryWrapper.orderBy(query.getOrderBy(), isAsc(query.getIsAsc()));
        }
        return getMapper().selectListByQuery(queryWrapper);
    }

    @Override
    public long selectCount(T entity) {
        return getMapper().selectCountByQuery(buildQueryWrapper(entity));
    }

    @Override
    public int save(T entity) {
        return getMapper().insertSelective(entity);
    }

    @Override
    public int updateById(T entity) {
        return getMapper().update(entity);
    }

    @Override
    public int delete(T entity) {
        return getMapper().deleteByQuery(buildQueryWrapper(entity));
    }

    @Override
    public int deleteById(Serializable id) {
        return getMapper().deleteById(id);
    }

    @Override
    public int deleteByIds(Collection<? extends Serializable> ids) {
        return getMapper().deleteBatchByIds(ids);
    }

    @Override
    public void saveBatch(List<T> list) {
        for (T record : list) {
            save(record);
        }
    }

    @Override
    public void updateBatch(List<T> list) {
        for (T record : list) {
            updateById(record);
        }
    }

    /**
     * 根据实体非空属性构建查询条件
     *
     * @param entity 实体
     * @return 查询条件
     */
    protected QueryWrapper buildQueryWrapper(T entity) {
        return ObjectUtil.isNotNull(entity) ? QueryWrapper.create(entity) : QueryWrapper.create();
    }

    /**
     * 判断排序方向是否为升序
     *
     * @param isAsc 排序方向
     * @return true 升序
     */
    protected boolean isAsc(String isAsc) {
        return OrderBy.ASC.equalsIgnoreCase(isAsc);
    }

}

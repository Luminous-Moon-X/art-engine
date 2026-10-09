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
import org.dromara.warm.flow.core.invoker.FrameInvoker;
import org.dromara.warm.flow.core.orm.dao.FlowUserDao;
import org.dromara.warm.flow.core.utils.ArrayUtil;
import org.dromara.warm.flow.core.utils.CollUtil;
import org.dromara.warm.flow.core.utils.ObjectUtil;
import org.dromara.warm.flow.orm.entity.FlowUser;
import org.dromara.warm.flow.orm.mapper.FlowUserMapper;

import java.util.Arrays;
import java.util.List;

/**
 * 流程用户Mapper接口
 *
 * @author warm
 * @since 2023-03-29
 */
public class FlowUserDaoImpl extends WarmDaoImpl<FlowUser> implements FlowUserDao<FlowUser> {

    @Override
    public FlowUserMapper getMapper() {
        return FrameInvoker.getBean(FlowUserMapper.class);
    }

    @Override
    public FlowUser newEntity() {
        return new FlowUser();
    }

    @Override
    public int deleteByTaskIds(List<Long> taskIdList) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(FlowUser::getAssociated).in(taskIdList);
        return getMapper().deleteByQuery(queryWrapper);
    }

    @Override
    public List<FlowUser> listByAssociatedAndTypes(List<Long> associatedList, String[] types) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (CollUtil.isNotEmpty(associatedList)) {
            if (associatedList.size() == 1) {
                queryWrapper.where(FlowUser::getAssociated).eq(associatedList.get(0));
            } else {
                queryWrapper.where(FlowUser::getAssociated).in(associatedList);
            }
        }
        queryWrapper.and(FlowUser::getType).in(Arrays.asList(types), ArrayUtil.isNotEmpty(types));
        return getMapper().selectListByQuery(queryWrapper);
    }

    @Override
    public List<FlowUser> listByProcessedBys(Long associated, List<String> processedBys, String[] types) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(FlowUser::getAssociated).eq(associated, ObjectUtil.isNotNull(associated));
        if (CollUtil.isNotEmpty(processedBys)) {
            if (processedBys.size() == 1) {
                queryWrapper.and(FlowUser::getProcessedBy).eq(processedBys.get(0));
            } else {
                queryWrapper.and(FlowUser::getProcessedBy).in(processedBys);
            }
        }
        queryWrapper.and(FlowUser::getType).in(Arrays.asList(types), ArrayUtil.isNotEmpty(types));
        return getMapper().selectListByQuery(queryWrapper);
    }
}

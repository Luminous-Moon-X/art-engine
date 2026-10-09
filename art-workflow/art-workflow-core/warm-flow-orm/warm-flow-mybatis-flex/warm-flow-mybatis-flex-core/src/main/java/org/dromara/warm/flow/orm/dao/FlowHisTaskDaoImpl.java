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
import org.dromara.warm.flow.core.enums.SkipType;
import org.dromara.warm.flow.core.invoker.FrameInvoker;
import org.dromara.warm.flow.core.orm.dao.FlowHisTaskDao;
import org.dromara.warm.flow.core.utils.CollUtil;
import org.dromara.warm.flow.orm.entity.FlowHisTask;
import org.dromara.warm.flow.orm.mapper.FlowHisTaskMapper;

import java.util.Arrays;
import java.util.List;

/**
 * 历史任务记录Mapper接口
 *
 * @author warm
 * @since 2023-03-29
 */
public class FlowHisTaskDaoImpl extends WarmDaoImpl<FlowHisTask> implements FlowHisTaskDao<FlowHisTask> {

    @Override
    public FlowHisTaskMapper getMapper() {
        return FrameInvoker.getBean(FlowHisTaskMapper.class);
    }

    @Override
    public FlowHisTask newEntity() {
        return new FlowHisTask();
    }

    @Override
    public List<FlowHisTask> getNoReject(Long instanceId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .where(FlowHisTask::getInstanceId).eq(instanceId)
            .and(FlowHisTask::getSkipType).eq(SkipType.PASS.getKey())
            .orderBy(FlowHisTask::getCreateTime, false);
        return getMapper().selectListByQuery(queryWrapper);
    }

    @Override
    public List<FlowHisTask> getByInsAndNodeCodes(Long instanceId, List<String> nodeCodes) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .where(FlowHisTask::getInstanceId).eq(instanceId)
            .and(FlowHisTask::getNodeCode).in(nodeCodes, CollUtil.isNotEmpty(nodeCodes))
            .orderBy(FlowHisTask::getCreateTime, false);
        return getMapper().selectListByQuery(queryWrapper);
    }

    @Override
    public int deleteByInsIds(List<Long> instanceIds) {
        QueryWrapper queryWrapper = QueryWrapper.create().where(FlowHisTask::getInstanceId).in(instanceIds);
        return getMapper().deleteByQuery(queryWrapper);
    }

    @Override
    public List<FlowHisTask> listByTaskIdAndCooperateTypes(Long taskId, Integer[] cooperateTypes) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .where(FlowHisTask::getTaskId).eq(taskId)
            .and(FlowHisTask::getCooperateType).in(Arrays.asList(cooperateTypes));
        return getMapper().selectListByQuery(queryWrapper);
    }

}

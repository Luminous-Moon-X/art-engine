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
package org.dromara.warm.flow.orm.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.warm.flow.core.entity.Node;
import org.dromara.warm.flow.core.entity.Skip;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 流程节点对象 flow_node
 *
 * @author warm
 * @since 2023-03-29
 */
@Data
@Accessors(chain = true)
@Table("flow_node")
public class FlowNode implements Node {

    /**
     * 跳转条件
     */
    @Column(ignore = true)
    List<Skip> skipList = new ArrayList<>();
    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 删除标记
     */
    @Column(isLogicDelete = true)
    private String delFlag;

    /**
     * 节点类型（0开始节点 1中间节点 2结束节点 3互斥网关 4并行网关）
     */
    private Integer nodeType;
    /**
     * 流程id
     */
    private Long definitionId;
    /**
     * 流程节点编码   每个流程的nodeCode是唯一的,即definitionId+nodeCode唯一,在数据库层面做了控制
     */
    private String nodeCode;
    /**
     * 流程节点名称
     */
    private String nodeName;
    /**
     * 权限标识（权限类型:权限标识，可以多个，用@@隔开)
     */
    private String permissionFlag;
    /**
     * 流程签署比例值
     */
    private String nodeRatio;
    /**
     * 流程节点坐标
     */
    private String coordinate;
    /**
     * 版本
     *
     * @deprecated 下个版本废弃
     */
    @Deprecated
    private String version;
    /**
     * 任意结点跳转
     */
    private String anyNodeSkip;
    /**
     * 监听器类型
     */
    private String listenerType;
    /**
     * 监听器路径
     */
    private String listenerPath;
    /**
     * 审批表单是否自定义（Y=是 N=否）
     */
    private String formCustom;

    /**
     * 审批表单路径
     */
    private String formPath;

    /**
     * 节点扩展属性
     */
    private String ext;

}

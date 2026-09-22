package com.art.auth;

/**
 * 数据行权限-授权客体字段元数据提供者
 *
 * <p>数据行权限按授权范围过滤时依赖授权客体上的特定字段：</p>
 * <ul>
 *     <li>所属部门 / 所属部门及以下 / 自定义部门范围：依赖 {@code dept_id}；</li>
 *     <li>本人创建数据：依赖 {@code create_id}；</li>
 *     <li>自定义字段：依赖规则配置的权限字段。</li>
 * </ul>
 *
 * <p>授权客体由管理员从数据库全表清单中选择，其中可能存在不含上述字段的表。
 * 若继续拼接条件，该表的所有查询都会报「字段不存在」而整体不可用，
 * 因此需要先判定字段是否存在，不存在时跳过该规则的数据权限处理。</p>
 *
 * <p>字段元数据由 art-system 模块基于数据库系统目录（{@code pg_attribute}）实现并提供缓存，
 * 本接口只暴露 art-core 的方言所需的最小契约。返回结果必须忽略大小写。</p>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@FunctionalInterface
public interface DataAuthColumnProvider {

    /**
     * 判断指定表是否存在指定字段（表名与字段名均忽略大小写）
     *
     * @param tableName  表名
     * @param columnName 字段名
     * @return 是否存在
     */
    boolean hasColumn(String tableName, String columnName);
}

package com.art.kits;

import cn.hutool.core.collection.CollectionUtil;
import com.art.common.CommonSearchVO;
import com.art.common.DataFilter;
import com.art.common.SearchSort;
import com.art.enums.SearchFilterJoinType;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;

import java.util.List;

/**
 * Mybatis-Plus条件构造器工具类
 *
 * @author Luminous.X
 * @since 0.0.1
 */
public class WrapperUtil {
    /**
     * 构建查询条件构造器
     *
     * @param wrapper  条件构造器
     * @param searchVO 通用查询对象
     */
    public static QueryWrapper buildWrapper(QueryWrapper wrapper, CommonSearchVO searchVO) {
        // 获取查询过滤条件
        List<DataFilter> filters = searchVO.getFilters();
        if (!CollectionUtil.isEmpty(filters)) {
            for (DataFilter filter : filters) {
                if (SearchFilterJoinType.OR.getType().equals(filter.getJoinType())) {
                    wrapper.or(buildFilter(filter));
                } else {
                    wrapper.and(buildFilter(filter));
                }
            }
        }
        // 获取排序规则
        List<SearchSort> sortRules = searchVO.getSortRules();
        if (!CollectionUtil.isEmpty(sortRules)) {
            for (SearchSort sortRule : sortRules) {
                wrapper.orderBy(sortRule.getSortField(), !"DESC".equals(sortRule.getSortType()));
            }
        }
        return wrapper;
    }

    /**
     * 构建查询条件
     *
     * @param filter  查询条件
     */
    private static QueryCondition buildFilter(DataFilter filter) {
        QueryColumn column = QueryMethods.column(filter.getFieldName());
        return switch (filter.getOperationType()) {
            case "=" -> column.eq(filter.getFieldValue());
            case "!=" -> column.ne(filter.getFieldValue());
            case "<=" -> column.le(filter.getFieldValue());
            case ">=" -> column.ge(filter.getFieldValue());
            case "LIKE" -> column.like(filter.getFieldValue());
            case "NOT LIKE" -> column.notLike(filter.getFieldValue());
            case "IN" -> column.in(filter.getFieldValue());
            case "NOT IN" -> column.notIn(filter.getFieldValue());
            default ->
                    throw new IllegalArgumentException(String.format("不支持的查询条件操作符：%s", filter.getOperationType()));
        };
    }
}

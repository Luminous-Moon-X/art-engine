package com.art.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.art.annotation.Query;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;

import java.lang.reflect.Field;

/**
 * 查询助手
 *
 * @author Luminous.X
 * @since 1.0.0
 */
public class QueryHelper {

    /**
     * 自动构建查询条件
     *
     * @param vo 查询条件对象
     * @return 条件构造器Wrapper
     */
    public static QueryWrapper buildQueryWrapper(Object vo) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (vo == null) return wrapper;

        Field[] fields = vo.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(vo);
                // 只有值不为空时才构建条件
                if (ObjectUtil.isEmpty(value)) continue;

                Query queryAnno = field.getAnnotation(Query.class);
                String columnName = (queryAnno != null && StrUtil.isNotBlank(queryAnno.column()))
                        ? queryAnno.column()
                        : StrUtil.toUnderlineCase(field.getName());

                QueryColumn column = new QueryColumn(columnName);

                // 根据注解类型构建条件
                if (queryAnno == null || queryAnno.type() == Query.Type.EQ) {
                    wrapper.and(column.eq(value));
                } else if (queryAnno.type() == Query.Type.LIKE) {
                    wrapper.and(column.like(value));
                } else if (queryAnno.type() == Query.Type.LT) {
                    wrapper.and(column.lt(value));
                } else if (queryAnno.type() == Query.Type.GT) {
                    wrapper.and(column.gt(value));
                } else if (queryAnno.type() == Query.Type.LE) {
                    wrapper.and(column.le(value));
                } else if (queryAnno.type() == Query.Type.GE) {
                    wrapper.and(column.ge(value));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return wrapper;
    }
}
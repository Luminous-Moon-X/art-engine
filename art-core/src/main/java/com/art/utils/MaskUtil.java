package com.art.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * 递归脱敏工具类
 *
 * @author Luminous.X
 * @since 1.3.3
 */
public class MaskUtil {

    /**
     * 递归脱敏
     *
     * @param node              JSON节点
     * @param currentPath       当前路径
     * @param excludePaths      明确排除的路径
     * @param sensitiveKeywords 敏感字段关键词
     * @param containsMatch     是否包含匹配
     */
    public static void mask(JsonNode node,
                            String currentPath,
                            Set<String> excludePaths,
                            Set<String> sensitiveKeywords,
                            boolean containsMatch) {
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            Iterator<String> fieldNames = obj.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                String path = currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName;
                JsonNode child = obj.get(fieldName);

                if (child.isContainerNode()) {
                    mask(child, path, excludePaths, sensitiveKeywords, containsMatch);
                } else {
                    // @ApiLog 明确排除的字段，直接脱敏
                    if (excludePaths.contains(path)) {
                        obj.put(fieldName, "******");
                        continue;
                    }
                    // 全局敏感字段名兜底
                    if (isSensitiveField(fieldName, sensitiveKeywords, containsMatch)) {
                        obj.put(fieldName, "******");
                    }
                }
            }
        } else if (node.isArray()) {
            int i = 0;
            for (JsonNode item : node) {
                mask(item, currentPath + "[" + i + "]", excludePaths, sensitiveKeywords, containsMatch);
                i++;
            }
        }
    }

    /**
     * 是否是敏感字段
     *
     * @param fieldName         字段名
     * @param sensitiveKeywords 敏感字段关键词
     * @param containsMatch     是否包含匹配
     * @return 是否是敏感字段
     */
    private static boolean isSensitiveField(String fieldName,
                                            Set<String> sensitiveKeywords,
                                            boolean containsMatch) {
        String lower = fieldName.toLowerCase(Locale.ROOT);
        for (String keyword : sensitiveKeywords) {
            String kw = keyword.toLowerCase(Locale.ROOT);
            if (containsMatch) {
                if (lower.contains(kw)) {
                    return true;
                }
            } else {
                if (lower.equals(kw)) {
                    return true;
                }
            }
        }
        return false;
    }
}
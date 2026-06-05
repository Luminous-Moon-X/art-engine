package com.art.log.aspect;

import com.art.common.HttpResult;
import com.art.log.annotation.ApiLog;
import com.art.log.domain.ApiLog;
import com.art.log.service.ApiLogService;
import com.art.log.util.IpUtil;
import com.art.utils.SecurityUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 接口日志切面
 * <p>
 * 拦截标注了 @ApiLog 注解的Controller方法，自动记录接口请求行为
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLogAspect {

    private final ApiLogService apiLogService;

    /**
     * 环绕通知：拦截 @ApiLog 注解的方法
     */
    @Around("@annotation(apiLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, ApiLog apiLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 构建日志实体
        ApiLog apiLogEntity = new ApiLog();

        try {
            // 设置当前用户信息
            apiLogEntity.setUserId(SecurityUtil.getUserId());
            apiLogEntity.setUserName(SecurityUtil.getUserName());
            apiLogEntity.setNickName(SecurityUtil.getUserAllName());
        } catch (Exception e) {
            // 未登录时用户信息为空，不记录
        }

        // 设置请求信息
        if (request != null) {
            apiLogEntity.setRequestUrl(request.getRequestURI());
            apiLogEntity.setRequestMethod(request.getMethod());
            apiLogEntity.setIp(IpUtil.getIpAddr(request));
        }

        apiLogEntity.setRequestTime(LocalDateTime.now());
        apiLogEntity.setDescription(apiLog.value());

        // 记录请求参数
        try {
            String params = buildRequestParams(joinPoint);
            // 限制参数长度，避免超大参数
            if (params.length() > 5000) {
                params = params.substring(0, 5000) + "...(截断)";
            }
            apiLogEntity.setRequestParams(params);
        } catch (Exception e) {
            apiLogEntity.setRequestParams("参数解析失败");
        }

        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            // 记录成功响应
            apiLogEntity.setResponseCode(200);
            // 记录返回值
            try {
                String resultStr = JSON.toJSONString(result);
                if (resultStr.length() > 5000) {
                    resultStr = resultStr.substring(0, 5000) + "...(截断)";
                }
                apiLogEntity.setResponseResult(resultStr);
            } catch (Exception e) {
                apiLogEntity.setResponseResult("返回值序列化失败");
            }
        } catch (Throwable e) {
            // 记录异常响应
            apiLogEntity.setResponseCode(500);
            apiLogEntity.setResponseResult(e.getMessage());
            throw e;
        } finally {
            // 计算耗时
            apiLogEntity.setCostTime(System.currentTimeMillis() - startTime);
            // 异步保存日志
            try {
                apiLogService.saveAsync(apiLogEntity);
            } catch (Exception e) {
                log.error("保存接口日志失败", e);
            }
        }

        return result;
    }

    /**
     * 构建请求参数字符串
     */
    private String buildRequestParams(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (parameterNames == null || parameterNames.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < parameterNames.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(parameterNames[i]).append("\": ");
            try {
                // 过滤掉 HttpServletRequest、HttpServletResponse 等不可序列化的参数
                Object arg = args[i];
                if (arg instanceof HttpServletRequest) {
                    sb.append("\"[HttpServletRequest]\"");
                } else {
                    sb.append(JSON.toJSONString(arg));
                }
            } catch (Exception e) {
                sb.append("\"[序列化失败]\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}

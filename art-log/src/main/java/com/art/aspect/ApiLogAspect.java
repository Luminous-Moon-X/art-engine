package com.art.aspect;

import com.art.annotation.ApiLog;
import com.art.service.ApiLogService;
import com.art.util.IpUtil;
import com.art.utils.SecurityUtil;
import com.art.utils.SensitiveUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * 接口日志切面
 * <p>
 * 拦截标注了 @ApiLog 注解的Controller方法，自动记录接口请求行为
 *
 * @author Luminous.X
 * @since 1.2.0
 */
@Slf4j
@Aspect
@Component
@SuppressWarnings("unused")
public class ApiLogAspect {

    /**
     * 接口日志服务
     */
    private final ApiLogService apiLogService;


    /**
     * 构造函数
     *
     * @param apiLogService      接口日志服务
     */
    public ApiLogAspect(ApiLogService apiLogService) {
        this.apiLogService = apiLogService;
    }

    /**
     * 环绕通知：拦截 @ApiLog 注解的方法
     */
    @Around("@annotation(apiLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, ApiLog apiLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 构建日志实体（使用全限定名避免与注解类名冲突）
        com.art.domain.ApiLog apiLogEntity = new com.art.domain.ApiLog();

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
        apiLogEntity.setDescription(apiLog.description());
        apiLogEntity.setModule(apiLog.module());
        apiLogEntity.setOperationType(apiLog.operationType().getCode());

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
            String resultStr;
            try {
                Object body = result;
                if (result instanceof ResponseEntity<?> response) {
                    body = response.getBody();
                }
                if (body instanceof InputStream
                        || body instanceof Resource
                        || body instanceof StreamingResponseBody) {
                    resultStr = "[二进制流]";
                } else {
                    resultStr = SensitiveUtil.markClassAsString(body);
                    if (resultStr.length() > 5000) {
                        resultStr = resultStr.substring(0, 5000) + "...(截断)";
                    }
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

        if (args == null || args.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            // 未开启 -parameters 编译参数时参数名为 null，退化为位置参数名，避免日志为空
            String paramName = (parameterNames != null && i < parameterNames.length)
                    ? parameterNames[i] : "arg" + i;
            sb.append("\"").append(paramName).append("\": ");
            try {
                // 过滤掉 HttpServletRequest、HttpServletResponse 等不可序列化的参数
                Object arg = args[i];
                if (arg instanceof HttpServletRequest) {
                    sb.append("\"[HttpServletRequest]\"");
                } else {
                    sb.append(SensitiveUtil.markClassAsString(arg));
                }
            } catch (Exception e) {
                sb.append("\"[序列化失败]\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}

package com.art.annotation.aspect;

import com.art.annotation.IgnoreSqlLog;
import com.art.context.IgnoreSqlLogContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 忽略SQL日志切面
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Aspect
@Component
public class IgnoreSqlLogAspect {
    @Around("@annotation(ignoreLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, IgnoreSqlLog ignoreLog) {
        // 忽略SQL日志
        IgnoreSqlLogContextHolder.enable();
        Object result;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            // 恢复默认
            IgnoreSqlLogContextHolder.disable();
        }
        return result;
    }
}

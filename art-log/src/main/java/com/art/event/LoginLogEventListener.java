package com.art.event;

import com.art.domain.LoginLog;
import com.art.service.LoginLogService;
import com.art.util.IpUtil;
import com.art.util.UserAgentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 登录日志事件监听器
 * <p>
 * 监听登录事件，异步记录登录日志
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginLogEventListener {

    private final LoginLogService loginLogService;

    /**
     * 处理登录日志事件
     *
     * @param event 登录日志事件
     */
    @Async
    @EventListener
    public void handleLoginLogEvent(LoginLogEvent event) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserName(event.getUserName());
            loginLog.setNickName(event.getNickName());
            // 登录请求为白名单路径，异步线程无租户上下文，需显式写入日志归属租户
            loginLog.setTenantId(event.getTenantId());
            loginLog.setLoginIp(IpUtil.getIpAddr(event.getRequest()));
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setBrowser(UserAgentUtil.getBrowser(event.getRequest()));
            loginLog.setOs(UserAgentUtil.getOs(event.getRequest()));
            loginLog.setStatus(event.getStatus());
            loginLog.setMessage(event.getMessage());
            loginLogService.save(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }
}

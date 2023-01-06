package com.example.wpct.config;


import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * 兼容 token 和 cookie
 **/
@Slf4j
public class CustomerWebSessionManager extends DefaultWebSessionManager {
    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        Serializable sessionId = req.getHeader("token");

        if(sessionId != null){
            return sessionId;
        }
        // 如果消息头获取为空，则使用shiro原来的方式获取
        return super.getSessionId(request, response);
    }
}
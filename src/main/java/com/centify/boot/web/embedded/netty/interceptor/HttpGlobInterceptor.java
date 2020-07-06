package com.centify.boot.web.embedded.netty.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <pre>
 * <b>Http拦截器</b>
 * <b>Describe:</b>
 *
 * <b>Author: tanlin [2020/6/3 18:51]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/6/3 18:51        tanlin            new file.
 * <pre>
 */
@Slf4j
@Component
public class HttpGlobInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //TODO Spring Demo 拦截器实现
        System.out.println("Interceptor");
        return true;
    }

    /**
     * <pre>
     * <b>Controller业务抛异常时,postHandle不会执行</b>
     * <b>Describe:TODO</b>
     *
     * <b>Author: tanlin [2020/6/9 17:19]</b>
     *
     * @param request TODO
     * @param response TODO
     * @param handler TODO
     * @param modelAndView TODO
     * @return void
     * <pre>
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //TODO 强烈要求业务异常由Controller 手工处理（防止堆栈上抛，影响CPU、内存），并在拦截器的 postHandle 处理其他渲染
    }

    /**
     * <pre>
     * <b>Controller业务抛异常时，会执行afterCompletion方法</b>
     * <b>Describe:TODO</b>
     *
     * <b>Author: tanlin [2020/6/9 17:22]</b>
     *
     * @param request TODO
     * @param response TODO
     * @param handler TODO
     * @param ex TODO
     * @return void
     * <pre>
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //TODO afterCompletion 处理未知异常。已知异常必须有Controller手工处理，减少堆栈信息占用 CPU、内存
    }
}

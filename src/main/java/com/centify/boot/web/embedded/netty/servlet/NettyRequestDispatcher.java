package com.centify.boot.web.embedded.netty.servlet;

import javax.servlet.*;
import java.io.IOException;

/**
 * <pre>
 * <b>Servlet分发器</b>
 * <b>Describe:由过滤器链执行Servlet过滤器组</b>
 *
 * <b>Author: tanlin [2020/6/16 18:55]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/6/16 18:55        tanlin            new file.
 * <pre>
 */
public class NettyRequestDispatcher implements RequestDispatcher {
    public static final String DISPATCHER_TYPE = NettyRequestDispatcher.class.getName() + ".DISPATCHER_TYPE";
    private final FilterChain filterChain;

    public NettyRequestDispatcher(FilterChain filterChain) {
        this.filterChain = filterChain;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(DISPATCHER_TYPE, DispatcherType.FORWARD);
        // TODO implement
        filterChain.doFilter(request, response);
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(DISPATCHER_TYPE, DispatcherType.INCLUDE);
        // TODO implement
        filterChain.doFilter(request, response);
    }

    public void dispatch(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(DISPATCHER_TYPE, DispatcherType.REQUEST);
        filterChain.doFilter(request, response);
    }
}


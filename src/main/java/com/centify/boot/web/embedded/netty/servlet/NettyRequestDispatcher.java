package com.centify.boot.web.embedded.netty.servlet;

import javax.servlet.*;
import java.io.IOException;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/7/6 11:07]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/6 11:07        tanlin            new file.
 * <pre>
 */
public class NettyRequestDispatcher implements RequestDispatcher {
    private final ServletContext context;
    private final FilterChain filterChain;

    public NettyRequestDispatcher(ServletContext context, FilterChain filterChain) {
        this.context = context;
        this.filterChain = filterChain;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }

    public void dispatch(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }
}

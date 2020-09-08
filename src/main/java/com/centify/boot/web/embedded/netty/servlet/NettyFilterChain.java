package com.centify.boot.web.embedded.netty.servlet;

import com.centify.boot.web.embedded.netty.context.NettyServletContext;

import javax.servlet.*;
import java.io.IOException;
import java.util.Iterator;

/**
 * <pre>
 * <b>Servlet过滤器链</b>
 * <b>Describe:执行Servlet过滤器集合</b>
 *
 * <b>Author: tanlin [2020/6/16 18:54]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/6/16 18:54        tanlin            new file.
 * <pre>
 */
public class NettyFilterChain implements FilterChain {
    private Iterator<Filter> filterIterator;
    private NettyServletRegistration servletRegistration;
    private NettyServletContext servletContext;

    private NettyFilterChain() {
    }

    private static class SingletonHolder {
        public final static NettyFilterChain xcCarEraInfoJFrame = new NettyFilterChain();
    }

    public static final NettyFilterChain getInstance(NettyServletRegistration servletRegistration, Iterable<Filter> filters) {
        NettyFilterChain chain = SingletonHolder.xcCarEraInfoJFrame;
        chain.servletRegistration = servletRegistration;
        chain.filterIterator = filters.iterator();
        return chain;
    }

    public NettyServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(NettyServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (filterIterator.hasNext()) {
            filterIterator.next().doFilter(request, response, this);
        } else {
            servletRegistration.getServlet().service(request, response);
        }
    }

    public NettyServletRegistration getServletRegistration() {
        return servletRegistration;
    }

    public void setServletRegistration(NettyServletRegistration servletRegistration) {
        this.servletRegistration = servletRegistration;
    }
}

package com.centify.boot.web.embedded.netty.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;

/**
 * <pre>
 * <b>Servlet 空过滤器示例</b>
 * <b>Describe:
 * 1、Web项目参照此过滤器，实现项目的过滤器服务
 * </b>
 *
 * <b>Author: tanlin [2020/5/26 11:18]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/5/26 11:18        tanlin            new file.
 * <pre>
 */
@Component
public class HttpGlobFilter implements OrderedFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpGlobFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) {
        try {
            chain.doFilter(request, response);
        } catch (Exception ex) {
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
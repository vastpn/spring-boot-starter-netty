package com.centify.boot.web.embedded.netty.servlet;

import com.centify.boot.web.embedded.netty.context.NettyServletContext;

import javax.servlet.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <pre>
 * <b>Servlet对象</b>
 * <b>Describe:实现ServletRegistration 标准</b>
 *
 * <b>Author: tanlin [2020/6/16 16:48]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/6/16 16:48        tanlin            new file.
 * <pre>
 */
public class NettyServletRegistration extends AbstractNettyRegistration implements ServletRegistration.Dynamic {
    private Servlet servlet;

    private MultipartConfigElement multipartConfig;

    public NettyServletRegistration(NettyServletContext context, String servletName, String className, Servlet servlet) throws ClassNotFoundException, IllegalAccessException, InstantiationException, ServletException {
        super(servletName, className, context);

        if (servlet == null) {
            this.servlet = (Servlet) Class.forName(getClassName()).newInstance();
        } else {
            this.servlet = servlet;
        }
        this.servlet.init(this);
    }

    public Servlet getServlet() {

        return this.servlet;
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {

    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        return null;
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {

    }

    @Override
    public void setRunAsRole(String roleName) {

    }

    private final static AtomicInteger tempInteger = new AtomicInteger(0);

    @Override
    public Set<String> addMapping(String... urlPatterns) {

        if (urlPatterns == null) {
            return Collections.emptySet();
        }

        NettyServletContext context = getNettyContext();
        for (String urlPattern : urlPatterns) {
            Optional.ofNullable(urlPattern).ifPresent((item) -> {
                context.addServletMapping(item, getName());
            });
        }
        return context.getServletMappings().keySet();
    }

    @Override
    public Collection<String> getMappings() {
        return null;
    }

    @Override
    public String getRunAsRole() {
        return null;
    }
}


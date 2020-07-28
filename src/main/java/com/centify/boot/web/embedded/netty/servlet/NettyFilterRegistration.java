package com.centify.boot.web.embedded.netty.servlet;

import com.centify.boot.web.embedded.netty.context.NettyServletContext;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <pre>
 * <b>Servlet过滤器登记对象</b>
 * <b>Describe:实现FilterRegistration 标准
 * Filter的生命周期
 *
 * （1）Filter接口中有三个重要的方法。
 *
 *  init()方法：初始化参数，在创建Filter时自动调用。当我们需要设置初始化参数的时候，可以写到该方法中。
 *  doFilter()方法：拦截到要执行的请求时，doFilter就会执行。这里面写我们对请求和响应的预处理。
 *  destroy()方法：在销毁Filter时自动调用
 *
 * （2）Filter的生命周期
 *
 * Filter的创建和销毁由web服务器控制。
 *
 * 服务器启动的时候，web服务器创建Filter的实例对象，并调用其init方法，完成对象的初始化功能。
 * filter对象只会创建一次，init方法也只会执行一次。拦截到请求时，执行doFilter方法。可以执行多次。
 * 服务器关闭时，web服务器销毁Filter的实例对象
 * </b>
 *
 * <b>Author: tanlin [2020/6/16 18:42]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/6/16 18:42        tanlin            new file.
 * <pre>
 */
public class NettyFilterRegistration extends AbstractNettyRegistration implements FilterRegistration.Dynamic {
    private Filter filter;

    public NettyFilterRegistration(NettyServletContext context, String filterName, String className, Filter filter) throws ServletException {
        super(filterName, className, context);
        this.filter = filter;
        this.filter.init(this);
    }

    public Filter getFilter()  {
        return filter;
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {

    }

    @Override
    public Collection<String> getServletNameMappings() {
        return null;
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        NettyServletContext context = getNettyContext();
        for (String urlPattern : urlPatterns) {
            context.addFilterMapping(dispatcherTypes, isMatchAfter, urlPattern);
        }
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return null;
    }
}

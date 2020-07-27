package com.centify.boot.web.embedded.netty.servlet;

import io.netty.util.Recycler;

import javax.servlet.*;
import java.io.IOException;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

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
    private  Iterator<Filter> filterIterator;
    private  Servlet servlet;

    private final Recycler.Handle<NettyFilterChain> handle;

    private static final Recycler<NettyFilterChain> RECYCLER = new Recycler<NettyFilterChain>() {
        @Override
        protected NettyFilterChain newObject(Handle<NettyFilterChain> handle) {
            return new NettyFilterChain(handle);
        }
    };

    private NettyFilterChain(Recycler.Handle<NettyFilterChain> handle){
        this.handle=handle;
    }

    public static final NettyFilterChain getInstance(Servlet servlet, Iterable<Filter> filters){
        NettyFilterChain chain = RECYCLER.get();
        chain.servlet =servlet;
        chain.filterIterator = filters.iterator();
        return chain;
    }

    public void recycle(){
        filterIterator = null;
        servlet = null;
        handle.recycle(this);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (filterIterator.hasNext()) {
            filterIterator.next().doFilter(request, response, this);
        } else {
            servlet.service(request, response);
            recycle();
        }
    }

}

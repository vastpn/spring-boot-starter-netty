package com.centify.boot.web.embedded.netty.servlet;

import io.netty.util.Recycler;

import javax.servlet.*;
import java.io.IOException;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
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
    private ServletContext context;
    private FilterChain filterChain;


    private final Recycler.Handle<NettyRequestDispatcher> handle;

    private static final Recycler<NettyRequestDispatcher> RECYCLER = new Recycler<NettyRequestDispatcher>() {
        @Override
        protected NettyRequestDispatcher newObject(Handle<NettyRequestDispatcher> handle) {
            return new NettyRequestDispatcher(handle);
        }
    };

    private NettyRequestDispatcher(Recycler.Handle<NettyRequestDispatcher> handle){
        this.handle=handle;
    }

    public static final NettyRequestDispatcher getInstance(ServletContext context, FilterChain filterChain){
        NettyRequestDispatcher dispatcher = RECYCLER.get();
        dispatcher.context = context;
        dispatcher.filterChain = filterChain;
        return dispatcher;
    }

    public void recycle(){
        this.context = null;
        this.filterChain = null;
        handle.recycle(this);
    }


    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(DISPATCHER_TYPE, DispatcherType.FORWARD);
        // TODO implement
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(DISPATCHER_TYPE, DispatcherType.INCLUDE);
        // TODO implement
    }

    public void dispatch(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(DISPATCHER_TYPE, DispatcherType.REQUEST);
        filterChain.doFilter(request, response);
    }
}


package com.centify.boot.web.embedded.netty.handler;

import com.centify.boot.web.embedded.netty.context.NettyServletContext;
import com.centify.boot.web.embedded.netty.servlet.NettyHttpServletRequest;
import com.centify.boot.web.embedded.netty.servlet.NettyRequestDispatcher;
import com.centify.boot.web.embedded.netty.utils.NettyChannelUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/7/6 11:27]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/6 11:27        tanlin            new file.
 * <pre>
 */
@ChannelHandler.Sharable
public class DispatcherServletHandler extends SimpleChannelInboundHandler<NettyHttpServletRequest> {
    private NettyServletContext servletContext;


    private static class SingletonHolder {

        public final static DispatcherServletHandler handler = new DispatcherServletHandler();
    }
    public static DispatcherServletHandler getInstance() {
        return DispatcherServletHandler.SingletonHolder.handler;
    }

    public void setServletContext(NettyServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public NettyServletContext getServletContext() {
        return servletContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyHttpServletRequest msg) throws Exception {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        NettyRequestDispatcher dispatcherServlet = (NettyRequestDispatcher) servletContext.getRequestDispatcher(msg.getRequestURI());
        dispatcherServlet.dispatch(msg, servletResponse);
        if (!msg.isActive()){
            ctx.close();
            ReferenceCountUtil.release(msg);
            return ;
        }
        NettyChannelUtil.sendResultByteBuf(
                ctx,
                HttpResponseStatus.valueOf(servletResponse.getStatus()),
                msg,
                Unpooled.wrappedBuffer(servletResponse.getContentAsByteArray())
        );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}

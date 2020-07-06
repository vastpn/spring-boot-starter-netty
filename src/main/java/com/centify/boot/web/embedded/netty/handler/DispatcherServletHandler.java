package com.centify.boot.web.embedded.netty.handler;

import com.centify.boot.web.embedded.netty.context.NettyServletContext;
import com.centify.boot.web.embedded.netty.servlet.NettyHttpServletRequest;
import com.centify.boot.web.embedded.netty.servlet.NettyHttpServletResponse;
import com.centify.boot.web.embedded.netty.servlet.NettyRequestDispatcher;
import com.centify.boot.web.embedded.netty.utils.NettyChannelUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

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
    private final NettyServletContext servletContext;

    public DispatcherServletHandler(NettyServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyHttpServletRequest msg) throws Exception {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        try {
            NettyRequestDispatcher dispatcher = (NettyRequestDispatcher) servletContext.getRequestDispatcher(msg.getRequestURI());
            if (dispatcher == null) {
                servletResponse.sendError(404);
                return;
            }
            dispatcher.dispatch(msg, servletResponse);
                        NettyChannelUtil.sendResultByteBuf(
                    ctx,
                    HttpResponseStatus.valueOf(servletResponse.getStatus()),
                    msg,
                    Unpooled.wrappedBuffer(servletResponse.getContentAsByteArray())
            );
        } finally {
//            if (!msg.isAsyncStarted()) {
//                servletResponse.getOutputStream().close();
//            }
//            ReferenceCountUtil.release(msg);
        }
//        try {
//            MockHttpServletResponse servletResponse = new MockHttpServletResponse();
//            NettyRequestDispatcher dispatcherServlet = (NettyRequestDispatcher) servletContext.getRequestDispatcher(msg.getRequestURI());
//            dispatcherServlet.dispatch(msg, servletResponse);
//            NettyChannelUtil.sendResultByteBuf(
//                    ctx,
//                    HttpResponseStatus.valueOf(servletResponse.getStatus()),
//                    msg,
//                    Unpooled.wrappedBuffer(servletResponse.getContentAsByteArray())
//            );
//        }catch (Exception ex){
//            ctx.close();
//            ReferenceCountUtil.release(msg);
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}

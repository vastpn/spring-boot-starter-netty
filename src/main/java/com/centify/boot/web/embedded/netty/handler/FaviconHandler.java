package com.centify.boot.web.embedded.netty.handler;

import com.centify.boot.web.embedded.netty.constant.NettyConstant;
import com.centify.boot.web.embedded.netty.context.NettyServletContext;
import com.centify.boot.web.embedded.netty.servlet.NettyHttpServletRequest;
import com.centify.boot.web.embedded.netty.utils.NettyChannelUtil;
import com.centify.boot.web.embedded.netty.utils.SpringContextUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/7/6 11:21]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/6 11:21        tanlin            new file.
 * <pre>
 */
@ChannelHandler.Sharable
public class FaviconHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FaviconHandler.class);
    private NettyServletContext servletContext;

    private static class SingletonHolder {

        public final static FaviconHandler handler = new FaviconHandler();
    }
    public static FaviconHandler getInstance() {
        return SingletonHolder.handler;
    }

    public void setServletContext(NettyServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public NettyServletContext getServletContext() {
        return servletContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        try {
            if (!fullHttpRequest.decoderResult().isSuccess() ||
                    NettyConstant.HTTP_REQUEST_FAVICON.equalsIgnoreCase(fullHttpRequest.uri())) {
                ctx.close();
                return;
            }

//            Environment environment = SpringContextUtil.getBean(Environment.class);
//            System.out.println("application.name="+environment.getProperty("spring.application.name"));
//            System.out.println("server.port="+environment.getProperty("server.port"));

//            System.out.println("getAddress().getHostAddress()="+remoteInetSocketAddress.getAddress().getHostAddress());
//            System.out.println("getAddress().getHostName()="+remoteInetSocketAddress.getAddress().getHostName());
//            System.out.println("getAddress().getCanonicalHostName()="+remoteInetSocketAddress.getAddress().getCanonicalHostName());
//            System.out.println("getPort="+remoteInetSocketAddress.getPort());
//            System.out.println("getHostName="+remoteInetSocketAddress.getHostName());
//            System.out.println("getHostString="+remoteInetSocketAddress.getHostString());
//            System.out.println("--------");
//            System.out.println("客户端IP="+getRemoteIP(fullHttpRequest,ctx));
            ctx.fireChannelRead(NettyHttpServletRequest.getInstance(servletContext,
                    fullHttpRequest,(InetSocketAddress)ctx.channel().remoteAddress()));
        } catch (Exception ex) {
            ctx.close();
        }finally {
            ctx.channel().pipeline().remove(this);
            ReferenceCountUtil.release(fullHttpRequest);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}

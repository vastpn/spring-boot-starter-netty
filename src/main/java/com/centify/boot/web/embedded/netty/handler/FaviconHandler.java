package com.centify.boot.web.embedded.netty.handler;

import com.centify.boot.web.embedded.netty.constant.NettyConstant;
import com.centify.boot.web.embedded.netty.context.NettyServletContext;
import com.centify.boot.web.embedded.netty.core.NettyServletWebServer;
import com.centify.boot.web.embedded.netty.utils.NettyChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Optional;

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
    private final NettyServletContext servletContext;

    public FaviconHandler(NettyServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        try {
            Optional.of(msg).ifPresent((request -> {
                if (!request.decoderResult().isSuccess() ||
                        NettyConstant.HTTP_REQUEST_FAVICON.equalsIgnoreCase(request.uri())) {
                    ctx.close();
                    return;
                }
                ctx.fireChannelRead(NettyChannelUtil.createServletRequest(ctx, servletContext, request));
            }));
        } catch (Exception ex) {
            ctx.close();
        }finally {
            ctx.channel().pipeline().remove(this);
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
    /**
     * <pre>
     * <b>获取远程客户端IP</b>
     * <b>Describe:</b>
     *
     * <b>Author: tanlin [2020/5/24 15:28]</b>
     *
     * @param httpRequest http请求对象
     * @param channelHandlerContext 数据管道上下文对象
     * @return String 客户端IP
     * <pre>
     */
    public String getRemoteIP(FullHttpRequest httpRequest, ChannelHandlerContext channelHandlerContext) {
        Channel channel = channelHandlerContext.channel();
        String ip = "";
        try {
            String ipForwarded = httpRequest.headers().get("x-forwarded-for");
            if (StringUtils.isBlank(ipForwarded) || "unknown".equalsIgnoreCase(ipForwarded)) {
                InetSocketAddress insocket = (InetSocketAddress) channel.remoteAddress();
                ip = insocket.getAddress().getHostAddress();
            } else {
                ip = ipForwarded;
            }
        } catch (Exception e) {
            LOGGER.error("getRemoteIP(): get remote ip fail!", e);
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }
}

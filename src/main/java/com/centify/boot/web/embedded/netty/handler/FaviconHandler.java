package com.centify.boot.web.embedded.netty.handler;

import com.centify.boot.web.embedded.netty.constant.NettyConstant;
import com.centify.boot.web.embedded.netty.servlet.NettyHttpServletRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * <pre>
 * <b>Servlet业务处理器</b>
 * <b>Describe:
 * 1、验证通道
 * 2、屏蔽并丢弃Favicon浏览器请求
 * 3、包装FullHttpRequest 到 ServletRequest请求</b>
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

    private static class SingletonHolder {
        public final static FaviconHandler handler = new FaviconHandler();
    }
    public static FaviconHandler getInstance() {
        return SingletonHolder.handler;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        try {
            /**验证请求、屏蔽Favicon*/
            if (!fullHttpRequest.decoderResult().isSuccess() ||
                    NettyConstant.HTTP_REQUEST_FAVICON.equalsIgnoreCase(fullHttpRequest.uri())) {
                /*SimpleChannelInboundHandler 已自动释放资源*/
                return;
            }
            /**转换Netty请求到ServletRequest，继续执行Netty处理器（客户端IP、服务端IP信息已获取到并赋值到ServletRequest中）*/
            ctx.fireChannelRead(new NettyHttpServletRequest(
                    fullHttpRequest,(InetSocketAddress)ctx.channel().remoteAddress()));
        }catch (Exception ex){
            LOGGER.error(this.getClass().getName()+" 处理异常",ex);
        }finally {
            ctx.channel().pipeline().remove(this);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
//        LOGGER.error(this.getClass().getName()+" exceptionCaught",cause);
    }
}

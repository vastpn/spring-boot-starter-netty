package com.centify.boot.web.embedded.netty.handler;

import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
import com.centify.boot.web.embedded.netty.constant.NettyConstant;
import com.centify.boot.web.embedded.netty.factory.NettyServletWebServerFactory;
import com.centify.boot.web.embedded.netty.servlet.NettyHttpServletRequest;
import com.centify.boot.web.embedded.netty.servlet.NettyHttpServletResponse;
import com.centify.boot.web.embedded.netty.servlet.NettyRequestDispatcher;
import com.centify.boot.web.embedded.netty.servlet.NettyServletOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.net.InetSocketAddress;

/**
 * <pre>
 * <b>Netty Servlet 处理器</b>
 * <b>Describe:
 * 1、请求Servlet 标准API
 * 2、输出Servlet 响应结果
 * 3、释放自定义的 ByteBuf 资源</b>
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
public class DispatcherServletHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServletHandler.class);

    private NettyEmbeddedProperties nettyProperties;

    public NettyEmbeddedProperties getNettyProperties() {
        return nettyProperties;
    }

    public void setNettyProperties(NettyEmbeddedProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
    }

    private static class SingletonHolder {

        public final static DispatcherServletHandler handler = new DispatcherServletHandler();
    }

    public static DispatcherServletHandler getInstance() {
        return DispatcherServletHandler.SingletonHolder.handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {

        /**1、验证请求解码状态，父类已释放资源*/
        if (!fullHttpRequest.decoderResult().isSuccess()) {
            return;
        }

        /**2、过滤FAVICON请求，父类已释放资源*/
        if (NettyConstant.HTTP_REQUEST_FAVICON.equalsIgnoreCase(fullHttpRequest.uri())) {
            return;
        }

        /**3、FullHTTPRequest 转换为ServletRequest*/
        NettyHttpServletRequest servletRequest = new NettyHttpServletRequest(
                fullHttpRequest, (InetSocketAddress) ctx.channel().remoteAddress());

        /**4、分配IO堆外内存，实现Zero拷贝*/
        ByteBuf result = ctx.alloc().ioBuffer();
        try {
            /**5、初始化ServletRequestStream、ServletOutputStream、ServletResponse、DispacherServlet对象*/
            NettyServletOutputStream outputStream = new NettyServletOutputStream(result);
            NettyHttpServletResponse servletResponse = new NettyHttpServletResponse(outputStream);
            /**响应报文编码设置*/
            servletResponse.setCharacterEncoding(nettyProperties.getUriEncoding().toString());

            NettyRequestDispatcher dispatcherServlet =
                    (NettyRequestDispatcher) NettyServletWebServerFactory.servletContext
                            .getRequestDispatcher(servletRequest.getRequestURI());

            /**6、执行Servlet.dispacher分发(包含Filter、Interceptor、Service流程)*/
            dispatcherServlet.dispatch(servletRequest, servletResponse);
            if (!servletRequest.isActive()) {
                return;
            }

            /**7、获取ServletOutputStream流并封装FullHTTPResponse对象*/
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(servletResponse.getStatus()),
                    result);

            /**8、设置Response头信息*/
            fullHttpResponse.headers().setAll(servletResponse.getHeaders());
            fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, result.readableBytes());
//            if (fullHttpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE) == null) {
//                fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
//            }

            /**10、返回客户端并监听关闭，写入ByteBuf失败，不再重复写入，生产建议开启判断，防止IO缓存OOM*/
            if (ctx.channel().isWritable()) {
                ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
            } else {
                LOGGER.error("Netty IO 输出队列已满/channel非活跃状态，丢弃消息，防止Netty OOM");
                throw new Exception("Netty IO 输出队列已满/channel非活跃状态，丢弃消息，防止Netty OOM");
            }
        } catch (Exception ex) {
            LOGGER.error(this.getClass().getName() + " 处理异常", ex);
            if (result != null) {
                ReferenceCountUtil.release(result);
            }
            ctx.close();
        } finally {
            if (servletRequest != null) {
                servletRequest.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}

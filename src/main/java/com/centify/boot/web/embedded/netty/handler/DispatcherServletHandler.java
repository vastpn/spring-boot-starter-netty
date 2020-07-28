package com.centify.boot.web.embedded.netty.handler;

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
public class DispatcherServletHandler extends SimpleChannelInboundHandler<NettyHttpServletRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServletHandler.class);
    private static class SingletonHolder {

        public final static DispatcherServletHandler handler = new DispatcherServletHandler();
    }
    public static DispatcherServletHandler getInstance() {
        return DispatcherServletHandler.SingletonHolder.handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyHttpServletRequest servletRequest) throws Exception {
        ByteBuf result = ctx.alloc().ioBuffer();
        try{
            /*Servlet Request、Response*/
            NettyServletOutputStream outputStream = new NettyServletOutputStream(result);
            NettyHttpServletResponse servletResponse = new NettyHttpServletResponse(outputStream);
            NettyRequestDispatcher dispatcherServlet =
                    (NettyRequestDispatcher) NettyServletWebServerFactory.servletContext
                            .getRequestDispatcher(servletRequest.getRequestURI());

            /*Do Servlet service(Request,Response)*/
            dispatcherServlet.dispatch(servletRequest, servletResponse);
            if (!servletRequest.isActive()){
                return ;
            }

            /*Create Default HttpResponse*/
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(servletResponse.getStatus()),
                    result);

            fullHttpResponse.headers().setAll(servletResponse.getHeaders());
            /**设置要返回的内容长度*/
            fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, result.readableBytes());

            if(fullHttpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE) ==null){
                /**设置默认头信息的的MIME类型*/
                fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
            }

            if (ctx.channel().isWritable()){
                /**返回客户端并监听关闭*/
                ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
            }else {
                //
            }


        }catch (Exception ex){
            LOGGER.error(this.getClass().getName()+" 处理异常",ex);
        }finally {
            if(servletRequest!=null){
                servletRequest.close();
            }
            if (result!=null){
                ReferenceCountUtil.release(result);
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
//        LOGGER.error(this.getClass().getName()+" exceptionCaught",cause);
    }
}

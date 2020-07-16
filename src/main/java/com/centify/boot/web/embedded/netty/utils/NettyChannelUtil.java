package com.centify.boot.web.embedded.netty.utils;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.springframework.http.MediaType;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/5/24 15:37]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/5/24 15:37        tanlin            new file.
 * <pre>
 */
public final class NettyChannelUtil {

    /**
     * <pre>
     * <b>通用Netty响应客户端方法</b>
     * <b>Describe:
     * 1、设置返回Header信息
     * 2、返回服务端处理结果
     * 3、监听客户端关闭事件，避免长时间未关闭
     * 4、释放InBound里读取的ByteBuf
     * 5、OutBound 不能手动释放，因为在channel链中还需要使用响应对象，由框架自行处理</b>
     *
     * <b>Author: tanlin [2020/5/24 15:48]</b>
     *
     * @param chc TODO
     * @param request TODO
     * @param result TODO
     * @return void
     * <pre>
     */
    public static void sendResultBytes(ChannelHandlerContext chc, HttpResponseStatus status, Object request, byte[] result) {
        ByteBuf content = null;
        if (result == null) {
            Unpooled.wrappedBuffer("".getBytes());
        } else {
            content = Unpooled.wrappedBuffer(result);
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        /**设置头信息的的MIME类型*/
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        /**设置要返回的内容长度*/
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        /**返回客户端并监听关闭*/
        chc.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        /**释放从InBound里读取的ByteBuf*/
        if (request != null) {
            ReferenceCountUtil.release(request);
        }
    }

    /**
     * <pre>
     * <b>通用Netty响应客户端方法</b>
     * <b>Describe:
     * 1、设置返回Header信息
     * 2、返回服务端处理结果
     * 3、监听客户端关闭事件，避免长时间未关闭
     * 4、释放InBound里读取的ByteBuf
     * 5、OutBound 不能手动释放，因为在channel链中还需要使用响应对象，由框架自行处理</b>
     *
     * <b>Author: tanlin [2020/5/24 15:48]</b>
     *
     * @param chc TODO
     * @param request TODO
     * @param result TODO
     * @return void
     * <pre>
     */
    public static void sendResultObject(ChannelHandlerContext chc, HttpResponseStatus status, Object request, Object result) {
        ByteBuf content = Unpooled.wrappedBuffer(JSONObject.toJSONString(result).getBytes(CharsetUtil.UTF_8));

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        /**设置头信息的的MIME类型*/
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        /**设置要返回的内容长度*/
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        /**返回客户端并监听关闭*/
        chc.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        /**释放从InBound里读取的ByteBuf*/
        if (request != null) {
            ReferenceCountUtil.release(request);
        }
    }

    public static void sendResultByteBuf(ChannelHandlerContext chc, HttpResponseStatus status, ByteBuf content) {

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        /**设置头信息的的MIME类型*/
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        /**设置要返回的内容长度*/
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        /**返回客户端并监听关闭*/
        chc.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

    }
}

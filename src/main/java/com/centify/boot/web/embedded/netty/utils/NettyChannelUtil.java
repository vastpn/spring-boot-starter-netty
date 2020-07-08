package com.centify.boot.web.embedded.netty.utils;

import com.alibaba.fastjson.JSONObject;
import com.centify.boot.web.embedded.netty.context.NettyServletContext;
import com.centify.boot.web.embedded.netty.servlet.NettyHttpServletRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.util.Optional;
import java.util.stream.Collectors;

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

    public static void sendResultByteBuf(ChannelHandlerContext chc, HttpResponseStatus status, Object request, ByteBuf content) {
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
     * <b>根据fullHttpRequest请求信息，创建NettyHttpServletRequest</b>
     * <b>Describe:TODO</b>
     *
     * <b>Author: tanlin [2020/5/24 16:13]</b>
     *
     *
     * @param ctx
     * @param servletContext
     * @param fullHttpRequest Http请求对象
     * @return org.springframework.mock.web.NettyHttpServletRequest
     * <pre>
     */
    public static NettyHttpServletRequest createServletRequest(ChannelHandlerContext ctx,
                                                               NettyServletContext servletContext,
                                                               FullHttpRequest fullHttpRequest) {

        UriComponents uriComponents = UriComponentsBuilder.fromUriString(fullHttpRequest.uri()).build();

        NettyHttpServletRequest servletRequest = new NettyHttpServletRequest(ctx,servletContext,fullHttpRequest);


        if (uriComponents.getScheme() != null) {
            servletRequest.setScheme(uriComponents.getScheme());
        }
        if (uriComponents.getHost() != null) {
            servletRequest.setServerName(uriComponents.getHost());
        }
        if (uriComponents.getPort() != -1) {
            servletRequest.setServerPort(uriComponents.getPort());
        }


        /**header 参数信息*/
        setRequestHeader(fullHttpRequest, servletRequest);

        setRequestParams(fullHttpRequest, servletRequest, uriComponents);

        Optional.ofNullable(fullHttpRequest.content())
                .ifPresent((tempContent)->servletRequest.setContent(ByteBufUtil.getBytes(tempContent)));
        return servletRequest;
    }

    private static void setRequestParams(FullHttpRequest fullHttpRequest, NettyHttpServletRequest servletRequest, UriComponents uriComponents) {
        /*URL 转码 */
        if (uriComponents.getQuery() != null) {
            servletRequest.setQueryString(UriUtils.decode(uriComponents.getQuery(), CharsetUtil.UTF_8));
        }
        if (HttpMethod.GET.equals(fullHttpRequest.method())) {
            innerGetParams(servletRequest, uriComponents);
        } else if (HttpMethod.POST.equals(fullHttpRequest.method())) {
            innerPostParams(fullHttpRequest, servletRequest);
        } else if (HttpMethod.DELETE.equals(fullHttpRequest.method())) {
        }
    }

    private static void innerPostParams(FullHttpRequest fullHttpRequest, NettyHttpServletRequest servletRequest) {
        Optional.ofNullable(fullHttpRequest.headers().get("Content-Type").trim().toLowerCase())
                .ifPresent(item -> {
                    if (item.contains("multipart/form-data") || item.contains("application/x-www-form-urlencoded")) {
                        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
                        servletRequest.setParameters(decoder.getBodyHttpDatas().parallelStream()
                                .filter((data) -> data.getHttpDataType().equals(InterfaceHttpData.HttpDataType.Attribute))
                                .map((convData) -> (MemoryAttribute) convData)
                                .collect(Collectors.toMap(
                                        MemoryAttribute::getName,
                                        MemoryAttribute::getValue,
                                        (key1, key2) -> key2)));
                    }
                });
    }

    private static void innerGetParams(NettyHttpServletRequest servletRequest, UriComponents uriComponents) {
        Optional.ofNullable(uriComponents.getQueryParams().entrySet())
                .ifPresent((entrys) -> {
                    entrys.parallelStream().forEach((entry) -> {
                        entry.getValue().parallelStream().forEach((item) -> {
                            servletRequest.addParameter(
                                    UriUtils.decode(entry.getKey(), CharsetUtil.UTF_8),
                                    UriUtils.decode(item, CharsetUtil.UTF_8));
                        });
                    });
                });
    }

    private static void setRequestHeader(FullHttpRequest fullHttpRequest, NettyHttpServletRequest servletRequest) {
        Optional.ofNullable(fullHttpRequest.headers().names())
                .ifPresent((headers) -> {
                    headers.parallelStream().forEach((item) -> {
                        servletRequest.addHeader(item, fullHttpRequest.headers().get(item));
                    });
                });
    }

    private static void setRequestInfo(FullHttpRequest fullHttpRequest, UriComponents uriComponents, NettyHttpServletRequest servletRequest) {
        servletRequest.setRequestURI(uriComponents.getPath());
        servletRequest.setPathInfo(uriComponents.getPath());

        if (uriComponents.getScheme() != null) {
            servletRequest.setScheme(uriComponents.getScheme());
        }
        if (uriComponents.getHost() != null) {
            servletRequest.setServerName(uriComponents.getHost());
        }
        if (uriComponents.getPort() != -1) {
            servletRequest.setServerPort(uriComponents.getPort());
        }
    }
}

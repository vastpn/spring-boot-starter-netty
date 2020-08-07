package com.centify.boot.web.embedded.netty.core;

import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
import com.centify.boot.web.embedded.netty.handler.DispatcherServletHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * <pre>
 * <b>Netty 管道处理初始化工厂对象</b>
 * <b>Describe:
 * 1、默认提供Netty处理器链</b>
 *
 * <b>Author: tanlin [2020/7/6 11:18]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/6 11:18        tanlin            new file.
 * <pre>
 */
public class NettyServletChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final DispatcherServletHandler dispatcherServletHandler;
    private final NettyEmbeddedProperties nettyCustom;

    public NettyServletChannelInitializer(NettyEmbeddedProperties nettyCustom) {
        this.dispatcherServletHandler = DispatcherServletHandler.getInstance();
        this.nettyCustom = nettyCustom;
    }


    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        /**必须第一步对通信数据进行编解码 (已包含HttpRequestDecoder/HttpResponseEncoder)*/
        channel.pipeline()
                /**转码*/
                .addLast("HttpCodec", new HttpServerCodec())
                /**请求数据粘包设置 请KB*/
                .addLast("HttpObject", new HttpObjectAggregator(nettyCustom.getRequestDataMaxcontentlength()))
                /**用于处理大的数据流*/
                .addLast("ChunkedWrite", new ChunkedWriteHandler())
//                /**读取超时*/
//                .addLast("RTimeout", new ReadTimeoutHandler(1))
//                /**写入超时*/
//                .addLast("WTimeout", new WriteTimeoutHandler(1))
                /**转交给SpringMVC dispatcherServlet 处理业务逻辑，可正常使用Spring RestController 等注解*/
                .addLast("DispatcherServlet", dispatcherServletHandler);
    }
}

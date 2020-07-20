package com.centify.boot.web.embedded.netty.core;

import com.centify.boot.web.embedded.netty.handler.DispatcherServletHandler;
import com.centify.boot.web.embedded.netty.handler.FaviconHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
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
    private final FaviconHandler faviconHandler;
    private final DispatcherServletHandler dispatcherServletHandler;
    /**
     * 请求粘包最大长度（256KB）
     */
    private static final Integer REQUEST_DATA_MAXCONTENTLENGTH = 256 * 1024;

    public NettyServletChannelInitializer() {
        this.faviconHandler = FaviconHandler.getInstance();
        this.dispatcherServletHandler = DispatcherServletHandler.getInstance();
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        /**必须第一步对通信数据进行编解码 (已包含HttpRequestDecoder/HttpResponseEncoder)*/
        channel.pipeline()
                /**读取超时*/
                .addLast("RTimeout", new ReadTimeoutHandler(1))
                /**转码*/
                .addLast("HttpCodec", new HttpServerCodec())
                /**请求数据粘包设置*/
                .addLast("HttpObject", new HttpObjectAggregator(REQUEST_DATA_MAXCONTENTLENGTH))
                /**用于处理大的数据流*/
                .addLast("ChunkedWrite", new ChunkedWriteHandler())
                /**过滤 favicon.ico 请求*/
                .addLast("Favicon", faviconHandler)
                /**转交给SpringMVC dispatcherServlet 处理业务逻辑，可正常使用Spring RestController 等注解*/
                .addLast("DispatcherServlet", dispatcherServletHandler)
                /**写入超时*/
                .addLast("WTimeout", new WriteTimeoutHandler(1));
    }
}

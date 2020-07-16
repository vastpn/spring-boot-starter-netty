package com.centify.boot.web.embedded.netty.servlet;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/7/16 15:02]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/16 15:02        tanlin            new file.
 * <pre>
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.Recycler;
import io.netty.util.ReferenceCountUtil;
import org.springframework.http.MediaType;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ResponseServletOutputStream extends ServletOutputStream {
    private ChannelHandlerContext chc;
    private NettyHttpServletResponse servletResponse;
    private ByteBuf content;

    private final Recycler.Handle<ResponseServletOutputStream> handle;

    private static final Recycler<ResponseServletOutputStream> RECYCLER = new Recycler<ResponseServletOutputStream>() {
        @Override
        protected ResponseServletOutputStream newObject(Handle<ResponseServletOutputStream> handle) {
            return new ResponseServletOutputStream(handle);
        }
    };

    private ResponseServletOutputStream(Recycler.Handle<ResponseServletOutputStream> handle){
        this.handle=handle;
    }

    public static final ResponseServletOutputStream getInstance(ChannelHandlerContext ctx, NettyHttpServletResponse servletResponse){
        ResponseServletOutputStream outputStream = RECYCLER.get();
        outputStream.chc = ctx;
        outputStream.servletResponse = servletResponse;
        outputStream.content = Unpooled.directBuffer();
        return outputStream;
    }

    public void recycle(){
        chc = null;
        servletResponse = null;
        ReferenceCountUtil.release(this.content);
        handle.recycle(this);
    }


    @Override
    public void flush() throws IOException {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(servletResponse.getStatus()),
                content);
        /**设置头信息的的MIME类型*/
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        /**设置要返回的内容长度*/
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        /**返回客户端并监听关闭*/
        chc.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(int b) throws IOException {
        this.content.writeByte(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.content.writeBytes(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.content.writeBytes(b,off,len);
    }
}

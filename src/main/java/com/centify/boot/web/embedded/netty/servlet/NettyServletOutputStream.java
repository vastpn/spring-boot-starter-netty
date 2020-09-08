package com.centify.boot.web.embedded.netty.servlet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;

/**
 * <pre>
 * <b>Servlet 响应流</b>
 * <b>Describe:实现Servlet标准输出流管道</b>
 *
 * <b>Author: tanlin [2020/7/20 16:25]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/20 16:25        tanlin            new file.
 * <pre>
 */
public class NettyServletOutputStream extends ServletOutputStream {
    private ByteBufOutputStream outputStream;

    public NettyServletOutputStream(ByteBuf buffer) {
        this.outputStream = new ByteBufOutputStream(buffer);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        //TODO 未实现输出监听器
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        outputStream.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
        outputStream.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
        outputStream.flush();
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
        outputStream.close();
    }
}
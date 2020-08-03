package com.centify.boot.web.embedded.netty.servlet;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <pre>
 * <b>Servlet 请求流对象</b>
 * <b>Describe:采用Netty ByteBuf 高性能内存缓冲池，实现0拷贝</b>
 *
 * <b>Author: tanlin [2020/7/13 15:39]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/13 15:39        tanlin            new file.
 * <pre>
 */
public class NettyServletInputStream extends ServletInputStream {
    private AtomicBoolean closed = new AtomicBoolean(false); //输入流是否已经关闭，保证线程安全
    private ByteBuf source;
    private int contentLength;

    public NettyServletInputStream() {
    }

    public void wrap(ByteBuf source) {
        if(this.source!=null){
            ReferenceCountUtil.release(this.source);
        }

        this.closed.set(false);
        this.source = source;
        this.contentLength = source.capacity();
    }
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public int readLine(byte[] b, int off, int len) throws IOException {
        checkClosed();
        return super.readLine(b, off, len); //模板方法，会调用当前类实现的read()方法
    }

    /**
     * 本次请求没再有新的HttpContent输入，而且当前的内容全部被读完
     * @return true=读取完毕 反之false
     */
    @Override
    public boolean isFinished() {
        if(closed.get()){
            return true;
        }
        return source.readableBytes() == 0;
    }

    /**
     * 已读入至少一次HttpContent且未读取完所有内容，或者HttpContent队列非空
     */
    @Override
    public boolean isReady() {
        if(source == null){
            return true;
        }
        return source.readableBytes() > 0;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        // TODO: 监听写入事件
    }

    /**
     * 跳过n个字节
     */
    @Override
    public long skip(long n) throws IOException {
        checkClosed();
        long skipLen = Math.min(source.readableBytes(), n); //实际可以跳过的字节数
        source.skipBytes((int) skipLen);
        return skipLen;
    }

    /**
     * @return 可读字节数
     */
    @Override
    public int available() throws IOException {
        checkClosed();
        return null == source ? 0 : source.readableBytes();
    }

    @Override
    public void close() throws IOException {
        ReferenceCountUtil.release(source);
        source = null;
    }

    /**
     * 尝试更新current，然后读取len个字节并复制到b中（off下标开始）
     * @return 实际读取的字节数
     */
    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        checkClosed();
        if (0 == len) {
            return 0;
        }
        if (isFinished()) {
            return -1;
        }

        //读取len个字节
        ByteBuf byteBuf = readContent(len);
        //总共可读的字节数
        int readableBytes = byteBuf.readableBytes();
        //复制到bytes数组
        byteBuf.readBytes(bytes, off, readableBytes);
        //返回实际读取的字节数
        int size  = readableBytes - byteBuf.readableBytes();
        ReferenceCountUtil.release(byteBuf);
        return size;
    }

    /**
     * 尝试更新current，然后读取一个字节，并返回 ,这里虽然返回int, 但第三方框架都是按1个字节处理的,而不是4个字节
     */
    @Override
    public int read() throws IOException {
        checkClosed();
        if (isFinished()) {
            return -1;
        }
        return source.readByte();
    }

    /**
     * 从中读取length个字节
     */
    private ByteBuf readContent(int length) {
        if (length < source.readableBytes()) {
            return source.readSlice(length);
        } else {
            return source;
        }
    }

    private void checkClosed() throws IOException {
        if (closed.get()) {
            throw new IOException("Stream closed");
        }
    }

}
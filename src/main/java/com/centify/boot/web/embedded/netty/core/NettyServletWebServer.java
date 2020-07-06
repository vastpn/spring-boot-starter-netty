package com.centify.boot.web.embedded.netty.core;

import com.centify.boot.web.embedded.netty.context.NettyServletContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

import java.net.InetSocketAddress;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/7/6 11:14]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/6 11:14        tanlin            new file.
 * <pre>
 */
public class NettyServletWebServer implements WebServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServletWebServer.class);
    /**ServletContext 全局上下文*/
    private final NettyServletContext servletContext;

    /**监听端口地址*/
    private final InetSocketAddress address;

    /**Netty事件接收线程池组*/
    private EventLoopGroup acceptGroup;

    /**Netty事件处理线程池组*/
    private EventLoopGroup workerGroup;

    private Boolean epollFlag;

    public NettyServletWebServer(InetSocketAddress address, NettyServletContext servletContext) {
        this.address = address;
        this.servletContext = servletContext;
    }

    @Override
    public void start() throws WebServerException {
        /**服务启动对象*/
        ServerBootstrap bootstrap = new ServerBootstrap();

        epollFlag = Epoll.isAvailable();
        /**接收请求工作组*/
        initAcceptGroup();
        /**处理请求工作组*/
        initWorkerGroup();
        try {
            /**绑定接收请求、处理请求工作组，并设置HTTP/TCP通讯参数*/
            bootstrap.group(acceptGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(address.getPort())
                    /*是否允许端口占用*/
                    .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    /*设置可处理队列数量*/
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(NioChannelOption.SO_RCVBUF, 4*1024)
                    /*ByteBuf重用缓冲区*/
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    /*响应时间有高要求的场景 禁用nagle 算法*/
                    .childOption(NioChannelOption.TCP_NODELAY, Boolean.TRUE)
                    /*是否允许端口占用*/
                    .childOption(NioChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    /*是否设置长连接*/
                    .childOption(NioChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                    /*设置接收数据大小 设置为4K*/
                    .childOption(NioChannelOption.SO_RCVBUF, 4*1024)
                    /*设置发送数据大小 设置为16K*/
                    .childOption(NioChannelOption.SO_SNDBUF, 16*1024)
                    /*设置ByteBuf重用缓冲区*/
                    .childOption(NioChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new NettyServletChannelInitializer(servletContext));

            /**绑定端口，并打印端口信息*/
            ChannelFuture channelFuture = bootstrap.bind(address).syncUninterruptibly().addListener(future -> {
                StringBuilder logBanner = new StringBuilder();
                logBanner.append("\n\n")
                        .append("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n")
                        .append("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n")
                        .append("* *                                                                               * *\n")
                        .append("                     Netty Http Server started on port {}.                     \n")
                        .append("* *                                                                               * *\n")
                        .append("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n")
                        .append("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n");
                LOGGER.info(logBanner.toString(), address.getPort());
            });
            /**通过引入监听器对象监听future状态，当future任务执行完成后会调用-》{}内的方法*/
            channelFuture.channel().closeFuture().addListener(future -> {
                LOGGER.info("Netty Http服务停止开始!");
                /**优雅关闭*/
                acceptGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                LOGGER.info("Netty Http服务停止完成!");
            });
        } catch (Exception e) {
            LOGGER.error("Netty Start Error " ,e);
            acceptGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            LOGGER.error("Netty Start Error，资源释放完成！");
        }

        LOGGER.info(" started on port: " + getPort());
    }

    private void initWorkerGroup() {
        LOGGER.info("System workerGroup {} ",epollFlag);
//        if (epollFlag){
//            workerGroup = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,
//                    new DefaultThreadFactory("workerGroup"));
//
//        }else{
        workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,
                new DefaultThreadFactory("workerGroup"));
//        }
    }

    private void initAcceptGroup() {
        LOGGER.info("System acceptGroup {} ",epollFlag);
//        if (epollFlag){
//            acceptGroup = new EpollEventLoopGroup(1,
//                    new DefaultThreadFactory("acceptGroup"));
//        }else{
        acceptGroup = new NioEventLoopGroup(1,
                new DefaultThreadFactory("acceptGroup"));
//        }
    }

    /**
     * 优雅地关闭各种资源
     *
     * @throws WebServerException
     */
    @Override
    public void stop() throws WebServerException {
        LOGGER.info("Embedded Netty Servlet Container shuting down.");
        try {
            if (null != acceptGroup) {
                acceptGroup.shutdownGracefully().await();
            }
            if (null != workerGroup) {
                workerGroup.shutdownGracefully().await();
            }
        } catch (InterruptedException e) {
            throw new WebServerException("Container stop interrupted", e);
        }
    }

    @Override
    public int getPort() {
        return address.getPort();
    }
}
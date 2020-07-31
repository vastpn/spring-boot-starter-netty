package com.centify.boot.web.embedded.netty.core;

import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
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
import java.util.concurrent.ForkJoinPool;

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

    /**监听端口地址*/
    private final InetSocketAddress address;
    private final NettyEmbeddedProperties nettyCustom;

    /**Netty事件接收线程池组*/
    private EventLoopGroup acceptGroup;

    /**Netty事件处理线程池组*/
    private EventLoopGroup workerGroup;

    private Boolean epollFlag;

    public NettyServletWebServer(InetSocketAddress address, NettyEmbeddedProperties nettyCustom) {
        this.address = address;
        this.nettyCustom = nettyCustom;
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
                    .option(ChannelOption.SO_REUSEADDR, nettyCustom.getOptionSoReuseaddr())
                    /*设置可处理队列数量*/
                    .option(ChannelOption.SO_BACKLOG, nettyCustom.getOptionSoBacklog())
                    .option(ChannelOption.SO_RCVBUF, nettyCustom.getOptionSoRcvbuf())
                    /*ByteBuf重用缓冲区*/
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(NioChannelOption.WRITE_BUFFER_WATER_MARK,
                            new WriteBufferWaterMark(
                                    nettyCustom.getChildOptionWriteBufferLowWaterMark(),
                                    nettyCustom.getChildOptionWriteBufferHighWaterMark()))
                    /*响应时间有高要求的场景 禁用nagle 算法*/
                    .childOption(NioChannelOption.TCP_NODELAY, nettyCustom.getChildOptiontcpNodelay())
                    /*是否允许端口占用*/
                    .childOption(NioChannelOption.SO_REUSEADDR, nettyCustom.getChildOptionsoReuseaddr())
//                    /*是否设置长连接*/
                    .childOption(NioChannelOption.SO_KEEPALIVE, nettyCustom.getChildOptionsoKeepalive())
                    /*设置接收数据大小 设置为4K*/
                    .childOption(NioChannelOption.SO_RCVBUF, nettyCustom.getChildOptionsoRcvbuf())
                    /*设置发送数据大小 设置为16K*/
                    .childOption(NioChannelOption.SO_SNDBUF, nettyCustom.getChildOptionsoSndbuf())
                    /*设置ByteBuf重用缓冲区*/
                    .childOption(NioChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new NettyServletChannelInitializer(nettyCustom));

            /**绑定端口，并打印端口信息*/
            ChannelFuture channelFuture = bootstrap.bind(address).syncUninterruptibly().addListener(future -> {
                StringBuilder logBanner = new StringBuilder();
                logBanner.append("[Container] 容器启动成功！")
                .append("服务端IP: ").append(address.getAddress().getHostAddress())
                .append("监听端口：").append(address.getPort());
                LOGGER.info(logBanner.toString());
            });
            /**通过引入监听器对象监听future状态，当future任务执行完成后会调用-》{}内的方法*/
            channelFuture.channel().closeFuture().addListener(future -> {
                LOGGER.info("[Container] Netty Http服务停止开始!");
                /**优雅关闭*/
                acceptGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                LOGGER.info("[Container] Netty Http服务停止完成!");
            });
        } catch (Exception e) {
            LOGGER.error("[Container] 容器启动异常，开始释放资源 " ,e);
            acceptGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            LOGGER.error("[Container] Netty Start Error，资源释放完成！");
            System.exit(0);
        }
    }

    private void initWorkerGroup() {
        LOGGER.info("[Container] workerGroup线程池Epoll类型：{} ",epollFlag);
//        if (epollFlag){
//            workerGroup = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,
//                    new DefaultThreadFactory("workerGroup"));
//
//        }else{
//        workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,new DefaultThreadFactory("workerGroup"));
        workerGroup =new NioEventLoopGroup(nettyCustom.getWorkerGroupThread(),
                new ForkJoinPool(nettyCustom.getWorkerGroupThread()));
//        }
    }

    private void initAcceptGroup() {
        LOGGER.info("[Container] acceptGroup线程池Epoll类型：{} ",epollFlag);
//        if (epollFlag){
//            acceptGroup = new EpollEventLoopGroup(1,
//                    new DefaultThreadFactory("acceptGroup"));
//        }else{
        acceptGroup = new NioEventLoopGroup(
                nettyCustom.getAcceptGroupThread(),
                new DefaultThreadFactory(nettyCustom.getAcceptGroupPoolName()));
//        }
    }

    /**
     * 优雅地关闭各种资源
     *
     * @throws WebServerException
     */
    @Override
    public void stop() throws WebServerException {
        LOGGER.info("[Container] 关闭容器，开始释放资源.");
        try {
            if (null != acceptGroup) {
                acceptGroup.shutdownGracefully().await();
            }
            if (null != workerGroup) {
                workerGroup.shutdownGracefully().await();
            }
        } catch (InterruptedException e) {
            LOGGER.error("[Container] 关闭容器异常.",e);
            throw new WebServerException("Container stop interrupted", e);
        }
        LOGGER.info("[Container] 关闭容器完成.");
    }

    @Override
    public int getPort() {
        return address.getPort();
    }
}
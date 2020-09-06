package com.centify.boot.web.embedded.netty.factory;

import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
import com.centify.boot.web.embedded.netty.constant.NettyConstant;
import com.centify.boot.web.embedded.netty.context.NettyServletContext;
import com.centify.boot.web.embedded.netty.core.NettyServletWebServer;
import io.netty.bootstrap.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import javax.servlet.ServletException;
import java.net.InetSocketAddress;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/8/28 10:59]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/8/28 10:59        tanlin            new file.
 * <pre>
 */
public class NettyServletWebServerFactory extends AbstractServletWebServerFactory
        implements ConfigurableNettyServletWebServerFactory, ResourceLoaderAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServletWebServerFactory.class);

    /**
     * 环境配置对象
     */
    private final Environment environment;
    /**
     * WebServer 配置属性
     */
    private final ServerProperties serverProperties;

    private final NettyEmbeddedProperties nettyProperties;

    /**
     * 资源加载器
     */
    private ResourceLoader resourceLoader;

    /**
     * 服务端IP、端口、主机名
     * */
    public static InetSocketAddress serverAddress;

    public static NettyServletContext servletContext;


    public NettyServletWebServerFactory(Environment environment,
                                        ServerProperties serverProperties,
                                        NettyEmbeddedProperties nettyProperties){

        super(serverProperties.getPort());
        getJsp().setRegistered(false);
        this.environment = environment;
        this.serverProperties = serverProperties;
        this.nettyProperties = nettyProperties;
    }

    @Override
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        /**容器信息日志*/
        logContainer();
        /** Servlet 容器初始化工厂 上下文设置*/
        onStartup(initializers);
        /**从SpringBoot配置中获取端口，如果没有则随机生成*/
        int port = getPort() > 0 ? getPort() : 8080;
        serverAddress = new InetSocketAddress(port);
        /**初始化容器并返回*/

        return new NettyServletWebServer(serverAddress,nettyProperties);
    }


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private void logContainer() {
        /**Netty启动环境相关信息*/
        Package nettyPackage = Bootstrap.class.getPackage();
        String title = nettyPackage.getImplementationTitle();
        String version = nettyPackage.getImplementationVersion();
        LOGGER.info("{} [Container] Netty环境：{} , {} ", NettyConstant.SERVER_AND_SYSTEM_INFO,title,version);
    }

    private void onStartup(ServletContextInitializer[] initializers) {
        servletContext = new NettyServletContext(getContextPath(),resourceLoader,serverProperties,serverAddress);
        for (ServletContextInitializer initializer : initializers) {
            try {
                initializer.onStartup(servletContext);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

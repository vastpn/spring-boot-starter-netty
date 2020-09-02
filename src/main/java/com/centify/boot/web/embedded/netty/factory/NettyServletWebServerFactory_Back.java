//package com.centify.boot.web.embedded.netty.factory;
//
//import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
//import com.centify.boot.web.embedded.netty.context.NettyServletContext;
//import com.centify.boot.web.embedded.netty.core.NettyServletWebServer;
//import io.netty.bootstrap.Bootstrap;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.autoconfigure.web.ServerProperties;
//import org.springframework.boot.context.properties.PropertyMapper;
//import org.springframework.boot.web.server.WebServer;
//import org.springframework.boot.web.server.WebServerFactoryCustomizer;
//import org.springframework.boot.web.servlet.ServletContextInitializer;
//import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
//import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
//import org.springframework.context.ResourceLoaderAware;
//import org.springframework.core.Ordered;
//import org.springframework.core.env.Environment;
//import org.springframework.core.io.ResourceLoader;
//
//import javax.servlet.ServletException;
//import java.net.InetSocketAddress;
//
///**
// * <pre>
// * <b>NettyServlet WebServer 容器</b>
// * <b>Describe:
// * 1、Netty Servlet WebServer 工厂服务类
// * 2、SpringBoot 自动注入并获取web应用的容器</b>
// *
// * <b>Author: tanlin [2020/7/6 9:51]</b>
// * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
// * <b>Changelog:</b>
// *   Ver   Date                  Author           Detail
// *   ----------------------------------------------------------------------------
// *   1.0   2020/7/6 9:51        tanlin            new file.
// * <pre>
// */
//public class NettyServletWebServerFactory_Back extends AbstractServletWebServerFactory implements ResourceLoaderAware,
//        WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>, Ordered {
//    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServletWebServerFactory_Back.class);
//    private NettyEmbeddedProperties nettyEmbeddedProperties;
//
//    /**
//     * 资源加载器
//     */
//    private ResourceLoader resourceLoader;
//
//    /**
//     * 环境配置对象
//     */
//    public static Environment environment;
//
//    /**
//     * 服务端IP、端口、主机名
//     * */
//    public static InetSocketAddress serverAddress;
//
//    public static NettyServletContext servletContext;
//
//    /**
//     * WebServer 配置属性
//     */
//    private final ServerProperties serverProperties;
//
//    public NettyServletWebServerFactory_Back(Environment environment, ServerProperties serverProperties, NettyEmbeddedProperties nettyEmbeddedProperties) {
//        NettyServletWebServerFactory_Back.environment = environment;
//        this.serverProperties = serverProperties;
//        this.nettyEmbeddedProperties = nettyEmbeddedProperties;
//    }
//
//    @Override
//    public WebServer getWebServer(ServletContextInitializer... initializers) {
//        /**容器信息日志*/
//        logContainer();
//        /** Servlet 容器初始化工厂 上下文设置*/
//        onStartup(initializers);
//        /**从SpringBoot配置中获取端口，如果没有则随机生成*/
//        int port = getPort() > 0 ? getPort() : 8080;
//        serverAddress = new InetSocketAddress(port);
//        /**初始化容器并返回*/
//        return new NettyServletWebServer(serverAddress,nettyEmbeddedProperties);
//    }
//    private void logContainer() {
//        /**Netty启动环境相关信息*/
//        Package nettyPackage = Bootstrap.class.getPackage();
//        String title = nettyPackage.getImplementationTitle();
//        String version = nettyPackage.getImplementationVersion();
//        LOGGER.info("[Container] Netty环境：{} , {} ",title,version);
//    }
//    private void onStartup(ServletContextInitializer[] initializers) {
//        servletContext = new NettyServletContext(getContextPath(),resourceLoader,serverProperties);
//        for (ServletContextInitializer initializer : initializers) {
//            try {
//                initializer.onStartup(servletContext);
//            } catch (ServletException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//    @Override
//    public void setResourceLoader(ResourceLoader resourceLoader) {
//        this.resourceLoader = resourceLoader;
//    }
//
//    @Override
//    public void customize(ConfigurableServletWebServerFactory factory) {
//        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
//        map.from(this.serverProperties::getPort).to(factory::setPort);
//        map.from(this.serverProperties::getAddress).to(factory::setAddress);
//        map.from(this.serverProperties.getServlet()::getContextPath)
//                .to(factory::setContextPath);
//        map.from(this.serverProperties.getServlet()::getJsp).to(factory::setJsp);
//        map.from(this.serverProperties.getServlet()::getContextParameters)
//                .to(factory::setInitParameters);
//    }
//
//    @Override
//    public int getOrder() {
//        return 0;
//    }
//}

package com.centify.boot.web.embedded.netty.factory;

import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
import com.centify.boot.web.embedded.netty.constant.NettyConstant;
import com.centify.boot.web.embedded.netty.context.NettyServletContext;
import com.centify.boot.web.embedded.netty.core.NettyServletWebServer;
import io.netty.bootstrap.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import javax.servlet.ServletException;
import java.io.File;
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
        implements ConfigurableNettyServletWebServerFactory, ResourceLoaderAware,  BeanFactoryAware, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServletWebServerFactory.class);

    /**
     * 环境配置对象
     */
    private final Environment environment;
    /**
     * WebServer 配置属性
     */
    private final ServerProperties serverProperties;

    /**
     * Netty Web 属性对象
     */
    private final NettyEmbeddedProperties nettyProperties;

    /**
     * 资源加载器
     */
    private ResourceLoader resourceLoader;
    /**
     * Bean 工厂
     */
    private BeanFactory beanFactory;
    /**
     * 服务端IP、端口、主机名
     */
    public static InetSocketAddress serverAddress;

    /**
     * ServletContext对象
     */
    public static NettyServletContext servletContext;

    private ClassLoader classLoader;

    private File baseDirectory;

    public NettyServletWebServerFactory(
            Environment environment,
            ServerProperties serverProperties,
            NettyEmbeddedProperties nettyProperties) {

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
        prepareContext();
        /** Servlet 容器初始化工厂 上下文设置*/
        onStartup(initializers);
        /**从SpringBoot配置中获取端口，如果没有则随机生成*/
        int port = getPort() > 0 ? getPort() : 8080;
        serverAddress = new InetSocketAddress(port);
        /**初始化容器并返回*/

        return new NettyServletWebServer(serverAddress, nettyProperties);
    }

    private void prepareContext() {

        File baseDir = (this.baseDirectory != null) ? this.baseDirectory : createTempDir("nettyServlet");

        servletContext = new NettyServletContext(getContextPath(),baseDir.getAbsolutePath(), resourceLoader, serverProperties, serverAddress);

        MultipartProperties multipartProperties = beanFactory.getBean(MultipartProperties.class);

        File docBase = (getValidDocumentRoot() != null) ? getValidDocumentRoot() : createTempDir("nettyServlet-docbase");

        servletContext.setServerHeader(getServerHeader());
        servletContext.setServletContextName(getDisplayName());
        for (MimeMappings.Mapping mapping : getMimeMappings()) {
            servletContext.setMimeTypesElement(mapping.getExtension(),mapping);
        }
        String location = multipartProperties.getLocation();
        if(location != null && !location.isEmpty()){
            servletContext.setDocBase(location,"");
        }else {
            servletContext.setDocBase(docBase.getAbsolutePath());
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.classLoader = resourceLoader.getClassLoader();
    }

    /**
     * <pre>
     * <b>容器日志输出</b>
     * <b>Describe:输出OS、Netty Web环境信息</b>
     *
     * <b>Author: tanlin [2020/9/7 11:18]</b>
     *
     * @return void
     * <pre>
     */
    private void logContainer() {
        Package nettyPackage = Bootstrap.class.getPackage();

        LOGGER.info("{}", NettyConstant.OS_SYSTEM_INFO);
        LOGGER.info("{}", NettyConstant.JVM_INFO);
        LOGGER.info("[Container] Spring.profiles.active：{} ", (environment.getActiveProfiles() != null && environment.getActiveProfiles().length > 0) ? environment.getActiveProfiles() : "dev");
        LOGGER.info("[Container] Netty环境：{} , {} ",
                nettyPackage.getImplementationTitle(),
                nettyPackage.getImplementationVersion());
        LOGGER.info("[Container] Netty容器：{} ",nettyProperties.toString());
    }

    /**
     * <pre>
     * <b>初始化ServletContext</b>
     * <b>Describe:即 initializer.onStartup()</b>
     *
     * <b>Author: tanlin [2020/9/7 11:19]</b>
     *
     * @param initializers TODO
     * @return void
     * <pre>
     */
    private void onStartup(ServletContextInitializer[] initializers) {
        for (ServletContextInitializer initializer : initializers) {
            try {
                initializer.onStartup(servletContext);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

//    @Override
//    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//        if(bean instanceof AbstractServletWebServerFactory && ((AbstractServletWebServerFactory) bean).getPort() > 0){
//            try {
//                configurableServletContext((AbstractServletWebServerFactory) bean);
//            } catch (Exception e) {
//                BeanInitializationException exception = new BeanInitializationException(e.getMessage(),e);
//                exception.setStackTrace(e.getStackTrace());
//                throw exception;
//            }
//        }
//        return bean;
//    }

//    private void configurableServletContext(AbstractServletWebServerFactory bean) {
//        MultipartProperties multipartProperties = beanFactory.getBean(MultipartProperties.class);
//        servletContext.setContextPath(bean.getContextPath());
//        servletContext.setServerHeader(bean.getServerHeader());
//        servletContext.setServletContextName(bean.getDisplayName());
//        for (MimeMappings.Mapping mapping :bean.getMimeMappings()) {
//            servletContext.setMimeTypesElement(mapping.getExtension(),mapping);
//        }
//        String location = multipartProperties.getLocation();
//        if(location != null && !location.isEmpty()){
//            servletContext.setDocBase(location,"");
//        }else {
//            servletContext.setDocBase(bean.getDocumentRoot().getAbsolutePath());
//        }
//    }



    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void setBaseDirectory(File baseDirectory) {

    }
}

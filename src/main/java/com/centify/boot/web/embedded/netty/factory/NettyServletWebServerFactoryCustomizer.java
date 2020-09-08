package com.centify.boot.web.embedded.netty.factory;

import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/8/28 10:09]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/8/28 10:09        tanlin            new file.
 * <pre>
 */
public class NettyServletWebServerFactoryCustomizer
        implements WebServerFactoryCustomizer<ConfigurableNettyServletWebServerFactory>, Ordered {

    private final Environment environment;

    private final ServerProperties serverProperties;

    private final NettyEmbeddedProperties nettyProperties;

    private BeanFactory beanFactory;

    private ClassLoader classLoader;

    public NettyServletWebServerFactoryCustomizer(
            Environment environment,
            ServerProperties serverProperties,
            NettyEmbeddedProperties nettyProperties,
            ConfigurableBeanFactory factory,
            ClassLoader classLoader) {
        this.environment = environment;
        this.serverProperties = serverProperties;
        this.nettyProperties = nettyProperties;
        this.beanFactory = factory;
        this.classLoader = classLoader;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public void customize(ConfigurableNettyServletWebServerFactory factory) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(this.serverProperties::getPort).to(factory::setPort);
        map.from(this.serverProperties::getAddress).to(factory::setAddress);
        map.from(this.serverProperties.getServlet()::getContextPath).to(factory::setContextPath);
        map.from(this.serverProperties.getServlet()::getApplicationDisplayName).to(factory::setDisplayName);
        map.from(this.serverProperties.getServlet()::getSession).to(factory::setSession);
        map.from(this.serverProperties::getSsl).to(factory::setSsl);
        map.from(this.serverProperties.getServlet()::getJsp).to(factory::setJsp);
        map.from(this.serverProperties::getCompression).to(factory::setCompression);
        map.from(this.serverProperties::getHttp2).to(factory::setHttp2);
        map.from(this.serverProperties::getServerHeader).to(factory::setServerHeader);
        map.from(this.serverProperties.getServlet()::getContextParameters).to(factory::setInitParameters);
    }
}

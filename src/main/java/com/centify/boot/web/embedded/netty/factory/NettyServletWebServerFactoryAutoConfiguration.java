package com.centify.boot.web.embedded.netty.factory;

import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
import io.netty.bootstrap.Bootstrap;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import javax.servlet.ServletRequest;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/8/28 10:36]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/8/28 10:36        tanlin            new file.
 * <pre>
 */

@Configuration(proxyBeanMethods = false)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(ServletRequest.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties({ServerProperties.class, NettyEmbeddedProperties.class})
public class NettyServletWebServerFactoryAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({Bootstrap.class,ServerProperties.class, NettyEmbeddedProperties.class})
    @ConditionalOnMissingBean(value = NettyServletWebServerFactory.class, search = SearchStrategy.CURRENT)
    public static class EmbeddedWebNetty {

        @Bean
        public NettyServletWebServerFactory nettyProtocolServletWebServerFactory(
                Environment environment,
                ServerProperties serverProperties,
                NettyEmbeddedProperties nettyProperties) {

            NettyServletWebServerFactory factory =
                    new NettyServletWebServerFactory(environment,serverProperties,nettyProperties);

            return factory;
        }

    }

}

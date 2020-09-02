package com.centify.boot.web.embedded.netty.factory;

import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
import io.netty.bootstrap.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/8/28 10:00]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/8/28 10:00        tanlin            new file.
 * <pre>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@EnableConfigurationProperties({ServerProperties.class, NettyEmbeddedProperties.class})
public class EmbeddedNettyWebServerFactoryCustomizerAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({Bootstrap.class})
    public static class NettyServletWebServerFactoryCustomizerConfiguration {
        private static final Logger LOGGER = LoggerFactory.getLogger(NettyServletWebServerFactoryCustomizerConfiguration.class);
        @Bean
        public NettyServletWebServerFactoryCustomizer embeddedNettyFactory(Environment environment,
                                                                           ServerProperties serverProperties,
                                                                           NettyEmbeddedProperties nettyEmbeddedProperties) {
            LOGGER.info("[Container] 启用 Netty Servlet Web Server 容器 :{} ,netty ={}", environment, nettyEmbeddedProperties);
            return new NettyServletWebServerFactoryCustomizer(environment, serverProperties,nettyEmbeddedProperties);
        }
    }
}

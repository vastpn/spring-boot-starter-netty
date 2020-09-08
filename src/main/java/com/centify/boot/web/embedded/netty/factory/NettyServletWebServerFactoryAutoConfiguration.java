package com.centify.boot.web.embedded.netty.factory;

import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
import io.netty.bootstrap.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import javax.servlet.ServletRequest;

/**
 * <pre>
 * <b>NettyServlet WebServer 容器</b>
 * <b>Describe:
 * 1、Netty Servlet WebServer 工厂服务类
 * 2、SpringBoot 自动注入并获取web应用的容器</b>
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
@ConditionalOnClass(ServletRequest.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties({ServerProperties.class, NettyEmbeddedProperties.class})
@Import({ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class})
public class NettyServletWebServerFactoryAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({Bootstrap.class, ServerProperties.class, NettyEmbeddedProperties.class})
    @ConditionalOnMissingBean(value = NettyServletWebServerFactory.class, search = SearchStrategy.CURRENT)
    public static class EmbeddedWebNetty {
        private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedWebNetty.class);

        /**
         * <pre>
         * <b>创建并转交Spring 管理的Netty Servlet Web 工厂</b>
         * <b>Describe:TODO</b>
         *
         * <b>Author: tanlin [2020/9/7 11:12]</b>
         *
         * @param environment 环境变量
         * @param serverProperties servlet环境变量
         * @param nettyProperties netty环境变量
         * @return com.centify.boot.web.embedded.netty.factory.NettyServletWebServerFactory
         * <pre>
         */
        @Bean
        public NettyServletWebServerFactory nettyProtocolServletWebServerFactory(
                Environment environment,
                ServerProperties serverProperties,
                NettyEmbeddedProperties nettyProperties,
                ConfigurableBeanFactory factory) {
            LOGGER.info("[Container] NettyServletWeb自定义容器工厂配置");
            return new NettyServletWebServerFactory(
                    environment,
                    serverProperties,
                    nettyProperties);
            /**可处理 BeanPostProcessor 的行为*/

        }

    }

}

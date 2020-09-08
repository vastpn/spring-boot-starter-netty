package com.centify.boot.web.embedded.netty.factory;

import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
import io.netty.bootstrap.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

/**
 * <pre>
 * <b>Netty Web Server 容器工厂</b>
 * <b>Describe:提供Netty  Http Server 容器</b>
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

        /**
         * <pre>
         * <b>管理NettyWeb工厂对象</b>
         * <b>Describe:TODO</b>
         *
         * <b>Author: tanlin [2020/9/7 10:06]</b>
         *
         * @param environment 环境变量
         * @param serverProperties Servlet配置属性
         * @param nettyEmbeddedProperties Netty配置属性
         * @param factory Bean配置管理工厂（用于Bean初始化前、后执行的配置逻辑，主要执行BeanPostProcessor 接口方法）
         * @param resourceLoader 资源加载器
         * @return com.centify.boot.web.embedded.netty.factory.NettyServletWebServerFactoryCustomizer
         * <pre>
         */
        @Bean
        public NettyServletWebServerFactoryCustomizer embeddedNettyFactory(
                Environment environment,
                ServerProperties serverProperties,
                NettyEmbeddedProperties nettyEmbeddedProperties,
                ConfigurableBeanFactory factory,
                ResourceLoader resourceLoader) {
            LOGGER.info("[Container] NettyServletWeb自定义工厂配置");
            return new NettyServletWebServerFactoryCustomizer(
                    environment,
                    serverProperties,
                    nettyEmbeddedProperties,
                    factory,
                    resourceLoader.getClassLoader());


        }
    }
}

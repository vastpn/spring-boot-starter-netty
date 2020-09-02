//package com.centify.boot.web.embedded.netty.factory;
//
//import com.centify.boot.web.embedded.netty.config.NettyEmbeddedProperties;
//import io.netty.bootstrap.Bootstrap;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.autoconfigure.AutoConfigureOrder;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
//import org.springframework.boot.autoconfigure.condition.SearchStrategy;
//import org.springframework.boot.autoconfigure.web.ServerProperties;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.Ordered;
//import org.springframework.core.env.Environment;
//
///**
// * <pre>
// * <b>Netty Web Server 容器工厂</b>
// * <b>Describe:
// * 1、提供Netty  Http Server 容器</b>
// *
// * <b>Author: tanlin [2020/7/6 9:43]</b>
// * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
// * <b>Changelog:</b>
// *   Ver   Date                  Author           Detail
// *   ----------------------------------------------------------------------------
// *   1.0   2020/7/6 9:43        tanlin            new file.
// * <pre>
// */
//
//@Configuration
//@ConditionalOnWebApplication
//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
//@EnableConfigurationProperties({ServerProperties.class, NettyEmbeddedProperties.class})
//public class EmbeddedNettyServletWebServerFactoryCustomizerAutoConfiguration_Back {
//
//    @Configuration
//    @ConditionalOnClass({Bootstrap.class})
//    @ConditionalOnMissingBean(value = NettyServletWebServerFactory.class, search = SearchStrategy.CURRENT)
//    public static class NettyHttpWebServerFactoryCustomizerConfiguration {
//        private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttpWebServerFactoryCustomizerConfiguration.class);
//        @Bean
//        public NettyServletWebServerFactory embeddedNettyFactory(Environment environment,
//                                                                 ServerProperties serverProperties,
//                                                                 NettyEmbeddedProperties nettyEmbeddedProperties) {
//            LOGGER.info("[Container] 启用 Netty Servlet Web Server 容器 :{} ,netty ={}", environment, nettyEmbeddedProperties);
//            return new NettyServletWebServerFactory(environment, serverProperties,nettyEmbeddedProperties);
//        }
//    }
//}

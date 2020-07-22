package com.centify.boot.web.embedded.netty;

import com.centify.boot.web.embedded.netty.factory.EmbeddedNettyServletWebServerFactoryCustomizerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/7/21 17:41]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/21 17:41        tanlin            new file.
 * <pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@EnableConfigurationProperties(ServerProperties.class)
@Import({EmbeddedNettyServletWebServerFactoryCustomizerAutoConfiguration.class})
public @interface EnableNettyEmbedded {
}

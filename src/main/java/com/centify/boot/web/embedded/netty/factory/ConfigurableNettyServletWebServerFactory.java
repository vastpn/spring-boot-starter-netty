package com.centify.boot.web.embedded.netty.factory;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;

import java.io.File;

/**
 * <pre>
 * <b>Netty Servlet Web 工厂扩展组件</b>
 * <b>Describe:可自定义扩展</b>
 *
 * <b>Author: tanlin [2020/8/28 10:15]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/8/28 10:15        tanlin            new file.
 * <pre>
 */
public interface ConfigurableNettyServletWebServerFactory extends ConfigurableServletWebServerFactory, ConfigurableWebServerFactory, ServletWebServerFactory {
    /**
     * Set the Netty Servlet Web base directory. If not specified a temporary directory will be used.
     *
     * @param baseDirectory the tomcat base directory
     */
    void setBaseDirectory(File baseDirectory);
}

package com.centify.boot.web.embedded.netty.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.ServletRequest;

/**
 * <pre>
 * <b>Netty Web Config 对象创建并被Spring托管</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/9/8 15:59]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/9/8 15:59        tanlin            new file.
 * <pre>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ServletRequest.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class NettyWebConfig {

    /**
     * <pre>
     * <b>文件上传解析对象</b>
     * <b>Describe:
     * 由standardMultipartHttpServletRequest 强制转换为 DefaultMultipartHttpServletRequest 处理文件上传，
     * 否则会出现【Required request part 'file' is not present】
     *
     * 1、
     *         <dependency>
     *             <groupId>commons-fileupload</groupId>
     *             <artifactId>commons-fileupload</artifactId>
     *         </dependency>
     * 2、
     *     @PostMapping("/files")
     *     public Object mult(@RequestParam("files") MultipartFile[] files){
     *
     *     }
     *
     *     或者
     *     @PostMapping("/upload")
     *     public Object mult(@RequestParam("file") MultipartFile file){
     *
     *     }
     * </b>
     *
     * <b>Author: tanlin [2020/9/8 16:01]</b>
     *
     * @return org.springframework.web.multipart.MultipartResolver
     * <pre>
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }
}

package com.centify.boot.web.embedded.netty.config;

import com.centify.boot.web.embedded.netty.interceptor.HttpGlobInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * <pre>
 * <b>Web拦截器示例配置</b>
 * <b>Describe:
 * 1、Web项目参考此拦截器配置对象，实现拦截器注入到Spring上下文，完成拦截器功能</b>
 *
 * <b>Author: tanlin [2020/3/10 15:45]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/3/10 15:45        tanlin            new file.
 * <pre>
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
  @Bean
  public MultipartResolver multipartResolver() {
    return new CommonsMultipartResolver();
  }
  @Autowired
  private HttpGlobInterceptor httpGlobInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
//    registry.addInterceptor(httpGlobInterceptor).addPathPatterns("/111/**").excludePathPatterns("/emp/toLogin","/emp/login","/js/**","/css/**","/images/**");

  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    //TODO Header公共参数统一赋值到Body中，减少前端工作(头信息赋值到RequestBody、RequestParam中)
  }

}

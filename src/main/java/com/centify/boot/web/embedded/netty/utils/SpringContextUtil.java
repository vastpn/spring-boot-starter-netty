package com.centify.boot.web.embedded.netty.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <pre>
 * <b>Spring 容器对象工具类</b>
 * <b>Describe:获取Spring管理的Bean对象</b>
 *
 * <b>Author: tanlin [2020/5/25 16:56]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/5/25 16:56        tanlin            new file.
 * <pre>
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

	private static ApplicationContext application = null;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextUtil.application = applicationContext;
	}

	public static ApplicationContext getApplication() {
		return application;
	}

	/**
	 * <pre>
	 * <b>根据bean id获得bean实例</b>
	 * <b>Describe:TODO</b>
	 *
	 * <b>Author: tanlin [2020/5/25 16:57]</b>
	 *
	 * @param id bean id
	 * @return T bean实例
	 * <pre>
	 */
	public static <T> T getBean(String id) {

		return (T) application.getBean(id);
	}

	/**
	 * <pre>
	 * <b>根据类型获取bean实例</b>
	 * <b>Describe:TODO</b>
	 *
	 * <b>Author: tanlin [2020/5/25 16:57]</b>
	 *
	 * @param clazz bean 类型
	 * @return T bean实例
	 * <pre>
	 */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        return (T) application.getBean(clazz);
    }

	/**
	 * <pre>
	 * <b>根据类型获取bean实例</b>
	 * <b>Describe:TODO</b>
	 *
	 * <b>Author: tanlin [2020/5/25 16:57]</b>
	 *
	 * @param name bean 名称
	 * @param requiredType bean 类型
	 * @return T bean实例
	 * <pre>
	 */
	public static Object getBean(String name, Class requiredType) throws BeansException {
		return application.getBean(name, requiredType);
	}

	/**
	 * <pre>
	 * <b>判断是否存在指定名称的bean</b>
	 * <b>Describe:TODO</b>
	 *
	 * <b>Author: tanlin [2020/5/25 16:57]</b>
	 *
	 * @param name bean 名称
	 * @return boolean 是否存在
	 * <pre>
	 */
	public static boolean containsBean(String name) {
		return application.containsBean(name);
	}


	/**
	 * <pre>
	 * <b>判断bean是否是单例模式</b>
	 * <b>Describe:TODO</b>
	 *
	 * <b>Author: tanlin [2020/5/25 16:57]</b>
	 *
	 * @param name bean 名称
	 * @return boolean 是否单例
	 * <pre>
	 */
	public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		return application.isSingleton(name);
	}

	/**
	 * <pre>
	 * <b>判断bean的类型</b>
	 * <b>Describe:TODO</b>
	 *
	 * <b>Author: tanlin [2020/5/25 16:57]</b>
	 *
	 * @param name bean 名称
	 * @return Class Bean 类型
	 * <pre>
	 */
	public static Class getType(String name) throws NoSuchBeanDefinitionException {
		return application.getType(name);
	}

	/**
	 * <pre>
	 * <b>获取指定类型的Bean集合</b>
	 * <b>Describe:TODO</b>
	 *
	 * <b>Author: tanlin [2020/5/25 16:57]</b>
	 *
	 * @param t bean 类型
	 * @return bean实例集合
	 * <pre>
	 */
    public   static <T> Map<String, T> getBeansByType(Class<T> t) {
        Map<String, T> beans = application.getBeansOfType(t);
        return beans;
    }
}

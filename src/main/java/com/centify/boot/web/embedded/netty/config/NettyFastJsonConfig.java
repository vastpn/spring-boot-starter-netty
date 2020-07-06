package com.centify.boot.web.embedded.netty.config;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <pre>
 * <b>Http FastJson 全局配置</b>
 * <b>Describe:
 * 1、删除Jackson JSON转换
 * 2、增加FastJson JSON转换</b>
 *
 * <b>Author: tanlin [2020/5/25 16:35]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/5/25 16:35        tanlin            new file.
 * <pre>
 */
@Configuration
public class NettyFastJsonConfig implements WebMvcConfigurer {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        /**删除 Jackson 配置*/
        removeJacksonConfigurer(converters);
        /**定义一个转换消息的对象*/
        FastJsonHttpMessageConverter fastJsonConverter = new FastJsonHttpMessageConverter();

        /**添加fastjson的配置信息 比如 ：是否要格式化返回的json数据*/
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        SerializerFeature[] features = new SerializerFeature[]{
//                /**格式化输出*/
//                SerializerFeature.PrettyFormat,
                /**序列化输出字段，使用引号,默认为true*/
                SerializerFeature.QuoteFieldNames,
                /**空值是否输出,默认为false*/
                SerializerFeature.WriteMapNullValue,
                /**空值Enum 字符串输出*/
                SerializerFeature.WriteEnumUsingToString,
                /**list字段如果为null，输出为[]*/
                SerializerFeature.WriteNullListAsEmpty,
                /**数值字段如果为null，输出为0*/
                SerializerFeature.WriteNullNumberAsZero,
                /**Boolean字段如果为null，输出为false*/
                SerializerFeature.WriteNullBooleanAsFalse,
                /**字符类型字段如果为null，输出为""*/
                SerializerFeature.WriteNullStringAsEmpty,
                /**消除循环引用*/
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.WriteBigDecimalAsPlain

        };
        /**默认使用 ToStringSerializer 转换 数字类型*/
        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
        serializeConfig.put(BigDecimal.class, ToStringSerializer.instance);
        serializeConfig.put(BigInteger.class, ToStringSerializer.instance);
        serializeConfig.put(Long.class, ToStringSerializer.instance);
        serializeConfig.put(Long.TYPE, ToStringSerializer.instance);
        fastJsonConfig.setSerializeConfig(serializeConfig);

        fastJsonConfig.setSerializerFeatures(features);
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fastJsonConverter.setFastJsonConfig(fastJsonConfig);

        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastJsonConverter.setSupportedMediaTypes(supportedMediaTypes);


        converters.add(fastJsonConverter);
    }

    private void removeJacksonConfigurer(List<HttpMessageConverter<?>> converters) {
        Iterator<HttpMessageConverter<?>> iterator = converters.iterator();
        while(iterator.hasNext()){
            HttpMessageConverter<?> converter = iterator.next();
            if(converter instanceof MappingJackson2HttpMessageConverter){
                iterator.remove();
            }
        }
    }


}

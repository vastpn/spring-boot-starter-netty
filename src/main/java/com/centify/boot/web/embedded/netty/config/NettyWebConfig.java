package com.centify.boot.web.embedded.netty.config;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
@EnableConfigurationProperties({ServerProperties.class, NettyEmbeddedProperties.class})
public class NettyWebConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyWebConfig.class);

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
    public MultipartResolver multipartResolver(NettyEmbeddedProperties properties) {
        LOGGER.info("[Container] 文件上传解析Bean注入 CommonsMultipartResolver(MultipartResolver)");

        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        /**HttpObjectAggregator请求数据粘包最大字节，默认=256KB；(存在文件上传时，需提高最大粘包大小) */
        multipartResolver.setMaxUploadSize(properties.getRequestDataMaxcontentlength());
        /**默认UTF-8*/
        multipartResolver.setDefaultEncoding(properties.getUriEncoding().toString());

        return multipartResolver;
    }

    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters(List<HttpMessageConverter<?>> converters) {
        removeJacksonConfigurer(converters);
        // 1.定义一个converters转换消息的对象
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
//                SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.WriteBigDecimalAsPlain

        };
        /**默认使用 ToStringSerializer 转换 数字类型*/
        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
        serializeConfig.put(BigDecimal.class, ToStringSerializer.instance);
        serializeConfig.put(BigInteger.class, ToStringSerializer.instance);
        serializeConfig.put(Long.class, ToStringSerializer.instance);
        serializeConfig.put(Long.TYPE, ToStringSerializer.instance);
        serializeConfig.put(LocalDateTime.class, LocalDateTimeSerializer.instance);
        serializeConfig.put(LocalDate.class, LocalDateSerializer.instance);
        serializeConfig.put(LocalTime.class, LocalTimeSerializer.instance);
        serializeConfig.put(Date.class, DateSerializer.instance);


        fastJsonConfig.setSerializeConfig(serializeConfig);

        fastJsonConfig.setSerializerFeatures(features);
//        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fastJsonConverter.setFastJsonConfig(fastJsonConfig);

        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastJsonConverter.setSupportedMediaTypes(supportedMediaTypes);
        LOGGER.info("[Container] Http JSON 解析类型：FastJSON");
        // 5.返回HttpMessageConverters对象
        return new HttpMessageConverters(fastJsonConverter);
    }

    private void removeJacksonConfigurer(List<HttpMessageConverter<?>> converters) {
        Iterator<HttpMessageConverter<?>> iterator = converters.iterator();
        while (iterator.hasNext()) {
            HttpMessageConverter<?> converter = iterator.next();
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                iterator.remove();
            }
        }
    }
}

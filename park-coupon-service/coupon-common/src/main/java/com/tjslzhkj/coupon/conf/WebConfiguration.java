package com.tjslzhkj.coupon.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 自定义 HTTP 消息转换器
 * **
 * Yuezejian  Created in 2020/11/3 下午11:30
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    /**
     * 重写 configureMessageConverters 方法，
     * 使用 MappingJackson2HttpMessageConverter 转换器，不用 springboot 去进行选择
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.clear();
        //TODO: 将 Java 对象转换为 HTTP 输出流，springboot 底层依靠 HttpMessageConverter<?>
        //TODO:将 Java 实体类 转换为json,当有多个转换器时，他会根据情况选择最合适的转换器使用
        converters.add(new MappingJackson2HttpMessageConverter());

    }
}

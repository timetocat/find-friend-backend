package com.lyx.usercenter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Spring MVC Json 配置
 *
 * @author timecat
 * @create
 */
@JsonComponent
public class JsonConfig {

    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        // 创建一个ObjectMapper，使用给定的builder
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        // 创建一个SimpleModule，其中包含ToStringSerializer
        SimpleModule module = new SimpleModule();
        // 将ToStringSerializer注册为Long类型的序列化器
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        // 将module注册到ObjectMapper
        objectMapper.registerModule(module);
        // 返回ObjectMapper
        return objectMapper;
    }

}

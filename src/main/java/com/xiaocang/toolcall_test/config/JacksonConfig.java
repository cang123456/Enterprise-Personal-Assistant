package com.xiaocang.toolcall_test.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对应笔记里的 Jackson 配置：
 *   - FAIL_ON_UNKNOWN_PROPERTIES=false：模型爱多传字段，别炸
 *   - ACCEPT_SINGLE_VALUE_AS_ARRAY=true：单值当数组
 *   - ACCEPT_CASE_INSENSITIVE_ENUMS=true："NORMAL" 也能认
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer ollamaJacksonCustomizer() {
        return builder -> builder
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .featuresToEnable(
                        DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                        MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS
                );
    }
}

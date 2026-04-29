package com.luohuo.game.config;

import com.github.xiaoymin.knife4j.spring.configuration.Knife4jProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    @ConditionalOnMissingBean(Knife4jProperties.class)
    public Knife4jProperties knife4jProperties() {
        Knife4jProperties properties = new Knife4jProperties();
        properties.setEnable(true);
        return properties;
    }
}

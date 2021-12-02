package com.tmalllet.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// WebMvcConfigurerAdapter 已过时弃用
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 所有请求都允许跨域
        // Cross-Origin Resource Sharing
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }

    @Autowired
    LoginInterceptor loginInterceptor;
    @Autowired
    OtherInterceptor otherInterceptor;

    // @Bean
    // public OtherInterceptor getOtherInterceptor() {
    //     return new OtherInterceptor();
    // }
    //
    // @Bean
    // public LoginInterceptor getLoginInterceptor() {
    //     return new LoginInterceptor();
    // }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(otherInterceptor)
                .addPathPatterns("/**");
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**");
    }
}

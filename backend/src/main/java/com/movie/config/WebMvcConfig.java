package com.movie.config;

import com.movie.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/health",
                        "/api/user/register",
                        "/api/user/login",
                        "/api/movie/public/**",
                        "/api/ranking/**",
                        "/api/crew/**",
                        "/api/report/**",
                        "/api/review/movie/**",
                        "/api/payment/products",
                        "/api/payment/notify",
                        "/api/payment/return",
                        "/api/ai/search",
                        "/api/ai/recommend",
                        "/api/upload/avatar",
                        "/api/graph/**"
                );
    }
}

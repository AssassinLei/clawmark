package com.clawmark.api.config;

import com.clawmark.api.security.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Resource
    private UploadProperties uploadProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = normalizePublicPathPrefix(uploadProperties.getPublicPathPrefix());
        String location = Paths.get(uploadProperties.getLocalDir()).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(prefix + "/**").addResourceLocations(location);
    }

    private String normalizePublicPathPrefix(String prefix) {
        String value = StringUtils.hasText(prefix) ? prefix.trim() : "/uploads";
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}

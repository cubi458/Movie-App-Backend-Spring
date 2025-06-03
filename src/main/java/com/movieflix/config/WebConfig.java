package com.movieflix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${project.poster}")
    private String posterPath;

    @Value("${project.video}")
    private String videoPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configure poster files
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:" + posterPath + "/");

        // Configure video files
        registry.addResourceHandler("/video/**")
                .addResourceLocations("file:" + videoPath + "/");
    }
} 
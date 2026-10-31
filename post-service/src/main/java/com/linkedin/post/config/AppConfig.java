package com.linkedin.post.config;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer paginationCustomizer(
            @Value("${app.pagination.default-page}") int defaultPage,
            @Value("${app.pagination.default-size}") int defaultSize,
            @Value("${app.pagination.max-page-size}") int maxPageSize) {

        return resolver -> {
            resolver.setFallbackPageable(PageRequest.of(defaultPage, defaultSize,
                            Sort.by(Sort.Direction.DESC, "createdAt")));
            resolver.setMaxPageSize(maxPageSize);
        };
    }
}

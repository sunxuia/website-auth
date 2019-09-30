package net.sunxu.website.auth.service.config;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/font/**").addResourceLocations("classpath:/static/font/");
        registry.addResourceHandler("/image/**").addResourceLocations("classpath:/static/image/");
    }

    @Bean
    public FilterRegistrationBean forwardHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ForwardedHeaderFilter());
        registration.setName("ForwardedHeaderFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public LayoutDialect thymeleafLayoutDialect() {
        return new LayoutDialect();
    }

    @Bean
    public FilterRegistrationBean registerCorsFilter(Map<String, ServiceProperties> serverProperties) {
        var config = new CorsConfiguration();
        config.addAllowedMethod(HttpMethod.GET);
        config.setAllowCredentials(true);

        Set<String> allowed = new HashSet<>();
        for (ServiceProperties value : serverProperties.values()) {
            for (String redirectUrl : value.getRedirectUrls()) {
                var uri = URI.create(redirectUrl);
                String path = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort();
                if (allowed.add(path)) {
                    config.addAllowedOrigin(path);
                }
            }
        }

        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/auth/**", config);
        CorsFilter corsFilter = new CorsFilter(configurationSource);

        FilterRegistrationBean registration = new FilterRegistrationBean(corsFilter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.setName("cors filter");
        return registration;
    }
}

package net.sunxu.website.auth.service.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    @Qualifier("rest-template")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConfigurationProperties("website.services")
    @RefreshScope
    public Map<String, ServiceProperties> serverProperties() {
        return new HashMap<>();
    }

    @Bean
    public RequestCache requestCache() {
        return new HttpSessionRequestCache();
    }
}

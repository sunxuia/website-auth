package net.sunxu.website.auth.service.config;

import java.util.List;
import java.util.Map;
import net.sunxu.website.user.dto.SocialType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CompositeFilter;

@Configuration
public class SocialConfig {

    @Bean
    public CompositeFilter socialFilter() {
        var socialFilter = new CompositeFilter();
        socialFilter.setFilters(List.of(githubFilter(), gitlabFilter(), weiboFilter()));
        return socialFilter;
    }

    @Bean
    @ConfigurationProperties("website.social.github")
    public ClientResource github() {
        return new ClientResource();
    }

    @Bean
    @Qualifier("github-filter")
    public SocialLoginFilter githubFilter() {
        return new SocialLoginFilter(SocialType.GITHUB, github());
    }

    @Bean
    @ConfigurationProperties("website.social.gitlab")
    public ClientResource gitlab() {
        return new ClientResource();
    }

    @Bean
    @Qualifier("gitlab-filter")
    public SocialLoginFilter gitlabFilter() {
        return new SocialLoginFilter(SocialType.GITLAB, gitlab());
    }


    @Bean
    @ConfigurationProperties("website.social.weibo")
    public ClientResource weibo() {
        return new ClientResource();
    }

    @Bean
    @Qualifier("weibo-filter")
    public SocialLoginFilter weiboFilter() {
        return new SocialLoginFilter(SocialType.WEIBO, weibo()) {
            @Override
            protected Map<String, Object> getUserInformation(
                    String accessTokenValue, Map<String, Object> accessToken) {
                String url = resource.getUserInfoUri()
                        + "?access_token=" + accessTokenValue + "&uid=" + accessToken.get("uid");
                return restTemplate.getForObject(url, Map.class);
            }
        };
    }
}

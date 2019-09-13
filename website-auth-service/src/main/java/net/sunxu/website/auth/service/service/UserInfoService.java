package net.sunxu.website.auth.service.service;

import java.util.Map;
import net.sunxu.website.auth.service.bo.UserDetailsImpl;
import net.sunxu.website.auth.service.config.ClientResource;
import net.sunxu.website.user.dto.SocialType;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserInfoService extends UserDetailsService {

    UserDetailsImpl getOrCreateUserDetails(ClientResource clientResource, SocialType socialType,
            Map<String, Object> userInfo);

    void updateUserSocialAccount(Long userId, ClientResource clientResource, SocialType socialType,
            Map<String, Object> userInfo);

}

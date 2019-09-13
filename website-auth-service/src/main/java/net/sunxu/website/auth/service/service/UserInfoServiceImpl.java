package net.sunxu.website.auth.service.service;

import java.util.Map;
import net.sunxu.website.auth.service.bo.UserDetailsImpl;
import net.sunxu.website.auth.service.config.ClientResource;
import net.sunxu.website.user.dto.SocialAccountDTO;
import net.sunxu.website.user.dto.SocialType;
import net.sunxu.website.user.feignclient.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userFeignClient.getUserDetails(username);
        if (user == null) {
            throw new UsernameNotFoundException("账户信息" + username + " 没找到");
        }
        return new UserDetailsImpl(user);
    }

    @Override
    public UserDetailsImpl getOrCreateUserDetails(ClientResource clientResource, SocialType socialType,
            Map<String, Object> userInfo) {
        var socialAccount = extractSocialAccount(clientResource, socialType, userInfo);
        var user = userFeignClient.getUserDetailsBySocialId(socialType, socialAccount.getSocialId());
        if (user == null) {
            user = userFeignClient.createUserBySocialAccount(socialAccount);
        }
        return new UserDetailsImpl(user);
    }

    private SocialAccountDTO extractSocialAccount(ClientResource clientResource, SocialType socialType,
            Map<String, Object> userInfo) {
        var social = new SocialAccountDTO();
        social.setSocialType(socialType);
        social.setAvatarUrl(getMapValue(userInfo, "avatar_url", "profile_image_url"));
        String profileUrl = getMapValue(userInfo, "url", "web_url", "profile_url");
        social.setSocialName(getMapValue(userInfo, "login", "name"));
        social.setSocialId(getMapValue(userInfo, "id", "uid"));
        if (profileUrl != null && profileUrl.startsWith("/")) {
            profileUrl = clientResource.getUrlPrefix() + profileUrl;
        }
        social.setProfileUrl(profileUrl);
        return social;
    }

    private String getMapValue(Map<String, Object> map, String... keys) {
        for (var key : keys) {
            var value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    @Override
    public void updateUserSocialAccount(Long userId, ClientResource clientResource, SocialType socialType,
            Map<String, Object> userInfo) {
        var socialAccount = extractSocialAccount(clientResource, socialType, userInfo);
        userFeignClient.updateUserSocialAccount(userId, socialAccount);
    }
}

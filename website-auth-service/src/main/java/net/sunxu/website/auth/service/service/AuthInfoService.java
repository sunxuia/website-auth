package net.sunxu.website.auth.service.service;


import javax.servlet.http.HttpSession;
import net.sunxu.website.auth.dto.UserTokenDTO;

public interface AuthInfoService {

    boolean isServiceAvailable(String serviceName, String redirect);

    String generateAuthCode();

    UserTokenDTO createUserToken(String serviceName, String uuid);

    UserTokenDTO refreshToken(String serviceName, String refreshToken);

    void notifyLogout(HttpSession session);
}

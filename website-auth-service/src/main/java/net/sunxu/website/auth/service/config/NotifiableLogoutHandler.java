package net.sunxu.website.auth.service.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sunxu.website.auth.service.service.AuthInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class NotifiableLogoutHandler implements LogoutHandler {

    @Autowired
    private AuthInfoService authInfoService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        authInfoService.notifyLogout(request.getSession());
    }
}

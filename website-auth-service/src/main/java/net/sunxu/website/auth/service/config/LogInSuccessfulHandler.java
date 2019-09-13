package net.sunxu.website.auth.service.config;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;

@Component
public class LogInSuccessfulHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Value("${website.auth.session-timeout-seconds}")
    private Integer timeoutSeconds;

    @Autowired
    private RequestCache requestCache;

    @PostConstruct
    public void initial() {
        setDefaultTargetUrl("/info");
        setRequestCache(requestCache);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        request.getSession().setMaxInactiveInterval(timeoutSeconds);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

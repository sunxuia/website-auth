package net.sunxu.website.auth.service.config;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import net.sunxu.website.auth.service.service.AuthInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@WebListener
@Slf4j
public class NotifiableHttpSessionListener implements HttpSessionListener {

    @Autowired
    @Lazy
    private AuthInfoService authInfoService;

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        authInfoService.notifyLogout(se.getSession());
    }
}

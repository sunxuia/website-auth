package net.sunxu.website.auth.service.controller;

import feign.Param;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sunxu.website.auth.dto.UserTokenDTO;
import net.sunxu.website.auth.service.bo.UserDetailsImpl;
import net.sunxu.website.auth.service.service.AuthInfoService;
import net.sunxu.website.config.feignclient.exception.InvalidException;
import net.sunxu.website.config.feignclient.exception.ServiceException;
import net.sunxu.website.config.security.authentication.SecurityHelpUtils;
import net.sunxu.website.config.security.authentication.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/auth")
@Controller
public class AuthController {

    @Autowired
    private RequestCache requestCache;

    @Autowired
    private AuthInfoService authInfoService;

    @GetMapping("/code")
    public void authCode(
            @RequestParam("service") String serviceName,
            @RequestParam("redirect") String redirect,
            Authentication auth,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        InvalidException.assertTrue(authInfoService.isServiceAvailable(serviceName, redirect),
                "Wrong service name or redirect url.");
        boolean isRestful = Optional.ofNullable(request.getHeader("Accept"))
                .orElse("").contains("application/json")
                || "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        boolean isAuthenticated = auth != null && auth.getPrincipal() instanceof UserDetailsImpl;
        if (isAuthenticated) {
            String code = authInfoService.generateAuthCode();
            response.sendRedirect(redirect + "?code=" + code);
        } else if (isRestful) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            requestCache.saveRequest(request, response);
            response.sendRedirect("/login");
        }
    }

    @PreAuthorize("hasRole('GATEWAY')")
    @PostMapping("/token")
    @ResponseBody
    public UserTokenDTO getToken(@Param("code") String code) {
        return authInfoService.createUserToken(getServiceName(), code);
    }

    private String getServiceName() {
        return Optional.ofNullable(SecurityHelpUtils.getCurrentUser())
                .filter(UserPrincipal::isService)
                .map(UserPrincipal::getUserName)
                .orElseThrow(() -> ServiceException.newException(HttpStatus.FORBIDDEN, "Only service is allowed."));
    }

    @PreAuthorize("hasRole('GATEWAY')")
    @PostMapping("/refresh")
    @ResponseBody
    public UserTokenDTO refreshToken(@RequestBody String refreshToken) {
        return authInfoService.refreshToken(getServiceName(), refreshToken);
    }
}

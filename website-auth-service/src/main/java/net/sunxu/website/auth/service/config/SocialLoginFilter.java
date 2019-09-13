package net.sunxu.website.auth.service.config;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import net.sunxu.website.auth.service.bo.UserDetailsImpl;
import net.sunxu.website.auth.service.service.UserInfoService;
import net.sunxu.website.help.util.ObjectHelpUtils;
import net.sunxu.website.user.dto.SocialType;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * oauth2 客户端认证的filter, 只实现了authorization code 认证.
 */
public class SocialLoginFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger logger = LoggerFactory.getLogger(SocialLoginFilter.class);

    private static final RandomStringGenerator stateGenerator = new RandomStringGenerator.Builder()
            .withinRange(new char[]{'0', '9'}, new char[]{'a', 'z'}, new char[]{'A', 'Z'}).build();

    protected final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    protected final ClientResource resource;

    protected final SocialType socialType;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected UserInfoService userInfoService;

    @Getter
    @Setter
    private String anonymousUserName = "anonymousUser";

    @Getter
    @Setter
    private String errorUrl = "/login";

    @Getter
    @Setter
    private String stateSessionAttributeName = "oauth_state";

    @Getter
    @Setter
    private String stateExpireSessionAttributeName = "oauth_state:expire";

    @Getter
    @Setter
    private long stateExpire = 5 * 60 * 1000L;

    public SocialLoginFilter(SocialType socialType, ClientResource resource) {
        super(resource.getFilterUrl());
        this.socialType = socialType;
        this.resource = resource;
    }

    @Autowired
    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        if (resource.isEnabled()) {
            var paras = request.getParameterMap();
            try {
                if (paras.containsKey("error")) {
                    throw new RuntimeException(paras.get("error")[0]);
                } else if (paras.containsKey("code")) {
                    return handleForGettingAccessToken(request, response);
                } else {
                    handleForRedirectToAuthorization(request, response);
                }
            } catch (Exception err) {
                if (err instanceof HystrixRuntimeException) {
                    err = (Exception) err.getCause();
                }
                logger.error("error happened while processing authentication in {}", request.getRequestURI());
                handleForError(request, response, err.getMessage());
            }
        }
        return null;
    }

    protected void handleForError(HttpServletRequest request, HttpServletResponse response, String error)
            throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(errorUrl);
        builder.queryParam("error", error);
        String redirectUri = builder.build().encode().toUriString();
        redirectStrategy.sendRedirect(request, response, redirectUri);
    }

    protected void handleForRedirectToAuthorization(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(resource.getUserAuthorizationUri());
        builder.queryParam("response_type", "code");
        builder.queryParam("client_id", resource.getClientId());
        builder.queryParam("redirect_uri", resource.getRedirectLocalUri());

        String state = stateGenerator.generate(6);
        builder.queryParam("state", state);
        request.getSession().setAttribute(stateSessionAttributeName, state);
        request.getSession().setAttribute(stateExpireSessionAttributeName, System.currentTimeMillis() + stateExpire);

        String redirectUri = builder.build().encode().toUriString();
        redirectStrategy.sendRedirect(request, response, redirectUri);
    }

    protected Authentication handleForGettingAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String state = request.getParameter("state");
        Assert.notNull(state, "state missing");
        Long expire = (Long) request.getSession().getAttribute(stateExpireSessionAttributeName);
        Assert.isTrue(expire != null && expire > System.currentTimeMillis(), "state is expired");
        String preservedState = (String) request.getSession().getAttribute(stateSessionAttributeName);
        Assert.state(preservedState != null && preservedState.equals(state), "state not match");
        String code = request.getParameter("code");
        Assert.notNull(code, "code missing");

        request.getSession().removeAttribute(stateSessionAttributeName);
        request.getSession().removeAttribute(stateExpireSessionAttributeName);

        var accessToken = getAccessToken(code);
        var accessTokenValue = getAccessTokenValue(accessToken);
        var userInfo = getUserInformation(accessTokenValue, accessToken);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || anonymousUserName.equals(auth.getName())) {
            var userDetails = userInfoService.getOrCreateUserDetails(resource, socialType, userInfo);
            logger.info("{}({}) login success with {}.", userDetails.getUsername(), userDetails.getId(), socialType);
            return new UsernamePasswordAuthenticationToken(userDetails, accessTokenValue, userDetails.getAuthorities());
        } else {
            var userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            userInfoService.updateUserSocialAccount(userId, resource, socialType, userInfo);
            return null;
        }
    }

    protected Map<String, Object> getAccessToken(String code) {
        MultiValueMap<String, Object> paras = new LinkedMultiValueMap<>(5);
        paras.add("client_id", resource.getClientId());
        paras.add("client_secret", resource.getClientSecret());
        paras.add("code", code);
        paras.add("grant_type", "authorization_code");
        paras.add("redirect_uri", resource.getRedirectLocalUri());

        return restTemplate.postForObject(resource.getAccessTokenUri(), paras, Map.class);
    }

    protected String getAccessTokenValue(Map<String, Object> accessToken) {
        var res = (String) accessToken.get("access_token");
        if (res == null) {
            var error = (String) accessToken.get("error");
            throw new RuntimeException("Cannot acquire access token" + ObjectHelpUtils.nvl(error, ""));
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getUserInformation(String accessTokenValue, Map<String, Object> accessToken) {
        return restTemplate.getForObject(
                resource.getUserInfoUri() + "?access_token=" + accessTokenValue,
                Map.class);
    }
}

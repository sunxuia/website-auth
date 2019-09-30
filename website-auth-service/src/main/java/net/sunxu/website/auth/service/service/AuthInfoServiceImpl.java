package net.sunxu.website.auth.service.service;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

import io.jsonwebtoken.JwtParser;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import net.sunxu.website.app.feignclient.AppFeignClient;
import net.sunxu.website.auth.dto.UserTokenDTO;
import net.sunxu.website.auth.service.bo.UserDetailsImpl;
import net.sunxu.website.auth.service.config.ServiceProperties;
import net.sunxu.website.config.feignclient.exception.ServiceException;
import net.sunxu.website.help.dto.ResultDTO;
import net.sunxu.website.help.webutil.RequestHelpUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AuthInfoServiceImpl implements AuthInfoService {

    private static final String GATEWAY_SERVICES_SESSION_ATTR_NAME = "GATEWAY_SERVICES";

    private static final String AUTH_ID_ATTR_NAME = "AUTH_ID";

    private static final String LOCK_KEY_PREFIX = "LOCK:";

    @Autowired
    private AppFeignClient appService;

    @Autowired
    private Map<String, ServiceProperties> services;

    @Value("${website.auth.session-timeout-seconds}")
    private Integer sessionTimeoutSeconds;

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    @Qualifier("serviceJwtParser")
    private JwtParser parser;

    @Resource
    private SessionRepository sessionRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    @LoadBalanced
    private RestTemplate ribbonTemplate;

    @Override
    public boolean isServiceAvailable(String serviceName, String redirect) {
        var service = services.get(serviceName);
        if (service != null) {
            for (String redirectUrl : service.getRedirectUrls()) {
                if (redirect.startsWith(redirectUrl)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String generateAuthCode() {
        for (int i = 0; i < 10; i++) {
            String code = RandomStringUtils.randomAlphanumeric(20);
            var session = RequestHelpUtils.getRequest().getSession();
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(wrapAuthCode(code), session.getId(), Duration.ofMinutes(1));
            if (Boolean.TRUE.equals(success)) {
                return code;
            }
        }
        throw ServiceException.newException("无法生成code.");
    }

    private String wrapAuthCode(String code) {
        return "AUTH_CODE:" + code;
    }

    @Override
    public UserTokenDTO createUserToken(String serviceName, String code) {
        String sessionId = redisTemplate.opsForValue().get(wrapAuthCode(code));
        if (sessionId == null) {
            throw ServiceException.newException("code 过期.");
        }
        var session = sessionRepository.findById(sessionId);
        if (session == null) {
            throw ServiceException.newException("用户登录已过期");
        }
        var dto = createUserTokenDTO(session, serviceName);
        redisTemplate.delete(code);
        return dto;
    }

    private UserTokenDTO createUserTokenDTO(Session session, String serviceName) {
        var context = (SecurityContext) session.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
        var user = Optional.ofNullable(context)
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof UserDetailsImpl)
                .map(p -> ((UserDetailsImpl) p).getUser())
                .orElseThrow(() -> ServiceException.newException("User is not authenticated."));

        Map<String, Object> accessToken = new HashMap<>();
        accessToken.put("id", user.getId());
        accessToken.put("name", user.getName());
        accessToken.put("roles", user.getRoles());
        accessToken.put("tokenName", "accessToken");
        accessToken.put("expire", sessionTimeoutSeconds * 500L);
        accessToken.put("tokenHolder", serviceName);

        Map<String, Object> refreshToken = new HashMap<>();
        refreshToken.put("id", user.getId());
        refreshToken.put("name", user.getName());
        refreshToken.put("tokenName", "refreshToken");
        refreshToken.put("expire", sessionTimeoutSeconds * 1000L);
        refreshToken.put("tokenHolder", serviceName);
        String authId = getAuthId(session);
        refreshToken.put("authId", authId);

        var tokens = appService.getUserToken(List.of(accessToken, refreshToken));

        UserTokenDTO dto = new UserTokenDTO();
        dto.setToken(tokens.get("accessToken"));
        dto.setRefreshToken(tokens.get("refreshToken"));
        dto.setAuthId(authId);
        dto.setTokenExpire(System.currentTimeMillis() + sessionTimeoutSeconds * 500L - 60_000L);
        dto.setRefreshTokenExpire(System.currentTimeMillis() + sessionTimeoutSeconds * 1000L - 60_000L);

        var serviceNames = (Set<String>) session.getAttribute(GATEWAY_SERVICES_SESSION_ATTR_NAME);
        if (serviceNames == null) {
            serviceNames = new HashSet<>();
        }
        serviceNames.add(serviceName);
        session.setAttribute(GATEWAY_SERVICES_SESSION_ATTR_NAME, serviceNames);
        sessionRepository.save(session);

        log.info("Access {}({}) from {}, authId {}.", user.getName(), user.getId(), serviceName, authId);

        return dto;
    }

    private String getAuthId(Session session) {
        String id = session.getAttribute(AUTH_ID_ATTR_NAME);
        if (id == null) {
            id = session.getId();
            session.setAttribute(AUTH_ID_ATTR_NAME, id);
        }
        return id;
    }

    @Override
    public UserTokenDTO refreshToken(String serviceName, String refreshToken) {
        var claims = parser.parseClaimsJws(refreshToken).getBody();
        String authId = claims.get("authId", String.class);
        var session = sessionRepository.findById(authId);
        if (session == null) {
            throw ServiceException.newException("用户登录已过期");
        }
        return createUserTokenDTO(session, serviceName);
    }

    @Override
    public void notifyLogout(HttpSession httpSession) {
        var authId = (String) httpSession.getAttribute(AUTH_ID_ATTR_NAME);
        if (authId == null) {
            return;
        }
        var session = sessionRepository.findById(authId);
        if (session != null) {
            var serviceNames = (Set<String>) session.getAttribute(GATEWAY_SERVICES_SESSION_ATTR_NAME);
            if (serviceNames != null && serviceNames.size() > 0) {
                boolean occupied = redisTemplate.opsForValue()
                        .setIfAbsent(LOCK_KEY_PREFIX + authId, "", Duration.ofSeconds(10));
                if (occupied) {
                    for (String serviceName : serviceNames) {
                        try {
                            String uri = String.format("http://%s/auth/logout?authId=%s", serviceName, authId);
                            HttpHeaders headers = new HttpHeaders();
                            HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
                            var ret = ribbonTemplate.postForObject(uri, httpEntity, ResultDTO.class);
                            if (ret == null || !ret.isSuccess()) {
                                log.error("Cannot logout for {}, message: {}",
                                        serviceName, ret == null ? null : ret.getMessage());
                            }
                        } catch (Exception err) {
                            log.error("Cannot logout for {}, message: {}", serviceName, err.getMessage());
                        }
                    }
                }
            }
            httpSession.removeAttribute(AUTH_ID_ATTR_NAME);
            httpSession.removeAttribute(GATEWAY_SERVICES_SESSION_ATTR_NAME);
        }
    }
}

package net.sunxu.website.auth.feignclient;

import net.sunxu.website.auth.dto.UserTokenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("auth-service")
public interface AuthFeignClient {

    @RequestMapping(path = "/auth/token", method = RequestMethod.POST)
    UserTokenDTO postForToken(@RequestParam("code") String code);

    @RequestMapping(path = "/auth/refresh", method = RequestMethod.POST)
    UserTokenDTO postForRefreshToken(@RequestBody String refrehToken);
}

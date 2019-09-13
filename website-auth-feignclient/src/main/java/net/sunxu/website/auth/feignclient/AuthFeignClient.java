package net.sunxu.website.auth.feignclient;

import net.sunxu.website.auth.dto.UserTokenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("auth-service")
public interface AuthFeignClient {

    @RequestMapping(value = "/auth/token", method = RequestMethod.POST)
    UserTokenDTO postForToken(@RequestParam("code") String code);

}

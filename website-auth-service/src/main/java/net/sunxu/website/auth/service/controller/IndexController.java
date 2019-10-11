package net.sunxu.website.auth.service.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import net.sunxu.website.auth.service.bo.UserDetailsImpl;
import net.sunxu.website.auth.service.config.LogInSuccessfulHandler;
import net.sunxu.website.user.dto.UserCreationDTO;
import net.sunxu.website.user.dto.UserDTO;
import net.sunxu.website.user.dto.UserDetailsDTO;
import net.sunxu.website.user.feignclient.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private LogInSuccessfulHandler logInSuccessfulHandler;

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource
            = new WebAuthenticationDetailsSource();

    @GetMapping("/")
    @ResponseBody
    public void index(HttpServletResponse response) throws IOException {
        response.sendRedirect("/login");
    }

    @GetMapping("/login")
    public ModelAndView login(Authentication authentication,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) Boolean logout) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return new ModelAndView("redirect:/info");
        }
        var res = new ModelAndView("login");
        if (error != null) {
            if (error.isEmpty()) {
                error = "用户名或密码不对";
            }
            res.addObject("error", error);
        }
        res.addObject("logout", logout == null ? false : logout);
        return res;
    }

    @PostMapping("/login")
    public ModelAndView loginFailed(@RequestParam String error) {
        var res = new ModelAndView("login");
        res.addObject("error", error);
        return res;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public ModelAndView info() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        ModelAndView res = new ModelAndView("info");
        UserDTO user = ((UserDetailsImpl) auth.getPrincipal()).getUser();
        res.addObject("avatar", user.getAvatarUrl());
        res.addObject("userName", user.getName());
        return res;
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid UserCreationDTO dto, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        UserDetailsDTO detailsDTO;
        try {
            detailsDTO = userFeignClient.createUser(dto);
        } catch (Exception err) {
            var res = new ModelAndView("login");
            res.addObject("error", err.getMessage());
            res.addObject("register", true);
            res.addObject("creationInfo", dto);
            return res;
        }
        var details = new UserDetailsImpl(detailsDTO);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                details, dto.getPassword(), details.getAuthorities());
        auth.setDetails(authenticationDetailsSource.buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
        logInSuccessfulHandler.onAuthenticationSuccess(request, response, auth);
        return null;
    }
}

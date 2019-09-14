// TODO: mock redis session
//package net.sunxu.website.auth.service;
//
//import static java.util.stream.Collectors.toList;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import net.sunxu.website.auth.service.bo.UserDetailsImpl;
//import net.sunxu.website.auth.service.service.UserInfoService;
//import net.sunxu.website.test.helputil.assertion.AssertHelpUtils;
//import net.sunxu.website.user.dto.SocialType;
//import net.sunxu.website.user.dto.UserDetailsDTO;
//import net.sunxu.website.user.dto.UserState;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.modules.junit4.PowerMockRunnerDelegate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.mock.web.MockHttpSession;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.session.Session;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.client.RestTemplate;
//
//@PrepareForTest(SecurityContextHolder.class)
//@RunWith(PowerMockRunner.class)
//@PowerMockRunnerDelegate(SpringRunner.class)
//@PowerMockIgnore({"javax.management.*", "com.sun.org.apache.xerces.*", "javax.xml.*",
//        "org.xml.*", "org.w3c.dom.*", "com.sun.org.apache.xalan.*", "javax.activation.*"})
//@SpringBootTest
//@AutoConfigureMockMvc
//public class SocialLoginFilterTest {
//
//    @Autowired
//    protected MockMvc mockMvc;
//
//    @Autowired
//    protected ObjectMapper objectMapper;
//
//    @MockBean
//    @Qualifier("rest-template")
//    private RestTemplate mockRestTemplate;
//
//    @MockBean
//    private UserInfoService userInfoService;
//
//    @MockBean(name = "mockSession")
//    private Session mockSession;
//
//    @Test
//    public void requestLogin_redirectToLoginPage() throws Exception {
//        var session = new MockHttpSession();
//        var result = mockMvc.perform(get("/login/github").session(session))
//                .andDo(print())
//                .andExpect(status().is3xxRedirection())
//                .andReturn();
//
//        String location = result.getResponse().getHeader("Location");
//        String code = (String) session.getAttribute("oauth_state");
//
//        Assert.assertTrue(location.startsWith("https://github.com/login/oauth/authorize"));
//        String[] queires = location.substring(location.indexOf('?') + 1).split("&");
//        String[] expected = new String[]{
//                "response_type=code",
//                "client_id=4b7c4d8da7668e66b851",
//                "redirect_uri=http://127.0.0.1:8200/login/github",
//                "state=" + code
//        };
//        AssertHelpUtils.assertCollectionEquals(List.of(expected), List.of(queires));
//    }
//
//    @Test
//    public void testHandleError() throws Exception {
//        mockMvc.perform(get("/login/github?error=test error"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(header().string("Location", "/login?error=test%20error"));
//    }
//
//    @Test
//    public void testLogin() throws Exception {
//        var mockSession = new MockHttpSession();
//        mockSession.putValue("oauth_state", "<state>");
//        mockSession.putValue("oauth_state:expire", System.currentTimeMillis() + 100000);
//
//        Mockito.when(mockRestTemplate.postForObject(Mockito.eq(
//                "https://github.com/login/oauth/access_token"), Mockito.anyMap(), Mockito.eq(Map.class)))
//                .thenAnswer(i -> {
//                    String code = ((List<String>) ((Map<String, Object>) i.getArgument(1)).get("code")).get(0);
//                    if (!code.equals("123456")) {
//                        throw new RuntimeException("code not equal");
//                    }
//                    return Map.of("access_token", "<access-token>");
//                });
//
//        Map<String, Object> mockUserInfo = new HashMap<>();
//        Mockito.when(mockRestTemplate.getForObject(Mockito.eq(
//                "https://api.github.com/user?access_token=<access-token>"), Mockito.eq(Map.class)))
//                .thenReturn(mockUserInfo);
//
//        var mockUserDetails = Mockito.mock(UserDetailsImpl.class);
//        Mockito.when(mockUserDetails.getUsername()).thenReturn("test-user-name");
//        Mockito.when(userInfoService.getOrCreateUserDetails(Mockito.any(), Mockito.any(), Mockito.eq(mockUserInfo)))
//                .thenReturn(mockUserDetails);
//        var mockAuthorities = (Collection<? extends GrantedAuthority>) List.of("ROLE_TEST").stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(toList());
//        Mockito.when(mockUserDetails.getAuthorities()).thenAnswer(i -> mockAuthorities);
//
//        var mockAuthenticationContext = Mockito.mock(SecurityContext.class);
//        PowerMockito.spy(SecurityContextHolder.class);
//        PowerMockito.when(SecurityContextHolder.getContext()).thenReturn(mockAuthenticationContext);
//
//        mockMvc.perform(get("/login/github?code=123456&state=<state>").session(mockSession))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(header().string("Location", "/"))
//                .andDo(print());
//
//        var captor = ArgumentCaptor.forClass(Authentication.class);
//        Mockito.verify(mockAuthenticationContext).setAuthentication(captor.capture());
//        var auth = captor.getValue();
//        Assert.assertNotNull(auth);
//        Assert.assertEquals("test-user-name", auth.getName());
//        Assert.assertEquals(mockUserDetails, auth.getPrincipal());
//        Assert.assertEquals("<access-token>", auth.getCredentials());
//        Assert.assertEquals(mockAuthorities, auth.getAuthorities());
//    }
//
//    @Test
//    public void testBindSocialAccount() throws Exception {
//        var mockSession = new MockHttpSession();
//        mockSession.putValue("oauth_state", "<state>");
//        mockSession.putValue("oauth_state:expire", System.currentTimeMillis() + 100000);
//
//        Mockito.when(mockRestTemplate.postForObject(Mockito.eq(
//                "https://github.com/login/oauth/access_token"), Mockito.anyMap(), Mockito.eq(Map.class)))
//                .thenReturn(Map.of("access_token", "<access-token>"));
//
//        Map<String, Object> mockUserInfo = new HashMap<>();
//        Mockito.when(mockRestTemplate.getForObject(Mockito.eq(
//                "https://api.github.com/user?access_token=<access-token>"), Mockito.eq(Map.class)))
//                .thenReturn(mockUserInfo);
//
//        // 设置用户登录
//        UserDetailsDTO dto = new UserDetailsDTO();
//        dto.setId(100L);
//        dto.setName("test-name");
//        dto.setUserState(UserState.NORMAL);
//        dto.setRoles(List.of("ROLE_NORMAL"));
//        var userDetails = new UserDetailsImpl(dto);
//        var auth = new UsernamePasswordAuthenticationToken(userDetails,
//                "<access-token>", userDetails.getAuthorities());
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        mockMvc.perform(get("/login/github?code=123456&state=<state>")
//                .session(mockSession))
////                .andExpect(status().is3xxRedirection())
////                .andExpect(header().string("Location", "/"))
//                .andDo(print());
//
//        Mockito.verify(userInfoService)
//                .updateUserSocialAccount(Mockito.any(), Mockito.any(), Mockito.eq(SocialType.GITHUB), Mockito.any());
//    }
//
//}

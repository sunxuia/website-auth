package net.sunxu.website.auth.service;

import java.util.Base64;
import net.sunxu.website.app.dto.PublicKeyDTO;
import net.sunxu.website.app.feignclient.AppFeignClient;
import net.sunxu.website.test.helputil.authtoken.AuthTokenBuilder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockConfiguration {

    @MockBean
    private AppFeignClient appFeignClient;

    @Autowired
    public void injectPublicKey() throws Exception {
        var resource = AuthTokenBuilder.publicKey();
        byte[] buffer = resource.getInputStream().readAllBytes();
        var dto = new PublicKeyDTO();
        dto.setType(AuthTokenBuilder.publicKeyType());
        dto.setPublicKey(Base64.getEncoder().encodeToString(buffer));

        Mockito.when(appFeignClient.getPublicKey()).thenReturn(dto);
    }

    @Autowired
    public void injectServiceToken(@Value("${spring.application.name}") String applicationName) {
        var token = new AuthTokenBuilder().name(applicationName).id(100L).exipreSeconds(100L).service(true).build();
        Mockito.when(appFeignClient.getServiceToken()).thenReturn(token);
    }
}

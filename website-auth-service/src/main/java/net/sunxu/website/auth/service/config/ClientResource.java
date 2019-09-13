package net.sunxu.website.auth.service.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ClientResource {

    private String filterUrl;

    private String redirectLocalUri;

    private String clientId;

    private String clientSecret;

    private String userAuthorizationUri;

    private String accessTokenUri;

    private String userInfoUri;

    private String urlPrefix = "";

    private boolean enabled = true;
}

package net.sunxu.website.auth.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserTokenDTO implements Serializable {

    private static final long serialVersionUID = -1L;

    private String token;

    private Long tokenExpire;

    private String refreshToken;

    private Long refreshTokenExpire;

    private String authId;

}

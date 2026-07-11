package com.clawmark.api.interfaces.vo.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthLoginVO {

    private String accessToken;
    private Long expiresIn;
    private AuthLoginUserVO user;
}

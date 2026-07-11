package com.clawmark.api.interfaces.vo.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthLoginUserVO {

    private String userId;
    private String openid;
    private String nickname;
    private String avatarUrl;
    private String coupleId;
    private Boolean isNewUser;
}

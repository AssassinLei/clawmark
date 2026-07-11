package com.clawmark.api.interfaces.vo.couples;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BindPartnerVO {

    private String userId;
    private String nickname;
    private String avatarUrl;
}

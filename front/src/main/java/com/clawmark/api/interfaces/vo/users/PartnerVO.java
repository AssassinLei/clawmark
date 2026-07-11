package com.clawmark.api.interfaces.vo.users;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PartnerVO {

    private String userId;
    private String nickname;
    private String avatarUrl;
}

package com.clawmark.api.interfaces.vo.users;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserMeVO {

    private String userId;
    private String nickname;
    private String avatarUrl;
    private String coupleId;
    private PartnerVO partner;
    private Integer togetherDays;
    private String boundAt;
}

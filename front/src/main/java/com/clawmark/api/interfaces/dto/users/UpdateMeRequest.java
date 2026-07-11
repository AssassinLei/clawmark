package com.clawmark.api.interfaces.dto.users;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateMeRequest {

    @Size(min = 1, max = 20, message = "nickname长度应为1-20")
    private String nickname;

    @Size(max = 500, message = "avatar_url长度不能超过500")
    private String avatarUrl;
}

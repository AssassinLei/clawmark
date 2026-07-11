package com.clawmark.api.interfaces.dto.couples;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BindCoupleRequest {

    @NotBlank(message = "invite_code不能为空")
    private String inviteCode;
}

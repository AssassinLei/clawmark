package com.clawmark.api.interfaces.vo.upload;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UploadStsTokenVO {

    private String accessKeyId;

    private String accessKeySecret;

    private String securityToken;

    private String expiration;
}

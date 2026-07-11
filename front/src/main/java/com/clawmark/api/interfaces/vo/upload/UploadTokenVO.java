package com.clawmark.api.interfaces.vo.upload;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UploadTokenVO {

    private String uploadUrl;

    private String uploadKey;

    private UploadStsTokenVO stsToken;

    private String cdnUrl;
}

package com.clawmark.api.interfaces.vo.upload;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UploadFileVO {

    private String fileUrl;

    private String imageType;

    private Long fileSize;
}

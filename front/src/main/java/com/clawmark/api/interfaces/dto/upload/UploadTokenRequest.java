package com.clawmark.api.interfaces.dto.upload;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UploadTokenRequest {

    @NotBlank(message = "file_type不能为空")
    private String fileType;

    @NotNull(message = "file_size不能为空")
    @Min(value = 1, message = "file_size必须大于0")
    private Long fileSize;

    @NotBlank(message = "image_type不能为空")
    private String imageType;
}

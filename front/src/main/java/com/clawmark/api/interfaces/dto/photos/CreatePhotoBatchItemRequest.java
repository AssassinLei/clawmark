package com.clawmark.api.interfaces.dto.photos;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreatePhotoBatchItemRequest {

    @NotBlank(message = "original_url不能为空")
    @Size(max = 700, message = "original_url长度不能超过700")
    private String originalUrl;

    @NotBlank(message = "thumbnail_url不能为空")
    @Size(max = 700, message = "thumbnail_url长度不能超过700")
    private String thumbnailUrl;

    @NotBlank(message = "shot_date不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "shot_date格式必须为YYYY-MM-DD")
    private String shotDate;

    private Integer width;

    private Integer height;
}

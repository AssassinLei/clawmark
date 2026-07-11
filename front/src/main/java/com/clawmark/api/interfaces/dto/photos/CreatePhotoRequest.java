package com.clawmark.api.interfaces.dto.photos;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreatePhotoRequest {

    @NotBlank(message = "original_url不能为空")
    @Size(max = 700, message = "original_url长度不能超过700")
    private String originalUrl;

    @NotBlank(message = "thumbnail_url不能为空")
    @Size(max = 700, message = "thumbnail_url长度不能超过700")
    private String thumbnailUrl;

    @Size(max = 20, message = "city_code长度不能超过20")
    private String cityCode;

    @Size(max = 100, message = "city_name长度不能超过100")
    private String cityName;


    @NotBlank(message = "shot_date不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "shot_date格式必须为YYYY-MM-DD")
    private String shotDate;

    private List<String> albumIds;

    private Integer width;

    private Integer height;
}

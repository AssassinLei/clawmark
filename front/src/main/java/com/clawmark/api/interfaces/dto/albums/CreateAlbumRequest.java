package com.clawmark.api.interfaces.dto.albums;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateAlbumRequest {

    @NotBlank(message = "title不能为空")
    @Size(min = 1, max = 30, message = "title长度应为1-30")
    private String title;

    @Size(max = 200, message = "description长度不能超过200")
    private String description;

    @Size(max = 700, message = "cover_photo_url长度不能超过700")
    private String coverPhotoUrl;

    @NotBlank(message = "city_name不能为空")
    @Size(max = 100, message = "city_name长度不能超过100")
    private String cityName;
}

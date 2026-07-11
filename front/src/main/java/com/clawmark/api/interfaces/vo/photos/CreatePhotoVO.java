package com.clawmark.api.interfaces.vo.photos;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreatePhotoVO {

    private String photoId;

    private String originalUrl;

    private String thumbnailUrl;

    private String cityCode;

    private String cityName;

    private String shotDate;

    private String uploaderId;

    private String createdAt;
}

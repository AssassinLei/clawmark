package com.clawmark.api.interfaces.vo.albums;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AlbumItemVO {

    private String albumId;

    private String type;

    private String title;

    private String cityCode;

    private String cityName;

    private String coverThumbnail;

    private Integer photoCount;

    private String createdAt;
}

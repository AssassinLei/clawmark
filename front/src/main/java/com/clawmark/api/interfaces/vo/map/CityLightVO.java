package com.clawmark.api.interfaces.vo.map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CityLightVO {

    private String cityCode;

    private String cityName;

    private String province;

    private Integer photoCount;

    private String coverThumbnail;

    private String latestDate;
}

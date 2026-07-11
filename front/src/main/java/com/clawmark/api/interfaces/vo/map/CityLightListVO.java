package com.clawmark.api.interfaces.vo.map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CityLightListVO {

    private List<CityLightVO> cities;

    private Integer totalCities;
}

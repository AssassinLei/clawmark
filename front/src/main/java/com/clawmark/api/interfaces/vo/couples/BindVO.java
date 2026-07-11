package com.clawmark.api.interfaces.vo.couples;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BindVO {

    private String coupleId;
    private BindPartnerVO partner;
    private String boundAt;
    private Integer togetherDays;
}

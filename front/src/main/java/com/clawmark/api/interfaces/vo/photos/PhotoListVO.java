package com.clawmark.api.interfaces.vo.photos;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PhotoListVO {

    private List<PhotoItemVO> photos;

    private String nextCursor;

    private Boolean hasMore;
}

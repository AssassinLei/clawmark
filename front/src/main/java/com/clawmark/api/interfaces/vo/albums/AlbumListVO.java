package com.clawmark.api.interfaces.vo.albums;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AlbumListVO {

    private List<AlbumItemVO> albums;

    private Long total;

    private Integer page;

    private Integer pageSize;
}

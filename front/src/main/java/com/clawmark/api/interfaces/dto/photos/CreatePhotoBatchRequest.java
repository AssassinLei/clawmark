package com.clawmark.api.interfaces.dto.photos;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreatePhotoBatchRequest {

    @NotEmpty(message = "album_ids不能为空")
    private List<String> albumIds;

    @Valid
    @NotEmpty(message = "photos不能为空")
    @Size(max = 20, message = "photos最多20张")
    private List<CreatePhotoBatchItemRequest> photos;
}

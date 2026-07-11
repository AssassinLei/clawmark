package com.clawmark.api.interfaces.vo.todos;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TodoUrgeVO {

    private String todoId;

    private String nextUrgeAvailableAt;
}

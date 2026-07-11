package com.clawmark.api.interfaces.vo.todos;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TodoCreateVO {

    private String todoId;

    private String title;

    private String type;

    private String status;

    private String deadline;

    private String createdBy;

    private String createdAt;
}

package com.clawmark.api.interfaces.vo.todos;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TodoItemVO {

    private String todoId;

    private String title;

    private String description;

    private String type;

    private String status;

    private String deadline;

    private Boolean isUrging;

    private String createdBy;

    private String assigneeId;

    private String completedBy;

    private String completedAt;

    private String createdAt;
}

package com.clawmark.api.interfaces.dto.todos;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateTodoRequest {

    @NotBlank(message = "title不能为空")
    @Size(min = 1, max = 50, message = "title长度应为1-50")
    private String title;

    @Size(max = 500, message = "description长度不能超过500")
    private String description;

    @NotBlank(message = "type不能为空")
    @Pattern(regexp = "^(mine|partner|shared)$", message = "type仅支持mine|partner|shared")
    private String type;

    private String deadline;
}

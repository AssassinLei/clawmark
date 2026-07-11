package com.clawmark.api.interfaces.vo.comments;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommentCreateVO {

    private String commentId;

    private String content;

    private String authorId;

    private String createdAt;
}

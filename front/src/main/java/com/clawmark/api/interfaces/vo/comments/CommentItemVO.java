package com.clawmark.api.interfaces.vo.comments;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommentItemVO {

    private String commentId;

    private String content;

    private CommentAuthorVO author;

    private String createdAt;
}

package com.clawmark.api.interfaces.vo.comments;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommentAuthorVO {

    private String userId;

    private String nickname;

    private String avatarUrl;
}

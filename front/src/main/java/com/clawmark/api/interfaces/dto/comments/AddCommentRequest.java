package com.clawmark.api.interfaces.dto.comments;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AddCommentRequest {

    @NotBlank(message = "content不能为空")
    @Size(min = 1, max = 200, message = "content长度应为1-200")
    private String content;
}

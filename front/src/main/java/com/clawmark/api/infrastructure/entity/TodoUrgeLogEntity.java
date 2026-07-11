package com.clawmark.api.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("todo_urge_logs")
public class TodoUrgeLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String todoId;

    private String senderUserId;

    private String receiverUserId;

    private LocalDateTime createdAt;
}

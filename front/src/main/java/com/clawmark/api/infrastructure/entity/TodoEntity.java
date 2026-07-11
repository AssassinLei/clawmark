package com.clawmark.api.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("todos")
public class TodoEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    private String coupleId;

    private String title;

    private String description;

    private String type;

    private String status;

    private LocalDateTime deadline;

    private Integer isUrging;

    private LocalDateTime lastUrgedAt;

    private LocalDateTime nextUrgeAvailableAt;

    private String createdBy;

    private String assigneeId;

    private String completedBy;

    private LocalDateTime completedAt;

    private Integer isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}

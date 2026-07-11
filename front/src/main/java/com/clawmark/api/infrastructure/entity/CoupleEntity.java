package com.clawmark.api.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("couples")
public class CoupleEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    private String inviterUserId;

    private String inviteeUserId;

    private String dataOwnerUserId;

    private String status;

    private LocalDateTime boundAt;

    private LocalDateTime unboundAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

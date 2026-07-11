package com.clawmark.api.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("couple_invites")
public class CoupleInviteEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    private String inviteCode;

    private String inviterUserId;

    private String usedByUserId;

    private String coupleId;

    private String status;

    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    private LocalDateTime createdAt;
}

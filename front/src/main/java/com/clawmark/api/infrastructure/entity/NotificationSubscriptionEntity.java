package com.clawmark.api.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification_subscriptions")
public class NotificationSubscriptionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    private String templateId;

    private String status;

    private LocalDateTime subscribedAt;

    private LocalDateTime unsubscribedAt;
}

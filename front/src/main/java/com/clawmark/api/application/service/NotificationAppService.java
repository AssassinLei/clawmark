package com.clawmark.api.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawmark.api.common.constant.BizCode;
import com.clawmark.api.common.exception.BizException;
import com.clawmark.api.infrastructure.entity.NotificationSubscriptionEntity;
import com.clawmark.api.infrastructure.mapper.NotificationSubscriptionMapper;
import com.clawmark.api.interfaces.dto.notifications.SubscribeRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class NotificationAppService {

    @Resource
    private NotificationSubscriptionMapper notificationSubscriptionMapper;

    @Transactional(rollbackFor = Exception.class)
    public void subscribe(String userId, SubscribeRequest request) {
        if (request.getTemplateIds() == null || request.getTemplateIds().isEmpty()) {
            throw new BizException(BizCode.BAD_REQUEST, "template_ids不能为空");
        }

        for (String templateId : request.getTemplateIds()) {
            if (!StringUtils.hasText(templateId)) {
                continue;
            }
            String tid = templateId.trim();

            NotificationSubscriptionEntity existing = notificationSubscriptionMapper.selectOne(
                    new LambdaQueryWrapper<NotificationSubscriptionEntity>()
                            .eq(NotificationSubscriptionEntity::getUserId, userId)
                            .eq(NotificationSubscriptionEntity::getTemplateId, tid)
                            .last("limit 1")
            );

            if (existing == null) {
                NotificationSubscriptionEntity row = new NotificationSubscriptionEntity();
                row.setUserId(userId);
                row.setTemplateId(tid);
                row.setStatus("subscribed");
                row.setSubscribedAt(LocalDateTime.now());
                notificationSubscriptionMapper.insert(row);
            } else {
                existing.setStatus("subscribed");
                existing.setSubscribedAt(LocalDateTime.now());
                existing.setUnsubscribedAt(null);
                notificationSubscriptionMapper.updateById(existing);
            }
        }
    }
}

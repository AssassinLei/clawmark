package com.clawmark.api.interfaces.rest;

import com.clawmark.api.application.service.NotificationAppService;
import com.clawmark.api.common.api.ApiResponse;
import com.clawmark.api.common.context.UserContext;
import com.clawmark.api.interfaces.dto.notifications.SubscribeRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationsController {

    @Resource
    private NotificationAppService notificationAppService;

    @PostMapping("/subscribe")
    public ApiResponse<Void> subscribe(@Valid @RequestBody SubscribeRequest request) {
        notificationAppService.subscribe(UserContext.getUserId(), request);
        return ApiResponse.successMessage("订阅记录已保存");
    }
}

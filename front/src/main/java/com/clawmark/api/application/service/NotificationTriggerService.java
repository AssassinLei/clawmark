package com.clawmark.api.application.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationTriggerService {

    public void onPartnerTodoCreated(String senderUserId, String receiverUserId, String todoTitle) {
    }

    public void onSharedTodoCreated(String senderUserId, String receiverUserId, String todoTitle) {
    }

    public void onTodoCompleted(String completerUserId, String creatorUserId, String todoTitle) {
    }

    public void onTodoUrged(String senderUserId, String receiverUserId, String todoTitle) {
    }
}

package com.bwc.approval_workflow_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bwc.approval_workflow_service.config.FeignConfig;
import com.bwc.approval_workflow_service.dto.NotificationRequestDTO;

@FeignClient(name = "notification-service", url = "${services.notification.url:http://localhost:8090}", configuration = FeignConfig.class)
public interface NotificationServiceClient {
    @PostMapping("/api/notifications")
    void sendNotification(@RequestBody NotificationRequestDTO notificationRequest);
}

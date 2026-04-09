package com.example.accountservices.feignconfig;

import com.example.accountservices.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("NOTIFICATION-SERVICE")
public interface NotificationServiceClient {

    @PostMapping("/notifications/send")
    void sendNotification(@RequestBody NotificationDTO notificationDTO);
}

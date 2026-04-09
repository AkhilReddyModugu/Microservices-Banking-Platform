package com.example.coustomerservices.feignconfig;

import com.example.coustomerservices.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationServiceClient {

    @PostMapping("/notifications/send")
    String sendNotification(@RequestBody NotificationDTO notificationDTO);
}

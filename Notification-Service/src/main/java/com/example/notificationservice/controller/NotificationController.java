package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationDTO;
import com.example.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    //notification send
    @PostMapping("notifications/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationDTO notificationDTO) {

        // Simulating notification sending logic
        try {
            String message=notificationService.sendNotification(notificationDTO);
            System.out.println("Sending notification to: " + notificationDTO.getReceiver());
            return ResponseEntity.ok(message);
        }

        // Log the exceptionm
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send notification");
        }
    }
}

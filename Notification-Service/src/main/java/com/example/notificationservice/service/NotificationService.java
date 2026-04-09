package com.example.notificationservice.service;

import com.example.notificationservice.Repo.NotificationRepository;
import com.example.notificationservice.dto.NotificationDTO;
import com.example.notificationservice.entity.Notification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private NotificationRepository notificationRepository;

    //format checking
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    //valid email checking
    private boolean isValidEmailFormat(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @RabbitListener(queues = {"${account.queue.json.name}"})
    public void handleNotification(NotificationDTO notificationDTO) {

        String message=sendNotification(notificationDTO);
        System.out.println("Received notification message: " + message);
    }

    //notification sending
    public String sendNotification(NotificationDTO notificationDTO) {
        if (!isValidEmailFormat(notificationDTO.getReceiver())) {
            return "Invalid email format please check the email.";
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(notificationDTO.getReceiver());
        message.setSubject(notificationDTO.getSubject());
        message.setText(notificationDTO.getBody());
        mailSender.send(message);
        saveNotification(notificationDTO);
        return "notification has been sent successfully.";
    }

    //save notification
    private void saveNotification(NotificationDTO notificationDTO) {
        Notification notification = new Notification();
        notification.setReceiver(notificationDTO.getReceiver());
        notification.setSubject(notificationDTO.getSubject());
        notification.setBody(notificationDTO.getBody());
        notificationRepository.save(notification);
    }
}

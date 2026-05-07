package com.example.notificationservice.service;

import com.example.notificationservice.Repo.NotificationRepository;
import com.example.notificationservice.dto.NotificationDTO;
import com.example.notificationservice.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

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
        log.info("Notification received from queue for: {}", notificationDTO.getReceiver());
        String message = sendNotification(notificationDTO);
        log.info("Notification result: {}", message);
    }

    //notification sending
    public String sendNotification(NotificationDTO notificationDTO) {
        if (!isValidEmailFormat(notificationDTO.getReceiver())) {
            log.warn("Invalid email format — skipping notification: {}", notificationDTO.getReceiver());
            return "Invalid email format please check the email.";
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(notificationDTO.getReceiver());
        message.setSubject(notificationDTO.getSubject());
        message.setText(notificationDTO.getBody());
        mailSender.send(message);
        log.info("Email sent to: {}, subject: {}", notificationDTO.getReceiver(), notificationDTO.getSubject());
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

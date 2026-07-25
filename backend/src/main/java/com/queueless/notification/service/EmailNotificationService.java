package com.queueless.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    public void sendEmail(String recipientEmail, String subject, String body) {
        log.info("Sending Email to {}: [{}] {}", recipientEmail, subject, body);
    }
}

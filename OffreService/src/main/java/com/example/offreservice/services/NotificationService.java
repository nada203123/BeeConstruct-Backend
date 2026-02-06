package com.example.offreservice.services;

import com.example.offreservice.model.Offre;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendStaleOffreNotification(Offre offre) {
        String message = String.format("Offre '%s' hasn't been updated in over one month", offre.getTitre());
        messagingTemplate.convertAndSend("/topic/notifications", message);
        System.out.println(offre.getTitre());


    }


}

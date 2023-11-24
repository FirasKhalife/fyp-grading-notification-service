package com.fypgrading.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fypgrading.notificationservice.service.dto.NotificationDTO;
import com.fypgrading.notificationservice.service.event.GradeFinalizedEvent;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Service;

@Service
public class EventHandler implements ChannelAwareMessageListener {

    private final Logger logger = LoggerFactory.getLogger(EventHandler.class);
    private final NotificationService notificationService;
    private final RabbitService rabbitService;
    private final ObjectMapper objectMapper;

    public EventHandler(NotificationService notificationService,
                        RabbitService rabbitService,
                        ObjectMapper objectMapper
    ) {
        this.notificationService = notificationService;
        this.rabbitService = rabbitService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, Channel channel) {
        try {
            logger.info("Received message: " + message);

            GradeFinalizedEvent event =
                    objectMapper.readValue(
                            new String(message.getBody()),
                            GradeFinalizedEvent.class
                    );

            notificationService.saveNotification(new NotificationDTO(event));

        } catch (Exception ex) {
            rabbitService.sendNack(message, channel);
        }
    }
}


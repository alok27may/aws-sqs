package com.kumaral.awssqs;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AppConfiguration {
    private final MessageQueueService messageQueueService;

    @PostConstruct
    public void initializeMessageQueue() {
        messageQueueService.createMessageQueue();
    }
}

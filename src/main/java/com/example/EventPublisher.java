package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPublisher {

    private final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    void publish(CdcEvent event) {
        logger.info("{}", event);
    }
}

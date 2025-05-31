package com.demo.mmi.util;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.demo.openapi.event.WebSocketEvent;

import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebSocketListener {

    @EventListener
    public void handleWebSocketMessage(WebSocketEvent event) {
        String message = event.getMessage();

        // Update UI on JavaFX thread
        Platform.runLater(() -> {
            log.info("Received message in JavaFX: " + message);
            // TODO: update your UI
        });
    }
}

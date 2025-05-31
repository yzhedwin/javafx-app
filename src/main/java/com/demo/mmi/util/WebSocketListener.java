package com.demo.mmi.util;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.demo.openapi.event.WebSocketEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketListener {

    // @Autowired
    // @Qualifier("ganttChartModelEventProperty")
    // private ObjectProperty<GanttChartModelEvent> ganttChartModelEventProperty;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @EventListener
    public void handleWebSocketMessage(WebSocketEvent event) {
        try {
            GanttChartModelEvent ganttChartModelEvent = mapper.readValue(event.getMessage(),
                    GanttChartModelEvent.class);
            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                log.info("Received message in JavaFX: " + ganttChartModelEvent.toString());
                // modelEventProperty.set(new GanttChartModelEvent(EModelEvent.CHANGE,
                // ganttChartModelEvent.getOldTask(),
                // ganttChartModelEvent.getNewTask()));
                // TODO: update your UI
            });
        } catch (JsonProcessingException e) {
            // // TODO Auto-generated catch block
            log.error("Error processing WebSocket message: " + e.getMessage());
        }

    }
}

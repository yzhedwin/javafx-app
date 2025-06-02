package com.demo.mmi.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.demo.mmi.entity.GanttChartModel;
import com.demo.mmi.entity.ScheduledTask;
import com.demo.mmi.util.GanttChartModelEvent;
import com.demo.openapi.event.WebSocketEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener for WebSocket events that updates the Gantt chart model.
 * This component listens for WebSocket messages and updates the Gantt chart
 * model accordingly, ensuring that UI updates are performed on the JavaFX
 * thread.
 */
@Slf4j
@Component
@Profile({ "client", "test" })
public class WebSocketListener {

    private final GanttChartModel ganttChartModel;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    public WebSocketListener(GanttChartModel ganttChartModel) {
        this.ganttChartModel = ganttChartModel;
    }

    @EventListener
    public void handleWebSocketMessage(WebSocketEvent event) {
        try {
            GanttChartModelEvent ganttChartModelEvent = mapper.readValue(event.getMessage(),
                    GanttChartModelEvent.class);
            // Update UI on JavaFX thread
            log.info("Received message in JavaFX: " + ganttChartModelEvent.toString());

            Platform.runLater(() -> {
                // Update JavaFX properties on UI thread
                // ganttChartModel.updateTask(ganttChartModelEvent.getOldTask(),
                // ganttChartModelEvent.getNewTask());
                ganttChartModel.removeTask(ganttChartModelEvent.getOldTask());

                ScheduledTask newtTask = ganttChartModelEvent.getNewTask();

                // TODO: validate out of range rbg values
                ganttChartModel.addTask(newtTask.getGroupName(), newtTask.getName(), newtTask.getColour().toFXColor(),
                        newtTask.getStartTime(), newtTask.getEndTime());
            });

        } catch (JsonProcessingException e) {
            // // TODO Auto-generated catch block
            log.error("Error processing WebSocket message: " + e.getMessage());
        }

    }
}

package com.demo.mmi.listener;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.demo.common.model.ScheduledTask;
import com.demo.mmi.entity.GanttChartModel;
import com.demo.mmi.entity.ScheduledTaskGroup;
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

    public WebSocketListener(GanttChartModel ganttChartModel) {
        this.ganttChartModel = ganttChartModel;
    }

    /*
     * Edited for compatibility with SimpleAssetMission model
     * Insufficient fields to handle add task and remove task
     */
    @EventListener
    public void handleWebSocketMessage(WebSocketEvent event) {
        try {
            final ScheduledTask newTask = mapper.readValue(event.getMessage(),
                    ScheduledTask.class);
            log.debug("NEW TASK: {} {} {} {} {}", newTask.getGroupName(), newTask.getName(), newTask.getColour(),
                    newTask.getDuration(),
                    newTask.getStartTime());
            Platform.runLater(() -> {
                ScheduledTask oldTask = null;
                for (ScheduledTaskGroup group : ganttChartModel.getTaskGroupList()) {
                    if (group.getTasks().contains(newTask)) {
                        oldTask = group.getTasks().stream().filter(obj -> obj.equals(newTask))
                                .findFirst().get();
                        break;
                    }
                }
                // Update JavaFX properties on UI thread
                if (oldTask != null) {
                    log.debug("OLD TASK: {} {} {} {} {}", oldTask.getGroupName(), oldTask.getName(),
                            oldTask.getColour(),
                            oldTask.getDuration(), oldTask.getStartTime());
                    // if (oldTask.equals(newTask)) {
                    // ganttChartModel.removeTask(oldTask.getGroupName(), oldTask.getName());
                    // return;
                    // }
                    ganttChartModel.updateTask(oldTask, newTask);
                    return;
                }
                // ganttChartModel.addTask(newTask.getGroupName(), newTask.getName(),
                // newTask.getColour().toFXColor(), newTask.getStartTime(),
                // newTask.getEndTime());

            });
        } catch (JsonProcessingException e) {
            // // TODO Auto-generated catch block
            log.error("Error processing WebSocket message: " + e.getMessage());
        }

    }
}

package com.demo.mmi.entity;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demo.mmi.util.DateTimeStep;
import com.demo.mmi.util.GanttChartUtil;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GanttChartContextMenuManager {

    @Getter
    private final ContextMenu contextMenu = new ContextMenu();
    // TODO To commonalise, change this to accept JavaFX native context menu
    private final Map<String, List<MenuItem>> menuItemsMap = new HashMap<>();
    private final GanttChartModel model;
    private final ObjectProperty<DateTimeStep> stepProperty;
    private final DoubleProperty pixelsPerTimeUnitProperty;
    private final ObjectProperty<ZonedDateTime> startTimeProperty;

    private String menuItemStyleClass = null;

    // Last selected object
    @Getter
    private Object lastSelectedObject;
    @Getter
    private ZonedDateTime lastSelectedTime;

    public GanttChartContextMenuManager(final GanttChartModel model, final ObjectProperty<DateTimeStep> stepProperty,
            final DoubleProperty pixelsPerTimeUnitProperty, final ObjectProperty<ZonedDateTime> startTimeProperty) {
        this.model = model;
        this.stepProperty = stepProperty;
        this.pixelsPerTimeUnitProperty = pixelsPerTimeUnitProperty;
        this.startTimeProperty = startTimeProperty;
        menuItemsMap.put(GanttChartUtil.MENU_TASK_LIST, new ArrayList<>());
        menuItemsMap.put(GanttChartUtil.MENU_SCHEDULED_TASK, new ArrayList<>());
    }

    public void setStyleClass(final String contextMenuStyleClass, final String menuItemStyleClass) {
        contextMenu.getStyleClass().add(contextMenuStyleClass);
        this.menuItemStyleClass = menuItemStyleClass;
        for (List<MenuItem> list : menuItemsMap.values()) {
            for (MenuItem mi : list) {
                mi.getStyleClass().add(menuItemStyleClass);
            }
        }
    }

    public void addMenuItem(final String s, final MenuItem item) {
        List<MenuItem> list = menuItemsMap.get(s);
        if (list != null) {
            if (menuItemStyleClass != null) {
                item.getStyleClass().add(menuItemStyleClass);
            }
            list.add(item);
        } else {
            log.error("Context Menu {} doesn't exist", s);
        }
    }

    public void removeMenuItem(final String s, final MenuItem item) {
        List<MenuItem> list = menuItemsMap.get(s);
        if (list != null) {
            list.remove(item);
        } else {
            log.error("Context Menu {} doesn't exist", s);
        }
    }

    // TODO To commonalise, change this to accept different MouseButtons
    // Overlapping events cannot be properly selected in 1 group
    public final EventHandler<MouseEvent> createTriggerHandler() {
        return evt -> {
            if (evt.getButton() == MouseButton.SECONDARY) {
                contextMenu.getItems().clear();
                MenuItem[] taskListArr = null;
                Node parentNode = null;
                Object source = evt.getSource();
                if (source instanceof Pane) {
                    parentNode = (Pane) source;
                    taskListArr = menuItemsMap.get(GanttChartUtil.MENU_TASK_LIST).toArray(new MenuItem[0]);
                    int lastClickedIndex = Double.valueOf(evt.getY() / GanttChartUtil.TASK_HEIGHT).intValue();
                    lastSelectedObject = model.getTaskGroup(lastClickedIndex);
                    double delta = evt.getX() / pixelsPerTimeUnitProperty.get();
                    lastSelectedTime = stepProperty.get().getDateTimeWithOffset(startTimeProperty.get(), delta);
                    evt.consume();
                } else if (source instanceof ScheduledTaskBar) {
                    ScheduledTaskBar bar = (ScheduledTaskBar) source;
                    parentNode = bar.getParent();
                    taskListArr = menuItemsMap.get(GanttChartUtil.MENU_SCHEDULED_TASK).toArray(new MenuItem[0]);
                    lastSelectedObject = source;
                    evt.consume();
                }

                if (taskListArr != null && parentNode != null) {
                    contextMenu.getItems().addAll(taskListArr);
                    contextMenu.show(parentNode, evt.getScreenX(), evt.getScreenY());
                }
            }
        };
    }
}

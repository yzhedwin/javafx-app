package com.demo.mmi.chart;

import java.util.HashMap;
import java.util.Map;

import com.demo.mmi.entity.GanttChartModel;
import com.demo.mmi.entity.ScheduledTaskGroup;
import com.demo.mmi.util.GanttChartUtil;

import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.MapChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class GanttChartTaskGroupList extends VBox {
	private final Pane dataPane = new Pane();
    private final Map<String, Label> groupLabelMap = new HashMap<>();

    public GanttChartTaskGroupList(final GanttChartModel model,
            final ObservableDoubleValue dataHeightReferenceProperty) {
        dataPane.getStyleClass().add(GanttChartUtil.CSS_TASK_GROUP_LIST_INFORMATION);
        dataPane.prefHeightProperty().bind(dataHeightReferenceProperty);
        getChildren().add(dataPane);

        model.getTaskGroupMap().addListener(new MapChangeListener<>() {
            @Override
            public void onChanged(
                    Change<? extends String, ? extends ScheduledTaskGroup> evt) {
                if (evt.wasRemoved()) {
                    removeTaskGroup(evt.getKey());
                }

                if (evt.wasAdded()) {
                    addTaskGroup(evt.getKey());
                }
            }

        });
    }

    private void addTaskGroup(final String id) {
        if (!groupLabelMap.containsKey(id)) {
            Label label = new Label();
            label.getStyleClass().add(GanttChartUtil.CSS_TASK_GROUP_LIST_DATA);
            label.setText(id);
            label.setAlignment(Pos.CENTER_LEFT);
            label.prefWidthProperty().bind(widthProperty());
            label.setPrefHeight(GanttChartUtil.TASK_HEIGHT);
            getChildren().add(label);
        }
    }

    private void removeTaskGroup(final String id) {
        Label label = groupLabelMap.remove(id);
        if (label != null) {
            getChildren().remove(label);
            label.prefWidthProperty().unbind();
        }
    }
}

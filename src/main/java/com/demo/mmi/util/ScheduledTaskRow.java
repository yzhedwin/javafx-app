package com.demo.mmi.util;

import javafx.scene.layout.Pane;

public class ScheduledTaskRow extends Pane {

    public ScheduledTaskRow() {
        getStyleClass().add(GanttChartUtil.CSS_ROW_INDICATOR);
        setPrefHeight(GanttChartUtil.TASK_HEIGHT);
    }
}

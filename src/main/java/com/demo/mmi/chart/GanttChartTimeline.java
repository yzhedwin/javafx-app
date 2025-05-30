package com.demo.mmi.chart;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.demo.mmi.util.DateTimeStep;
import com.demo.mmi.util.GanttChartUtil;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class GanttChartTimeline extends HBox {
    private final InvalidationListener refreshTimelineListener = evt -> {
        updateScale();
    };
    private final ObjectProperty<ZonedDateTime> startTimeProperty;
    private final ObjectProperty<DateTimeStep> stepProperty;
    private final DoubleProperty pixelsPerTimeUnitProperty;

    // For dragging
    private double pressedX;
    private ZonedDateTime referencedDateTime;

    // For steps
    private final DateTimeStep[] steps = GanttChartUtil.DATE_TIME_STEPS;
    private int currentStepIndex = GanttChartUtil.INITIAL_STEP_INDEX;
    private final List<Label> labelList = new ArrayList<>();

    public GanttChartTimeline(final ObjectProperty<ZonedDateTime> startTimeProperty,
            final ObjectProperty<DateTimeStep> stepProperty, final DoubleProperty pixelsPerTimeUnitProperty) {
        this.startTimeProperty = startTimeProperty;
        this.pixelsPerTimeUnitProperty = pixelsPerTimeUnitProperty;
        this.stepProperty = stepProperty;
        getStyleClass().add(GanttChartUtil.CSS_TIMELINE);

        addMouseEvents();
        initScale();
        updateScale();

        startTimeProperty.addListener(refreshTimelineListener);
        stepProperty.addListener(refreshTimelineListener);
        pixelsPerTimeUnitProperty.addListener(refreshTimelineListener);
    }

    private void initScale() {
        for (int i = 0; i < GanttChartUtil.COLUMN_COUNT; i++) {
            Label cl = new Label();
            cl.prefWidthProperty().bind(pixelsPerTimeUnitProperty);
            cl.setPadding(GanttChartUtil.TIMELINE_PADDING);
            cl.setBorder(GanttChartUtil.TIMELINE_UNIT_BORDER);
            ZonedDateTime newTime = stepProperty.get().getDateTimeWithOffset(startTimeProperty.get(), i);
            cl.setText(GanttChartUtil.DATE_TIME_FORMATTER.format(newTime));
            getChildren().add(cl);
            labelList.add(cl);
        }
    }

    private void updateScale() {
        int size = labelList.size();
        for (int i = 0; i < size; i++) {
            ZonedDateTime newTime = stepProperty.get().getDateTimeWithOffset(startTimeProperty.get(), i);
            labelList.get(i).setText(GanttChartUtil.DATE_TIME_FORMATTER.format(newTime));
        }
    }

    private void addMouseEvents() {
        setOnMouseEntered(evt -> {
            getScene().setCursor(Cursor.H_RESIZE);
        });

        setOnMouseExited(evt -> {
            getScene().setCursor(Cursor.DEFAULT);
        });

        setOnMousePressed(evt -> {
            pressedX = evt.getSceneX();
            referencedDateTime = startTimeProperty.get();
        });

        setOnMouseDragged(evt -> {
            if (referencedDateTime != null) {
                double deltaX = evt.getSceneX() - pressedX;
                DateTimeStep step = stepProperty.get();
                double timeUnitChange = deltaX / pixelsPerTimeUnitProperty.get();
                double deltaTime = step.getValue() * timeUnitChange;
                ZonedDateTime newTime = step.getDateTimeWithOffset(referencedDateTime, deltaTime);
                startTimeProperty.set(newTime);
            }
        });

        setOnMouseReleased(evt -> {
            referencedDateTime = null;
        });

        setOnScroll(evt -> {
            currentStepIndex = evt.getDeltaY() > 0 ? --currentStepIndex : ++currentStepIndex;
            currentStepIndex = Math.min(steps.length - 1, Math.max(0, currentStepIndex));
            stepProperty.set(steps[currentStepIndex]);
        });
    }
}

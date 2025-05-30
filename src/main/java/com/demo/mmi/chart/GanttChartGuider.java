package com.demo.mmi.chart;

import java.time.ZonedDateTime;

import com.demo.mmi.util.DateTimeStep;
import com.demo.mmi.util.GanttChartUtil;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class GanttChartGuider extends VBox {
    private final Label informationLabel = new Label();
    private final Pane guiderTrack = new Pane();

    private final DoubleProperty pixelsPerTimeUnitProperty;
    private final ObjectProperty<DateTimeStep> stepProperty;
    private final ObjectProperty<ZonedDateTime> startTimeProperty;
    private final DoubleProperty guidingLineRatioProperty;
    private final StringProperty informationProperty;
    private final Pane guiderButton;

    public GanttChartGuider(final DoubleProperty pixelsPerTimeUnitProperty,
            final ObjectProperty<DateTimeStep> stepProperty,
            final ObjectProperty<ZonedDateTime> startTimeProperty, final DoubleProperty guidingLineRatioProperty,
            final StringProperty informationProperty) {
        super();
        getStyleClass().add(GanttChartUtil.CSS_GUIDER);
        guiderTrack.getStyleClass().add(GanttChartUtil.CSS_GUIDER_TRACK);
        this.pixelsPerTimeUnitProperty = pixelsPerTimeUnitProperty;
        this.stepProperty = stepProperty;
        this.startTimeProperty = startTimeProperty;
        this.guidingLineRatioProperty = guidingLineRatioProperty;
        this.informationProperty = informationProperty;
        informationLabel.textProperty().bind(informationProperty);
        getChildren().add(informationLabel);
        getChildren().add(guiderTrack);

        guiderButton = createGuider();
        pixelsPerTimeUnitProperty.addListener(evt -> {
            guiderButton.setLayoutX(
                    guiderTrack.getWidth() * guidingLineRatioProperty.get() - GanttChartUtil.GUIDER_WIDTH / 2);
        });
    }

    private Pane createGuider() {
        Pane pane = new Pane();
        pane.getStyleClass().add(GanttChartUtil.CSS_GUIDER_BUTTON);
        pane.setPrefSize(GanttChartUtil.GUIDER_WIDTH, GanttChartUtil.GUIDER_HEIGHT);
        double halfWidth = GanttChartUtil.GUIDER_WIDTH / 2;

        guiderTrack.getChildren().add(pane);

        pane.setOnMouseMoved(evt -> {
            getScene().setCursor(Cursor.H_RESIZE);
        });

        pane.setOnMouseExited(evt -> {
            getScene().setCursor(Cursor.DEFAULT);
        });

        pane.setOnMousePressed(pressedEvt -> {
            double pressedX = pressedEvt.getSceneX() - pane.getLayoutBounds().getMinX();
            double x = pane.getLayoutX();
            double maxWidth = guiderTrack.getWidth();
            double maxX = maxWidth - halfWidth;
            pane.setOnMouseDragged(draggedEvt -> {
                double newX = x + draggedEvt.getSceneX() - pressedX;
                newX = Math.max(-halfWidth, Math.min(newX, maxX));
                pane.setLayoutX(newX);
                guidingLineRatioProperty.set((newX + halfWidth) / maxWidth);
                updateInformation();
            });
        });

        pane.setOnMouseReleased(evt -> {
            informationProperty.set("");
        });

        return pane;
    }

    private void updateInformation() {
        double delta = guidingLineRatioProperty.get() * guiderTrack.getWidth() / pixelsPerTimeUnitProperty.get();
        ZonedDateTime dt = stepProperty.get().getDateTimeWithOffset(startTimeProperty.get(), delta);
        String info = GanttChartUtil.DATE_TIME_FORMATTER.format(dt).replace('\n', ' ');
        informationProperty.set(info);
    }
}

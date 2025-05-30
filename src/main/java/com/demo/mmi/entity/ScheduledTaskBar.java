package com.demo.mmi.entity;

import java.util.Objects;

import com.demo.mmi.util.DateTimeStep;
import com.demo.mmi.util.GanttChartUtil;
import com.demo.mmi.util.GanttChartUtil.EColourUtil;
import com.demo.mmi.util.GanttChartUtil.EResizeDirection;
import com.demo.mmi.util.IGanttChartRowChecker;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import lombok.Getter;

public class ScheduledTaskBar extends Group {
	private static final double RESIZE_BORDER_OFFSET = 5.0; // 5px
	private static final double BORDER_WIDTH = 2.0; // 5px
	private static final Color BORDER_COLOUR = EColourUtil.GREY_100.getColour();

	@Getter
	private final ScheduledTask task;
	private final ObjectProperty<DateTimeStep> stepProperty;
	private final double pixelPerTimeUnit;
	private final IGanttChartRowChecker rowChecker;
	private final ChangeListener<ScheduledTask> taskChangeListener;

	private DoubleProperty barWidthProperty;

	private Rectangle bar;

	// Resize
	private EResizeDirection direction = EResizeDirection.NIL;
	private double originalStartX = -1;
	private double originalEndX = -1;
	private double originalStartY = -1;

	public ScheduledTaskBar(final ScheduledTask task, final ObjectProperty<DateTimeStep> stepProperty,
			final double pixelPerTimeUnit, final IGanttChartRowChecker rowChecker,
			final ChangeListener<ScheduledTask> taskChangeListener) {
		this.task = task;
		this.stepProperty = stepProperty;
		this.pixelPerTimeUnit = pixelPerTimeUnit;
		this.rowChecker = Objects.requireNonNull(rowChecker);
		this.taskChangeListener = Objects.requireNonNull(taskChangeListener);
		createBar();
		addMouseEvents();
	}

	private void createBar() {
		double duration = stepProperty.get().getSecondsRatio(task.getDuration()) * pixelPerTimeUnit;
		double height = GanttChartUtil.TASK_HEIGHT;
		double arc = height * 0.2;
		bar = new Rectangle(0, 0, duration, height);
		bar.fillProperty().bind(Bindings.createObjectBinding(() -> {
			return bar.hoverProperty().get() ? task.getColour() : task.getColour().darker();
		}, bar.hoverProperty()));
		bar.setArcHeight(arc);
		bar.setArcWidth(arc);
		bar.setStroke(BORDER_COLOUR);
		bar.setStrokeWidth(BORDER_WIDTH);
		bar.setStrokeType(StrokeType.INSIDE);
		barWidthProperty = bar.widthProperty();
		getChildren().add(bar);

		Label label = new Label();
		label.minWidthProperty().bind(barWidthProperty.subtract(RESIZE_BORDER_OFFSET * 2));
		label.prefHeightProperty().bind(bar.heightProperty().subtract(RESIZE_BORDER_OFFSET * 2));
		label.setLayoutX(RESIZE_BORDER_OFFSET);
		label.setLayoutY(RESIZE_BORDER_OFFSET);
		label.setAlignment(Pos.CENTER);
		label.setText(task.getName());
		getChildren().add(label);

		// To ensure only 1 click event is fired
		// Tested: Label blocks all except click events
		label.addEventHandler(MouseEvent.ANY, evt -> {
			if (evt.getEventType() != MouseEvent.MOUSE_CLICKED) {
				bar.fireEvent(evt);
			}
		});
	}

	private void addMouseEvents() {
		bar.setOnMouseMoved(evt -> {
			direction = getResizeDirection(evt);
		});

		bar.setOnMouseExited(evt -> {
			getScene().setCursor(Cursor.DEFAULT);
		});

		bar.setOnMousePressed(pressedEvt -> {
			// TODO To commonalise, change this to accept different MouseButtons
			if (pressedEvt.getButton() != MouseButton.PRIMARY) {
				return;
			}

			double pressedX = pressedEvt.getSceneX();
			double pressedY = pressedEvt.getSceneY();
			double refWidth = bar.getLayoutBounds().getWidth();
			double refHeight = bar.getLayoutBounds().getHeight();
			originalStartX = getLayoutX();
			originalStartY = getLayoutY();
			originalEndX = originalStartX + getLayoutBounds().getWidth();
			bar.setOnMouseDragged(draggedEvt -> {
				double deltaX = draggedEvt.getSceneX() - pressedX;
				switch (direction) {
					case EAST:
						bar.setWidth(refWidth + deltaX);
						break;
					case WEST:
						setLayoutX(originalStartX + deltaX);
						bar.setWidth(refWidth - deltaX);
						break;
					case NIL:
						double x = originalStartX + draggedEvt.getSceneX() - pressedX;
						double y = originalStartY + draggedEvt.getSceneY() - pressedY;
						Parent p = getParent();
						if (p != null) {
							Bounds b = p.getLayoutBounds();
							if (b != null) {
								if (y <= b.getMinY()) {
									y = b.getMinY();
								} else if ((y + refHeight) >= b.getMaxY()) {
									y = b.getMaxY() - refHeight;
								}
							}
						}
						setLayoutX(x);
						setLayoutY(y);
						rowChecker.onTaskDrag(x + refWidth / 2, y + refHeight / 2);
						break;
					default:
						break;
				}
			});
		});

		bar.setOnMouseReleased(evt -> {
			// TODO To commonalise, change this to accept different MouseButtons
			if (evt.getButton() != MouseButton.PRIMARY) {
				return;
			}

			double currentStartX = getLayoutX();
			double currentEndX = currentStartX + getLayoutBounds().getWidth();
			double startTimeDelta = (currentStartX - originalStartX) / pixelPerTimeUnit;
			double endTimeDelta = (currentEndX - originalEndX) / pixelPerTimeUnit;
			DateTimeStep step = stepProperty.get();
			ScheduledTask oldTask = task.duplicate();
			task.setStartTime(step.getDateTimeWithOffset(task.getStartTime(), startTimeDelta));
			task.setEndTime(step.getDateTimeWithOffset(task.getEndTime(), endTimeDelta));
			rowChecker.onTaskDragEnd(this);
			taskChangeListener.changed(null, oldTask, task);
		});
	}

	// Also changes the cursor icon
	private EResizeDirection getResizeDirection(final MouseEvent evt) {
		Bounds barLayout = bar.getLayoutBounds();
		double x = evt.getX();

		if (x < RESIZE_BORDER_OFFSET) {
			getScene().setCursor(Cursor.H_RESIZE);
			return EResizeDirection.WEST;
		} else if (x > (barLayout.getWidth() - RESIZE_BORDER_OFFSET)) {
			getScene().setCursor(Cursor.H_RESIZE);
			return EResizeDirection.EAST;
		} else {
			getScene().setCursor(Cursor.MOVE);
			return EResizeDirection.NIL;
		}
	}
}

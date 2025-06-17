package com.demo.mmi.chart;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import com.demo.common.model.ScheduledTask;
import com.demo.mmi.entity.GanttChartContextMenuManager;
import com.demo.mmi.entity.GanttChartModel;
import com.demo.mmi.entity.ScheduledTaskBar;
import com.demo.mmi.entity.ScheduledTaskGroup;
import com.demo.mmi.util.DateTimeStep;
import com.demo.mmi.util.GanttChartUtil;
import com.demo.mmi.util.IGanttChartModelInternal;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class GanttChart extends SplitPane {
	private final ObjectProperty<DateTimeStep> stepProperty = new SimpleObjectProperty<>(
			new DateTimeStep(1, TimeUnit.MINUTES));
	private final DoubleProperty pixelsPerTimeUnitProperty = new SimpleDoubleProperty();

	private final ObjectProperty<ZonedDateTime> startTimeProperty = new SimpleObjectProperty<>(ZonedDateTime.now());
	private final DoubleProperty guidingLineRatioProperty = new SimpleDoubleProperty(0);
	private final StringProperty informationProperty = new SimpleStringProperty("");

	@Getter
	private final GanttChartModel model = new GanttChartModel();
	private final IGanttChartModelInternal modelInternal = model.createModelInternal();

	@Getter
	private final GanttChartContextMenuManager contextMenuManager = new GanttChartContextMenuManager(model,
			stepProperty, pixelsPerTimeUnitProperty, startTimeProperty);
	private final EventHandler<MouseEvent> contextMenuTriggerHandler = contextMenuManager.createTriggerHandler();

	private final VBox guideTimelineTaskHolder = new VBox();

	private final GanttChartTimeline timeline = new GanttChartTimeline(startTimeProperty, stepProperty,
			pixelsPerTimeUnitProperty);

	private final GanttChartGuider guider = new GanttChartGuider(pixelsPerTimeUnitProperty, stepProperty,
			startTimeProperty, guidingLineRatioProperty, informationProperty);

	private final BooleanProperty taskGroupListVisibleProperty = new SimpleBooleanProperty(true);
	private final GanttChartTaskGroupList taskGroupList = new GanttChartTaskGroupList(model,
			timeline.heightProperty().add(guider.heightProperty()));

	private final BooleanProperty taskListVisibleProperty = new SimpleBooleanProperty(true);
	private final GanttChartTaskList taskList = new GanttChartTaskList(model, startTimeProperty, stepProperty,
			pixelsPerTimeUnitProperty, guidingLineRatioProperty, contextMenuTriggerHandler,
			model.getTaskChangeListener(), informationProperty);

	public GanttChart(final double width, final double height) {
		getStylesheets().add(GanttChartUtil.CSS_PATH);
		getStyleClass().add(GanttChartUtil.CSS_GANTTCHART);

		setMinSize(width, height);
		setMaxSize(width, height);

		guideTimelineTaskHolder.getChildren().add(timeline);
		guideTimelineTaskHolder.getChildren().add(guider);
		guideTimelineTaskHolder.getChildren().add(taskList);

		createDefaultMenuItems();

		getItems().add(taskGroupList);
		getItems().add(guideTimelineTaskHolder);
		setDividerPosition(0, 0.3);

		pixelsPerTimeUnitProperty.bind(guideTimelineTaskHolder.widthProperty().divide(GanttChartUtil.COLUMN_COUNT));

		taskGroupListVisibleProperty.addListener((obj, oldVal, newVal) -> {
			if (newVal) {
				getItems().add(0, taskGroupList);
			} else {
				getItems().remove(taskGroupList);
			}
		});

		taskListVisibleProperty.addListener((obj, oldVal, newVal) -> {
			if (newVal) {
				getItems().add(1, taskList);
			} else {
				getItems().remove(taskList);
			}
		});
	}

	public void setTaskGroupListVisible(final boolean isVisible) {
		taskGroupListVisibleProperty.set(isVisible);
	}

	public void setTaskListVisible(final boolean isVisible) {
		taskListVisibleProperty.set(isVisible);
	}

	public void serStartTime(final ZonedDateTime dateTime) {
		startTimeProperty.set(dateTime);
	}

	private void createDefaultMenuItems() {
		MenuItem deleteItem = new MenuItem(GanttChartUtil.MENU_DELETE);
		deleteItem.setOnAction(evt -> {
			Object o = contextMenuManager.getLastSelectedObject();
			if (o instanceof ScheduledTaskBar) {
				ScheduledTaskBar bar = (ScheduledTaskBar) o;
				ScheduledTask task = bar.getTask();
				modelInternal.removeTask(task);
			}
		});
		contextMenuManager.addMenuItem(GanttChartUtil.MENU_SCHEDULED_TASK, deleteItem);

		MenuItem addItem = new MenuItem(GanttChartUtil.MENU_ADD);
		addItem.setOnAction(evt -> {
			Object o = contextMenuManager.getLastSelectedObject();
			if (o instanceof ScheduledTaskGroup) {
				ScheduledTaskGroup group = (ScheduledTaskGroup) o;
				ZonedDateTime startTime = contextMenuManager.getLastSelectedTime();
				ZonedDateTime endTime = stepProperty.get().getDateTimeWithOffset(startTime, 1);
				modelInternal.addTask(group.getId(), startTime, endTime);
			}
		});
		contextMenuManager.addMenuItem(GanttChartUtil.MENU_TASK_LIST, addItem);
	}
}

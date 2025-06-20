package com.demo.mmi.chart;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.demo.common.model.ScheduledTask;
import com.demo.mmi.entity.GanttChartModel;
import com.demo.mmi.entity.ScheduledTaskBar;
import com.demo.mmi.entity.ScheduledTaskGroup;
import com.demo.mmi.util.DateTimeStep;
import com.demo.mmi.util.GanttChartUtil;
import com.demo.mmi.util.IGanttChartRowChecker;
import com.demo.mmi.util.ScheduledTaskRow;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.extern.log4j.Log4j2;

/**
 * ScrollPane used for hiding elements outside the viewport
 */
@Log4j2
public class GanttChartTaskList extends ScrollPane {
	private final Pane content = new Pane();
	private final InvalidationListener refreshTaskListener = evt -> {
		updateTasks();
	};

	private final GanttChartModel model;
	private final IGanttChartRowChecker rowChecker = createRowChecker();
	private final ChangeListener<ScheduledTask> taskChangeListener;
	private final ObjectProperty<ZonedDateTime> startTimeProperty;
	private final ObjectProperty<DateTimeStep> stepProperty;
	private final DoubleProperty pixelsPerTimeUnitProperty;
	private final DoubleProperty guidingLineRatioProperty;
	private final StringProperty informationProperty;

	private final EventHandler<MouseEvent> mouseClickEvent;
	private final List<ScheduledTaskBar> taskBarList = new ArrayList<>();

	// For dragging
	private int lastHoveredIndex = -1;
	private ScheduledTaskRow indicator = new ScheduledTaskRow();

	// For guider
	private final Pane guiderLine;

	public GanttChartTaskList(final GanttChartModel model, final ObjectProperty<ZonedDateTime> startTimeProperty,
			final ObjectProperty<DateTimeStep> stepProperty, final DoubleProperty pixelsPerTimeUnitProperty,
			final DoubleProperty guidingLineRatioProperty, final EventHandler<MouseEvent> mouseClickEvent,
			final ChangeListener<ScheduledTask> taskChangeListener, final StringProperty informationProperty) {
		this.model = model;
		this.startTimeProperty = startTimeProperty;
		this.stepProperty = stepProperty;
		this.pixelsPerTimeUnitProperty = pixelsPerTimeUnitProperty;
		this.guidingLineRatioProperty = guidingLineRatioProperty;
		this.taskChangeListener = taskChangeListener;
		this.mouseClickEvent = mouseClickEvent;
		this.informationProperty = informationProperty;
		setContent(content);
		setFitToWidth(true);

		content.getStyleClass().add(GanttChartUtil.CSS_TASK_LIST);
		content.setOnMouseClicked(mouseClickEvent);
		content.setOnMouseMoved(evt -> {
			double x = evt.getSceneX() - content.localToScene(0, 0).getX();
			double deltaTime = x / pixelsPerTimeUnitProperty.get();
			DateTimeStep step = stepProperty.get();
			ZonedDateTime zdt = step.getDateTimeWithOffset(startTimeProperty.get(), deltaTime);
			String dt = GanttChartUtil.DATE_TIME_INFO_FORMATTER.format(zdt);
			informationProperty.set(dt);
		});
		content.setOnMouseExited(evt -> {
			informationProperty.set("");
		});

		startTimeProperty.addListener(refreshTaskListener);
		stepProperty.addListener(refreshTaskListener);
		model.addTaskInvalidationListener(refreshTaskListener);
		pixelsPerTimeUnitProperty.addListener(refreshTaskListener);

		indicator.prefWidthProperty().bind(content.widthProperty());
		indicator.setLayoutX(0);
		indicator.setVisible(false);

		guiderLine = createGuiderLine();
		content.getChildren().add(guiderLine);
	}

	private void updateTasks() {
		content.getChildren().clear();
		clearTaskBarEvents();
		taskBarList.clear();

		List<ScheduledTaskGroup> list = model.getTaskGroupList();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			ScheduledTaskGroup group = list.get(i);
			List<ScheduledTask> taskList = group.getTasks();
			for (ScheduledTask task : taskList) {
				ScheduledTaskBar rect = createTaskBar(task, i);
				content.getChildren().add(rect);
				rect.setOnMouseClicked(mouseClickEvent);
				taskBarList.add(rect);
			}
		}

		content.getChildren().add(indicator);
		content.getChildren().add(guiderLine);
	}

	private void clearTaskBarEvents() {
		for (ScheduledTaskBar tb : taskBarList) {
			tb.setOnMouseClicked(null);
		}
	}

	private ScheduledTaskBar createTaskBar(final ScheduledTask task, final int index) {
		double pixelPerTimeUnit = pixelsPerTimeUnitProperty.get();
		ScheduledTaskBar taskBar = new ScheduledTaskBar(task, stepProperty, pixelPerTimeUnit, rowChecker,
				taskChangeListener);
		double secondsDelta = Duration.between(startTimeProperty.get(), task.getStartTime()).toSeconds();
		double startX = stepProperty.get().getSecondsRatio(secondsDelta) * pixelPerTimeUnit;
		taskBar.setLayoutX(startX);
		taskBar.setLayoutY(index * GanttChartUtil.TASK_HEIGHT);
		return taskBar;
	}

	private Pane createGuiderLine() {
		Pane line = new Pane();
		line.setPrefWidth(1);
		line.prefHeightProperty().bind(content.heightProperty());
		line.getStyleClass().add(GanttChartUtil.CSS_GUIDER_LINE);
		guidingLineRatioProperty.addListener((obj, oldVal, newVal) -> {
			line.setLayoutX(newVal.doubleValue() * getWidth());
		});
		pixelsPerTimeUnitProperty.addListener(evt -> {
			line.setLayoutX(guidingLineRatioProperty.get() * getWidth());
		});

		return line;
	}

	private IGanttChartRowChecker createRowChecker() {
		return new IGanttChartRowChecker() {
			@Override
			public void onTaskEnter(ScheduledTaskBar taskBar) {
				ScheduledTask task = taskBar.getTask();
				informationProperty.set(createTaskInformation(task.getName(), task.getGroupName(), null,
						task.getStartTime(), task.getEndTime()));
			}

			@Override
			public void onTaskExit(ScheduledTaskBar taskBar) {
				informationProperty.set("");
			}

			@Override
			public void onTaskDrag(ScheduledTaskBar taskBar, double centreY, double startTimeDelta,
					double endTimeDelta) {
				lastHoveredIndex = Double.valueOf(centreY / GanttChartUtil.TASK_HEIGHT).intValue();
				indicator.setVisible(true);
				indicator.setLayoutY(lastHoveredIndex * GanttChartUtil.TASK_HEIGHT);

				ScheduledTask task = taskBar.getTask();
				ScheduledTaskGroup newGroup = model.getTaskGroup(lastHoveredIndex);
				String groupName = task.getGroupName();
				String newGroupName = null;
				if (newGroup != null && !groupName.equals(newGroup.getId())) {
					newGroupName = newGroup.getId();
				}

				ZonedDateTime startTime = stepProperty.get().getDateTimeWithOffset(task.getStartTime(), startTimeDelta);
				ZonedDateTime endTime = stepProperty.get().getDateTimeWithOffset(task.getEndTime(), endTimeDelta);
				informationProperty.set(
						createTaskInformation(task.getName(), task.getGroupName(), newGroupName, startTime, endTime));
			}

			@Override
			public void onTaskDragEnd(ScheduledTaskBar taskBar) {
				indicator.setVisible(false);
				taskBar.setLayoutY(indicator.getLayoutY());
				ScheduledTask task = taskBar.getTask();
				ScheduledTaskGroup group = model.getTaskGroup(task.getGroupName());
				ScheduledTaskGroup newGroup = model.getTaskGroup(lastHoveredIndex);
				if (group != null && newGroup != null) {
					group.removeTask(task);
					newGroup.addTask(task);
				}
				informationProperty.set("");
			}
		};
	}

	private String createTaskInformation(final String name, final String oldGroupName, final String newGroupName,
			final ZonedDateTime startTime, final ZonedDateTime endTime) {
		StringJoiner sj = new StringJoiner(" | ");
		sj.add(name);
		if (newGroupName != null) {
			sj.add(oldGroupName + " --> " + newGroupName);
		}
		String startDt = GanttChartUtil.DATE_TIME_INFO_FORMATTER.format(startTime);
		String endDt = GanttChartUtil.DATE_TIME_INFO_FORMATTER.format(endTime);
		sj.add(startDt + " - " + endDt);
		return sj.toString();
	}
}

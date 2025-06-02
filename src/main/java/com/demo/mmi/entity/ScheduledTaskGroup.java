package com.demo.mmi.entity;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.demo.mmi.util.GanttChartUtil;
import com.demo.mmi.util.SerializableColor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ScheduledTaskGroup {
	private final Object lock = new Object();

	@Getter
	@EqualsAndHashCode.Include
	private final String id;
	@Getter
	private final ObservableList<ScheduledTask> taskList = FXCollections.observableArrayList();

	public ScheduledTask addTask(final ZonedDateTime startTime, final ZonedDateTime endTime) {
		return addTask(GanttChartUtil.DEFAULT_TASK_NAME, GanttChartUtil.DEFAULT_TASK_COLOUR, startTime, endTime);
	}

	public ScheduledTask addTask(final String name, final Color colour, final ZonedDateTime start,
			final ZonedDateTime end) {
		ScheduledTask st = ScheduledTask.builder()
				.id(GanttChartUtil.getNewTaskId())
				.groupName(id)
				.name(name)
				.colour(new SerializableColor(colour))
				.startTime(start)
				.endTime(end)
				.build();
		addTask(st);
		return st;
	}

	public void addTask(final ScheduledTask st) {
		st.setGroupName(id);
		synchronized (lock) {
			taskList.add(st);
		}
	}

	public void removeTask(final ScheduledTask st) {
		synchronized (lock) {
			taskList.remove(st);
		}
	}

	public List<ScheduledTask> getTasks() {
		synchronized (lock) {
			return new ArrayList<>(taskList);
		}
	}

	public void updateTask(final ScheduledTask st) {
		synchronized (lock) {
			int index = taskList.indexOf(st);
			if (index != -1) {
				taskList.set(index, st);
			}
		}
	}
}

package com.demo.mmi.entity;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.demo.common.model.ScheduledTask;
import com.demo.common.model.SerializableColor;
import com.demo.mmi.util.GanttChartModelEvent;
import com.demo.mmi.util.GanttChartUtil.EModelEvent;
import com.demo.mmi.util.GanttChartUtil.EModelEventSource;
import com.demo.mmi.util.IGanttChartModelInternal;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GanttChartModel {
	@Getter
	private final ObservableMap<String, ScheduledTaskGroup> taskGroupMap = FXCollections.observableHashMap();
	private final List<String> idIndexer = new ArrayList<>();
	private final Object lock = new Object();

	private final List<InvalidationListener> taskInvalidationListeners = new ArrayList<>();

	@Getter
	private final ChangeListener<ScheduledTask> taskChangeListener = createTaskChangeListener();

	@Getter
	private final ObjectProperty<GanttChartModelEvent> modelEventProperty = new SimpleObjectProperty<>();

	public void addTaskGroup(final String groupId) {
		synchronized (lock) {
			if (!taskGroupMap.containsKey(groupId)) {
				ScheduledTaskGroup group = new ScheduledTaskGroup(groupId);
				bindTaskListeners(group.getTaskList());
				taskGroupMap.put(groupId, group);
				idIndexer.add(groupId);
			}
		}
	}

	public void addTask(final String groupId, final String name, final Color colour, final ZonedDateTime start,
			final ZonedDateTime end) {
		synchronized (lock) {
			ScheduledTaskGroup taskGroup = taskGroupMap.get(groupId);
			if (taskGroup != null) {
				ScheduledTask st = taskGroup.addTask(name, colour, start, end);
				modelEventProperty.set(new GanttChartModelEvent(EModelEventSource.EXTERNAL, EModelEvent.ADD, null, st));
			}
		}
	}

	private void updateTaskGroup(final ScheduledTaskGroup taskGroup, final List<ScheduledTask> list,
			final String groupId,
			final String name,
			final Color colour, final ZonedDateTime start,
			final ZonedDateTime end) {
		synchronized (lock) {
			for (ScheduledTask st : list) {
				ScheduledTask oldSt = st.duplicate();
				st.setName(name);
				st.setColour(new SerializableColor(colour));
				st.setStartTime(start);
				st.setEndTime(end);
				taskGroup.addTask(st);
				modelEventProperty
						.set(new GanttChartModelEvent(EModelEventSource.EXTERNAL, EModelEvent.CHANGE, oldSt, st));
			}
		}
	}

	/*
	 * Edited for compatibility with SimpleAssetMission model
	 * Insufficient fields to handle groups
	 */
	public void updateTask(final ScheduledTask oldTask, final ScheduledTask newTask) {
		synchronized (lock) {
			ScheduledTaskGroup taskGroup = taskGroupMap.get(oldTask.getGroupName());
			// if (oldTask.getGroupName().compareTo(newTask.getGroupName()) == 0) { // Same
			// group
			List<ScheduledTask> list = taskGroup.removeTask(oldTask.getName());
			updateTaskGroup(taskGroup, list, oldTask.getGroupName(), newTask.getName(),
					oldTask.getColour().toFXColor(),
					newTask.getStartTime(), newTask.getEndTime());
			log.debug("Updating task in the same group: " + oldTask.getGroupName());
			return;
			// }
			// Different group, remove from old group and add to new group
			// List<ScheduledTask> list = taskGroup.removeTask(oldTask.getName());
			// taskGroup = taskGroupMap.get(newTask.getGroupName());
			// updateTaskGroup(taskGroup, list, newTask.getGroupName(), newTask.getName(),
			// newTask.getColour().toFXColor(),
			// newTask.getStartTime(), newTask.getEndTime());
			// log.debug("Updating task in the different group: " + oldTask.getGroupName());
		}
	}

	public void removeTaskGroup(final String groupId) {
		synchronized (lock) {
			ScheduledTaskGroup group = taskGroupMap.remove(groupId);
			if (group != null) {
				unbindTaskListeners(group.getTaskList());
				idIndexer.remove(groupId);

				List<ScheduledTask> list = group.getTasks();
				int sz = list.size();
				for (int i = 0; i < sz; i++) {
					modelEventProperty.set(new GanttChartModelEvent(EModelEventSource.EXTERNAL, EModelEvent.REMOVE,
							list.get(i), null));
				}
			}
		}
	}

	public void removeTask(final String groupId, final String name) {
		synchronized (lock) {
			ScheduledTaskGroup taskGroup = taskGroupMap.get(groupId);
			if (taskGroup != null) {
				List<ScheduledTask> list = taskGroup.removeTask(name);
				int sz = list.size();
				for (int i = 0; i < sz; i++) {
					modelEventProperty.set(new GanttChartModelEvent(EModelEventSource.EXTERNAL, EModelEvent.REMOVE,
							list.get(i), null));
				}
			}
		}
	}

	public ScheduledTaskGroup getTaskGroup(final String groupId) {
		return taskGroupMap.get(groupId);
	}

	public ScheduledTaskGroup getTaskGroup(final int index) {
		if (index >= 0 && index < idIndexer.size()) {
			return taskGroupMap.get(idIndexer.get(index));
		}
		return null;
	}

	// To maintain order
	public List<ScheduledTaskGroup> getTaskGroupList() {
		List<ScheduledTaskGroup> list = new ArrayList<>();
		synchronized (lock) {
			for (String s : idIndexer) {
				list.add(taskGroupMap.get(s));
			}
			return list;
		}
	}

	public void addTaskInvalidationListener(final InvalidationListener listener) {
		taskInvalidationListeners.add(listener);
	}

	private void bindTaskListeners(final ObservableList<ScheduledTask> o) {
		for (InvalidationListener il : taskInvalidationListeners) {
			o.addListener(il);
		}
	}

	private void unbindTaskListeners(final ObservableList<ScheduledTask> o) {
		for (InvalidationListener il : taskInvalidationListeners) {
			o.removeListener(il);
		}
	}

	private ChangeListener<ScheduledTask> createTaskChangeListener() {
		return (obj, oldVal, newVal) -> {
			modelEventProperty
					.set(new GanttChartModelEvent(EModelEventSource.INTERNAL, EModelEvent.CHANGE, oldVal, newVal));
		};
	}

	public IGanttChartModelInternal createModelInternal() {
		return new IGanttChartModelInternal() {

			@Override
			public void addTask(String groupId, ZonedDateTime start, ZonedDateTime end) {
				synchronized (lock) {
					ScheduledTaskGroup taskGroup = taskGroupMap.get(groupId);
					if (taskGroup != null) {
						ScheduledTask st = taskGroup.addTask(start, end);
						modelEventProperty
								.set(new GanttChartModelEvent(EModelEventSource.INTERNAL, EModelEvent.ADD, null, st));
					}
				}
			}

			@Override
			public void removeTask(ScheduledTask task) {
				synchronized (lock) {
					ScheduledTaskGroup taskGroup = taskGroupMap.get(task.getGroupName());
					if (taskGroup != null) {
						taskGroup.removeTask(task);
						modelEventProperty.set(
								new GanttChartModelEvent(EModelEventSource.INTERNAL, EModelEvent.REMOVE, task, null));
					}
				}
			}
		};
	}
}

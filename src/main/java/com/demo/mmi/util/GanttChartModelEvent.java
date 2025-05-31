package com.demo.mmi.util;

import com.demo.mmi.entity.ScheduledTask;
import com.demo.mmi.util.GanttChartUtil.EModelEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//@formatter:off
/**
 * Add - only newTask is filled
 * Remove - only oldTask is filled
 * Change - both newTask and oldTask are filled
 */
//@formatter:on
@Getter
@Setter
@Builder
public class GanttChartModelEvent {
	private final EModelEvent modelEvent;
	private final ScheduledTask oldTask;
	private final ScheduledTask newTask;

	@JsonCreator
	public GanttChartModelEvent(
			@JsonProperty("modelEvent") EModelEvent modelEvent,
			@JsonProperty("oldTask") ScheduledTask oldTask,
			@JsonProperty("newTask") ScheduledTask newTask) {
		this.modelEvent = modelEvent;
		this.oldTask = oldTask;
		this.newTask = newTask;
	}

	@Override
	public String toString() {
		return modelEvent + "\nOld - " + oldTask + "\nNew - " + newTask;
	}

}

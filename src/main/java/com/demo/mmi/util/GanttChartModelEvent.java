package com.demo.mmi.util;

import com.demo.common.model.ScheduledTask;
import com.demo.mmi.util.GanttChartUtil.EModelEvent;
import com.demo.mmi.util.GanttChartUtil.EModelEventSource;
import com.demo.openapi.gateway.GatewayInterface;
import com.demo.openapi.model.EHttp;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

//@formatter:off
/**
 * Add - only newTask is filled
 * Remove - only oldTask is filled
 * Change - both newTask and oldTask are filled
 */
//@formatter:on
@Getter
@Setter
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class GanttChartModelEvent {
	private final EModelEventSource eventSource; // internally popualated
	private final EModelEvent modelEvent;
	private final ScheduledTask oldTask;
	private final ScheduledTask newTask;

	@JsonCreator
	public GanttChartModelEvent(
			@JsonProperty("eventSource") EModelEventSource eventSource,
			@JsonProperty("modelEvent") EModelEvent modelEvent,
			@JsonProperty("oldTask") ScheduledTask oldTask,
			@JsonProperty("newTask") ScheduledTask newTask) {
		this.eventSource = eventSource;
		this.modelEvent = modelEvent;
		this.oldTask = oldTask;
		this.newTask = newTask;
	}

	public void processInternalModelEvent(GatewayInterface gateway) {
		switch (modelEvent) {
			case ADD: // call put
				gateway.send(newTask, EHttp.PUT);
				break;
			case CHANGE: // call patch
				sendUpdates(gateway);
				break;
			case REMOVE: // call delete
				gateway.send(oldTask, EHttp.DELETE);
				break;
		}
		log.debug("Internal event: " + modelEvent);
	}

	private void sendUpdates(GatewayInterface gateway) {
		ScheduledTask payload = newTask.duplicate();
		if (newTask.getDuration() != oldTask.getDuration()) {
			payload.setColour(null);
			payload.setName(null);
			gateway.send(payload, EHttp.PATCH);
			return;
		}
		if (newTask.getStartTime() != oldTask.getStartTime()) {
			if (newTask.getDuration() == oldTask.getDuration()) {
				payload.setEndTime(null);
			}
			payload.setColour(null);
			payload.setName(null);
			gateway.send(payload, EHttp.PATCH);
			return;
		}
		if (!newTask.getName().equals(oldTask.getName())) {
			payload.setColour(null);
			payload.setEndTime(null);
			payload.setStartTime(null);
			gateway.send(payload, EHttp.PATCH);
			return;
		}
	}

	@Override
	public String toString() {
		return "[" + eventSource + "] " + modelEvent + "\nOld - " + oldTask + "\nNew - " + newTask;
	}
}

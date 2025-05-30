package com.demo.mmi.util;

import com.demo.mmi.entity.ScheduledTask;
import com.demo.mmi.util.GanttChartUtil.EModelEvent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class GanttChartModelEvent {
	private final EModelEvent modelEvent;
	private final ScheduledTask oldTask;
	private final ScheduledTask newTask;

	@Override
	public String toString() {
		return modelEvent + "\nOld - " + oldTask + "\nNew - " + newTask;
	}
}

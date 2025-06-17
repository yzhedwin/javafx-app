package com.demo.mmi.util;

import java.time.ZonedDateTime;

import com.demo.common.model.ScheduledTask;

public interface IGanttChartModelInternal {

	void addTask(final String groupId, final ZonedDateTime start, final ZonedDateTime end);

	void removeTask(final ScheduledTask task);
}

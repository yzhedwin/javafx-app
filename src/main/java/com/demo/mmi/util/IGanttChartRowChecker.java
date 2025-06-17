package com.demo.mmi.util;

import com.demo.mmi.entity.ScheduledTaskBar;

public interface IGanttChartRowChecker {
	void onTaskEnter(ScheduledTaskBar taskBar);

	void onTaskExit(ScheduledTaskBar taskBar);

	void onTaskDrag(ScheduledTaskBar taskBar, double centreY, double startTimeDelta, double endTimeDelta);

	void onTaskDragEnd(ScheduledTaskBar taskBar);
}

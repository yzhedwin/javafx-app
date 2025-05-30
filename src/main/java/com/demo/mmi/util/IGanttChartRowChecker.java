package com.demo.mmi.util;

import com.demo.mmi.entity.ScheduledTaskBar;

public interface IGanttChartRowChecker {
    void onTaskDrag(double x, double y);

    void onTaskDragEnd(ScheduledTaskBar taskBar);
}

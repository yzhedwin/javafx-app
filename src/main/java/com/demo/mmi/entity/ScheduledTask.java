package com.demo.mmi.entity;

import java.time.Duration;
import java.time.ZonedDateTime;

import com.demo.mmi.util.GanttChartUtil;

import javafx.scene.paint.Color;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ScheduledTask {
	@EqualsAndHashCode.Include
	private final long id;
	private String groupName;
	private String name;
	private ZonedDateTime startTime;
	private ZonedDateTime endTime;
	private Color colour;

	/*
	 * @return seconds between startTime and endTime
	 */
	public long getDuration() {
		return getDuration(startTime, endTime);
	}

	/**
	 * @param start
	 * @return seconds between start and endTime
	 */
	public long getDurationFrom(final ZonedDateTime start) {
		return getDuration(start, endTime);
	}

	/**
	 * @param end
	 * @return seconds between startTime and end
	 */
	public long getDurationTo(final ZonedDateTime end) {
		return getDuration(startTime, end);
	}

	private long getDuration(final ZonedDateTime start, final ZonedDateTime end) {
		return Duration.between(start, end).toSeconds();
	}

	public ScheduledTask duplicate() {
		ScheduledTask st = new ScheduledTask(id);
		st.setGroupName(groupName);
		st.setName(name);
		st.setColour(colour);
		st.setStartTime(startTime);
		st.setEndTime(endTime);
		return st;
	}

	@Override
	public String toString() {
		return "ScheduledTask [" + groupName + " - " + name + "]\n"
				+ startTime.format(GanttChartUtil.DATE_TIME_FORMATTER) + "\nTo\n"
				+ endTime.format(GanttChartUtil.DATE_TIME_FORMATTER);
	}
}

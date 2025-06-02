package com.demo.mmi.entity;

import java.time.Duration;
import java.time.ZonedDateTime;

import com.demo.mmi.util.GanttChartUtil;
import com.demo.mmi.util.SerializableColor;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ScheduledTask {
	@EqualsAndHashCode.Include
	private final long id;
	private String groupName;
	private String name;
	private ZonedDateTime startTime;
	private ZonedDateTime endTime;
	private SerializableColor colour;

	@JsonCreator
	public ScheduledTask(@JsonProperty("id") long id, @JsonProperty("groupName") String groupName,
			@JsonProperty("name") String name, @JsonProperty("startTime") ZonedDateTime startTime,
			@JsonProperty("endTime") ZonedDateTime endTime, @JsonProperty("colour") SerializableColor colour) {
		this.id = id;
		this.groupName = groupName;
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.colour = colour;
	}

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
		ScheduledTask st = ScheduledTask.builder()
				.id(id)
				.groupName(groupName)
				.name(name)
				.colour(colour)
				.startTime(startTime)
				.endTime(endTime)
				.build();
		return st;
	}

	@Override
	public String toString() {
		return "ScheduledTask [" + groupName + " - " + name + "]\n"
				+ startTime.format(GanttChartUtil.DATE_TIME_FORMATTER) + "\nTo\n"
				+ endTime.format(GanttChartUtil.DATE_TIME_FORMATTER);
	}
}

package com.demo.mmi.util;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Getter
@RequiredArgsConstructor
@Log4j2
public class DateTimeStep {
	private final long value;
	private final TimeUnit timeUnit;

	public ZonedDateTime getDateTimeWithOffset(final ZonedDateTime dateTime, final int index) {
		if (timeUnit == TimeUnit.DAYS) {
			return dateTime.plusDays(value * index);
		} else if (timeUnit == TimeUnit.HOURS) {
			return dateTime.plusHours(value * index);
		} else if (timeUnit == TimeUnit.MINUTES) {
			return dateTime.plusMinutes(value * index);
		} else if (timeUnit == TimeUnit.SECONDS) {
			return dateTime.plusSeconds(value * index);
		}
		log.error("TimeUnit {} is not supported for {}", timeUnit, DateTimeStep.class);
		return null;
	}

	public ZonedDateTime getDateTimeWithOffset(final ZonedDateTime dateTime, final Double timeUnitDelta) {
		Long ns = null;
		if (timeUnit == TimeUnit.DAYS) {
			ns = DateTimeUtil.toNanoseconds(value * timeUnitDelta, 0.0, 0.0, 0.0);
		} else if (timeUnit == TimeUnit.HOURS) {
			ns = DateTimeUtil.toNanoseconds(0.0, value * timeUnitDelta, 0.0, 0.0);
		} else if (timeUnit == TimeUnit.MINUTES) {
			ns = DateTimeUtil.toNanoseconds(0.0, 0.0, value * timeUnitDelta, 0.0);
		} else if (timeUnit == TimeUnit.SECONDS) {
			ns = DateTimeUtil.toNanoseconds(0.0, 0.0, 0.0, value * timeUnitDelta);
		}

		if (ns != null) {
			return dateTime.plusNanos(ns);
		}
		log.error("TimeUnit {} is not supported for {}", timeUnit, DateTimeStep.class);
		return null;
	}

	public Double getSecondsRatio(final double seconds) {
		if (timeUnit == TimeUnit.DAYS) {
			return seconds / (value * 24 * 3600);
		} else if (timeUnit == TimeUnit.HOURS) {
			return seconds / (value * 3600);
		} else if (timeUnit == TimeUnit.MINUTES) {
			return seconds / (value * 60);
		} else if (timeUnit == TimeUnit.SECONDS) {
			return seconds / value;
		}
		log.error("TimeUnit {} is not supported for {}", timeUnit, DateTimeStep.class);
		return null;
	}
}

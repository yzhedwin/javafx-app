package com.demo.mmi.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import lombok.extern.log4j.Log4j2;

/**
 * May be replaced by Core
 */
@Log4j2
public final class DateTimeUtil {

    public enum EDateTimeFormat {
        DEFAULT("dd MMM yyyy HH:mm:ss"),
        IRDT("ddHHmm:ss"),
        NO_SECONDS("dd MMM yyyy HH:mm"),
        DATE_ONLY("dd MMM yyyy"),
        TIME_ONLY("HH:mm:ss"),
        NO_YEAR("dd MMM HH:mm"),
        WEEKDAY_COMPRESSED("EEEE\ndd-MM-YY\nHH:mm:ss");

        private DateTimeFormatter formatter;

        private EDateTimeFormat(String format) {
            formatter = DateTimeFormatter.ofPattern(format);
        }

        public DateTimeFormatter getFormatter() {
            return formatter;
        }

        /**
         * You may get Epoch time from Instant (based on system time zone)
         */
        public Instant getInstant(String text) {
            ZonedDateTime zdt = ZonedDateTime.parse(text, formatter);
            return zdt.toInstant().atOffset(OffsetDateTime.now().getOffset()).toInstant();
        }
    }

    public static LocalDateTime toLocalDateTime(long s) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(s), ZoneId.systemDefault());
    }

    public static LocalDateTime toLocalDateTimeSecond(long s) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(s), ZoneId.systemDefault());
    }

    public static long fromLocalDateTime(LocalDateTime ldt) {
        return ldt.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
    }

    public static long fromLocalDateTimeSecond(LocalDateTime ldt) {
        return ldt.toEpochSecond(OffsetDateTime.now().getOffset());
    }

    public static Instant from(short day, short month, int year, short hour, short minute, short second) {
        try {
            String date = String.format("%02d", day) + " "
                    + Month.of(month).toString().substring(0, 3) + " "
                    + String.format("%04d", year);
            String time = String.format("%02d", hour) + ":"
                    + String.format("%02d", minute) + ":"
                    + String.format("%02d", second);

            return EDateTimeFormat.DEFAULT.getInstant(date + " " + time);
        } catch (DateTimeParseException e) {
            log.debug("Problems with parsing date time value : {}", e);
        }
        return null;
    }

    /**
     * Get a formatted string of the current time
     * 
     * @param format - the type of format to be displayed
     * @return formatted time, in string
     */
    public static String formattedNow(final EDateTimeFormat format) {
        return format.getFormatter().format(OffsetDateTime.now());
    }

    /**
     * Current local time
     * 
     * @return epoch seconds
     */
    public static long now() {
        return OffsetDateTime.now().toEpochSecond();
    }

    /**
     * Includes local time zone conversion
     * 
     * @param duration - java.time
     * @return - Instant of local time zone
     */
    public static Instant toInstant(Duration duration) {
        return Instant.ofEpochMilli(duration.toMillis()).atOffset(OffsetDateTime.now().getOffset()).toInstant();
    }

    public static Duration toDuration(Instant instant) {
        return Duration.ofMillis(instant.toEpochMilli());
    }

    /**
     * Smaller than nanoseconds are truncated
     * 
     * @param days
     * @param hours
     * @param minutes
     * @param seconds
     * @return
     */
    public static long toNanoseconds(Double days, Double hours, Double minutes, Double seconds) {
        final long nanoFactor = 1000000000;
        double inSeconds = days * 24 * 3600 + hours * 3600 + minutes * 60 + seconds;
        return Double.valueOf(inSeconds * nanoFactor).longValue();
    }

    private DateTimeUtil() {

    }
}

package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * An immutable end time between 08:30 and 20:00, in 30-minute intervals.
 * Appointment must also check that the end is at least 30 minutes after its start.
 */
public class EndTime {
    public static final String MESSAGE_CONSTRAINTS =
            "End time must be between 08:30 and 20:00 in 30-minute intervals, using HH:mm, "
                    + "and at least 30 minutes after the start time.";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final LocalTime EARLIEST = LocalTime.of(8, 30);
    private static final LocalTime LATEST = LocalTime.of(20, 0);

    public final LocalTime value;

    /**
     * Constructs an end time from an exact HH:mm string.
     */
    public EndTime(String endTime) {
        requireNonNull(endTime);
        if (!isValidEndTime(endTime)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        value = LocalTime.parse(endTime, FORMATTER);
    }

    /**
     * Returns whether the input meets end-time format, working-hour and interval constraints.
     */
    public static boolean isValidEndTime(String test) {
        if (test == null || !test.matches("[0-9]{2}:[0-9]{2}")) {
            return false;
        }
        try {
            LocalTime time = LocalTime.parse(test, FORMATTER);
            return !time.isBefore(EARLIEST) && !time.isAfter(LATEST)
                    && time.getMinute() % 30 == 0;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns whether this end time is at least 30 minutes after the given start on the same day.
     */
    public boolean isAtLeastThirtyMinutesAfter(StartTime startTime) {
        requireNonNull(startTime);
        return Duration.between(startTime.value, value).toMinutes() >= 30;
    }

    @Override
    public String toString() {
        return value.format(FORMATTER);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof EndTime otherTime && value.equals(otherTime.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

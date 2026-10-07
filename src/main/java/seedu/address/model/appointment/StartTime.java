package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * An immutable start time between 08:00 and 19:30, in 30-minute intervals.
 */
public class StartTime {
    public static final String MESSAGE_CONSTRAINTS =
            "Start time must be between 08:00 and 19:30 in 30-minute intervals, using HH:mm.";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final LocalTime EARLIEST = LocalTime.of(8, 0);
    private static final LocalTime LATEST = LocalTime.of(19, 30);

    public final LocalTime value;

    /**
     * Constructs a start time from an exact HH:mm string.
     */
    public StartTime(String startTime) {
        requireNonNull(startTime);
        if (!isValidStartTime(startTime)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        value = LocalTime.parse(startTime, FORMATTER);
    }

    /**
     * Returns whether the input is an allowed start time. Future booking checks require a date too.
     */
    public static boolean isValidStartTime(String test) {
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

    @Override
    public String toString() {
        return value.format(FORMATTER);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof StartTime otherTime && value.equals(otherTime.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

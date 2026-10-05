package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * An immutable appointment date. Past dates are allowed for viewing and loading records.
 * The scheduling command must separately reject dates before today in Singapore.
 */
public class AppointmentDate {
    public static final String MESSAGE_CONSTRAINTS = "Date must be a real DD-MM-YYYY date.";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    public final LocalDate value;

    /**
     * Constructs a date from an exact DD-MM-YYYY string.
     */
    public AppointmentDate(String date) {
        requireNonNull(date);
        if (!isValidDate(date)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        value = LocalDate.parse(date, FORMATTER);
    }

    /**
     * Returns whether the input is a real date with a four-digit positive year.
     */
    public static boolean isValidDate(String test) {
        if (test == null || !test.matches("[0-9]{2}-[0-9]{2}-[0-9]{4}")) {
            return false;
        }
        try {
            return LocalDate.parse(test, FORMATTER).getYear() > 0;
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
                || (other instanceof AppointmentDate otherDate && value.equals(otherDate.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

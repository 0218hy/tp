package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class AppointmentDateTest {
    //@@author 0218hy
    @Test
    public void constructor_validDates_preservesDateAndFormat() {
        // BVA: year limits and the last valid days of February and a 30-day month.
        // Leap-year EPs: divisible by 400 and divisible by 4 but not 100.
        String[] dates = {"01-01-0001", "29-02-2000", "29-02-2024", "31-12-9999",
            "28-02-2025", "30-04-2026"};
        LocalDate[] expected = {LocalDate.of(1, 1, 1), LocalDate.of(2000, 2, 29),
            LocalDate.of(2024, 2, 29), LocalDate.of(9999, 12, 31),
            LocalDate.of(2025, 2, 28), LocalDate.of(2026, 4, 30)};
        for (int i = 0; i < dates.length; i++) {
            assertTrue(AppointmentDate.isValidDate(dates[i]), dates[i]);
            AppointmentDate date = new AppointmentDate(dates[i]);
            assertEquals(expected[i], date.value);
            assertEquals(dates[i], date.toString());
        }
    }

    @Test
    public void constructor_pastDate_allowsHistoricalRecords() {
        // Valid EP: an ordinary historical date; rejecting past bookings belongs to commands, not this value type.
        assertEquals(LocalDate.of(2000, 6, 15), new AppointmentDate("15-06-2000").value);
    }

    @Test
    public void constructor_invalidDate_rejectsInvalidCalendarDatesAndFormats() {
        String[] invalidDates = {
            // EP: malformed DD-MM-YYYY (width, order, separator, whitespace, or nonnumeric components).
            "", " ", "1-01-2026", "01-1-2026", "01-01-26", "2026-01-01",
            "01/01/2026", "01-01-2026 ", " 01-01-2026", "aa-bb-cccc",
            // BVA: first invalid day/month values; April must not roll forward to May.
            "00-01-2026", "32-01-2026", "31-04-2026", "01-00-2026", "01-13-2026",
            // EP: common year versus century not divisible by 400. Both must reject February 29.
            "29-02-2025", "29-02-1900",
            // BVA: immediately outside the supported positive four-digit year range.
            "01-01-0000", "01-01-10000"
        };
        for (String input : invalidDates) {
            assertFalse(AppointmentDate.isValidDate(input), input);
            IllegalArgumentException error = assertThrows(
                    IllegalArgumentException.class, () -> new AppointmentDate(input), input);
            assertEquals(AppointmentDate.MESSAGE_CONSTRAINTS, error.getMessage());
        }
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertFalse(AppointmentDate.isValidDate(null));
        assertThrows(NullPointerException.class, () -> new AppointmentDate(null));
    }

    @Test
    public void equalsAndHashCode_compareDateValues() {
        AppointmentDate date = new AppointmentDate("18-09-2026");
        AppointmentDate copy = new AppointmentDate("18-09-2026");
        assertTrue(date.equals(date));
        assertTrue(date.equals(copy));
        assertTrue(copy.equals(date));
        assertEquals(date.hashCode(), copy.hashCode());
        assertFalse(date.equals(new AppointmentDate("19-09-2026")));
        assertFalse(date.equals(null));
        assertFalse(date.equals("18-09-2026"));
    }
    //@@author
}

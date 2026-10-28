package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

public class StartTimeTest {
    //@@author 0218hy
    @Test
    public void constructor_validTimes_acceptsBoundariesAndHalfHours() {
        // Valid EP: working-hour times on the half-hour grid. BVA: inclusive lower/upper limits.
        // Interior representatives exercise both :00 and :30, rather than every valid slot.
        String[] inputs = {"08:00", "08:30", "12:00", "19:30"};
        LocalTime[] expected = {LocalTime.of(8, 0), LocalTime.of(8, 30),
            LocalTime.of(12, 0), LocalTime.of(19, 30)};
        for (int i = 0; i < inputs.length; i++) {
            assertTrue(StartTime.isValidStartTime(inputs[i]), inputs[i]);
            StartTime time = new StartTime(inputs[i]);
            assertEquals(expected[i], time.value);
            assertEquals(inputs[i], time.toString());
        }
    }

    @Test
    public void constructor_invalidTimes_rejectsFormatsAndOutOfRangeValues() {
        String[] invalidTimes = {
            // EP: missing/malformed HH:mm; each form targets a different formatting rule.
            "", " ", "8:00", "08:0", "08.00", "08:00:00", "08:00 ", " 08:00", "aa:bb",
            // EP: outside working hours. Nearest half-hour slots isolate the range rule from the grid rule.
            "07:30", "20:00",
            // EP: within working hours but off-grid. BVA: neighbours of :00 and :30; 15 is an interior value.
            "12:01", "12:15", "12:29", "12:31", "12:59",
            // EP: correctly shaped strings with invalid clock components (first invalid hour/minute).
            "24:00", "12:60"
        };
        for (String input : invalidTimes) {
            assertFalse(StartTime.isValidStartTime(input), input);
            IllegalArgumentException error = assertThrows(
                    IllegalArgumentException.class, () -> new StartTime(input), input);
            assertEquals(StartTime.MESSAGE_CONSTRAINTS, error.getMessage());
        }
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        // Null is a separate EP: the validator returns false, while construction rejects a missing reference.
        assertFalse(StartTime.isValidStartTime(null));
        assertThrows(NullPointerException.class, () -> new StartTime(null));
    }

    @Test
    public void equalsAndHashCode_compareTimeValues() {
        StartTime time = new StartTime("10:30");
        StartTime copy = new StartTime("10:30");
        assertTrue(time.equals(time));
        assertTrue(time.equals(copy));
        assertTrue(copy.equals(time));
        assertEquals(time.hashCode(), copy.hashCode());
        assertFalse(time.equals(new StartTime("11:00")));
        assertFalse(time.equals(null));
        assertFalse(time.equals(new EndTime("10:30")));
    }
    //@@author
}

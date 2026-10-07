package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

public class StartTimeTest {
    @Test
    public void constructor_validTimes_acceptsBoundariesAndHalfHours() {
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
        String[] invalidTimes = {"", " ", "8:00", "08:0", "08.00", "08:00:00", "08:00 ",
            " 08:00", "08:00 extra", "aa:bb", "07:30", "07:59", "19:31", "20:00",
            "08:01", "08:15", "08:29", "08:31", "12:59", "24:00", "12:60"};
        for (String input : invalidTimes) {
            assertFalse(StartTime.isValidStartTime(input), input);
            IllegalArgumentException error = assertThrows(
                    IllegalArgumentException.class, () -> new StartTime(input), input);
            assertEquals(StartTime.MESSAGE_CONSTRAINTS, error.getMessage());
        }
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
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
}

package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

public class EndTimeTest {
    @Test
    public void constructor_validTimes_acceptsBoundariesAndHalfHours() {
        String[] inputs = {"08:30", "09:00", "12:30", "20:00"};
        LocalTime[] expected = {LocalTime.of(8, 30), LocalTime.of(9, 0),
            LocalTime.of(12, 30), LocalTime.of(20, 0)};
        for (int i = 0; i < inputs.length; i++) {
            assertTrue(EndTime.isValidEndTime(inputs[i]), inputs[i]);
            EndTime time = new EndTime(inputs[i]);
            assertEquals(expected[i], time.value);
            assertEquals(inputs[i], time.toString());
        }
    }

    @Test
    public void constructor_invalidTimes_rejectsFormatsAndOutOfRangeValues() {
        String[] invalidTimes = {"", " ", "8:30", "08:3", "08.30", "08:30:00", "08:30 ",
            " 08:30", "08:30 extra", "aa:bb", "08:00", "08:29", "20:01", "20:30",
            "08:31", "09:15", "09:29", "12:59", "24:00", "12:60"};
        for (String input : invalidTimes) {
            assertFalse(EndTime.isValidEndTime(input), input);
            IllegalArgumentException error = assertThrows(
                    IllegalArgumentException.class, () -> new EndTime(input), input);
            assertEquals(EndTime.MESSAGE_CONSTRAINTS, error.getMessage());
        }
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertFalse(EndTime.isValidEndTime(null));
        assertThrows(NullPointerException.class, () -> new EndTime(null));
    }

    @Test
    public void minimumDuration_requiresThirtyMinutesOnSameDay() {
        EndTime end = new EndTime("12:00");
        assertTrue(end.isAtLeastThirtyMinutesAfter(new StartTime("11:30")));
        assertTrue(end.isAtLeastThirtyMinutesAfter(new StartTime("08:00")));
        assertFalse(end.isAtLeastThirtyMinutesAfter(new StartTime("12:00")));
        assertFalse(end.isAtLeastThirtyMinutesAfter(new StartTime("12:30")));
        assertTrue(new EndTime("08:30").isAtLeastThirtyMinutesAfter(new StartTime("08:00")));
        assertTrue(new EndTime("20:00").isAtLeastThirtyMinutesAfter(new StartTime("19:30")));
        assertFalse(new EndTime("08:30").isAtLeastThirtyMinutesAfter(new StartTime("19:30")));
        assertThrows(NullPointerException.class, () -> end.isAtLeastThirtyMinutesAfter(null));
    }

    @Test
    public void equalsAndHashCode_compareTimeValues() {
        EndTime time = new EndTime("12:00");
        EndTime copy = new EndTime("12:00");
        assertTrue(time.equals(time));
        assertTrue(time.equals(copy));
        assertTrue(copy.equals(time));
        assertEquals(time.hashCode(), copy.hashCode());
        assertFalse(time.equals(new EndTime("12:30")));
        assertFalse(time.equals(null));
        assertFalse(time.equals(new StartTime("12:00")));
    }
}

package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.client.Phone;

public class AppointmentListTest {
    private static final Appointment FIRST = create("10:00", "11:00");
    private static final Appointment SECOND = create("11:00", "12:00");

    @Test
    public void equalsHashCodeAndToString_compareAppointmentContents() {
        AppointmentList appointments = new AppointmentList();
        appointments.setAppointments(List.of(FIRST, SECOND));
        AppointmentList copy = new AppointmentList();
        copy.setAppointments(List.of(FIRST, SECOND));

        assertTrue(appointments.equals(appointments));
        assertTrue(appointments.equals(copy));
        assertEquals(appointments.hashCode(), copy.hashCode());
        assertEquals(appointments.toString(), copy.toString());
        assertFalse(appointments.equals(null));
        assertFalse(appointments.equals("appointments"));
        assertNotEquals(appointments, new AppointmentList());
    }

    private static Appointment create(String start, String end) {
        return new Appointment(new Phone("91234567"), "BuBu", new AppointmentDate("01-01-2020"),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }
}

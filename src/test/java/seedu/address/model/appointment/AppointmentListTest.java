package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.testutil.ClientBuilder;

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

    @Test
    public void updateOwner_updatesMatchingAppointmentsAndPreservesOthers() {
        AppointmentList appointments = new AppointmentList();
        Appointment currentOwnerAppointment = create("10:00", "11:00");
        Appointment otherOwnerAppointment = createAppointment(new Phone("81234567"), "Mochi",
                new AppointmentDate("01-01-2020"), new StartTime("11:00"), new EndTime("12:00"),
                Service.NAIL_TRIM);
        appointments.setAppointments(List.of(currentOwnerAppointment, otherOwnerAppointment));
        Client currentOwner = new ClientBuilder().withPhone("91234567").build();
        Client editedOwner = new ClientBuilder(currentOwner).withPhone("99999999").build();

        appointments.updateOwner(currentOwner, editedOwner);

        assertEquals(new Phone("99999999"), appointments.getAppointmentList().get(0).getOwnerPhone());
        assertEquals(new Phone("81234567"), appointments.getAppointmentList().get(1).getOwnerPhone());
    }

    private static Appointment create(String start, String end) {
        return createAppointment(new Phone("91234567"), "BuBu", new AppointmentDate("01-01-2020"),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }
}

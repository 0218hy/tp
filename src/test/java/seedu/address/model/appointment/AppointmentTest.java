package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Phone;

public class AppointmentTest {
    private static final Phone OWNER_PHONE = new Phone("91234567");
    private static final AppointmentDate DATE = new AppointmentDate("18-09-2026");

    @Test
    public void constructor_validAppointment_preservesFields() {
        Appointment appointment = create("10:30", "12:00");
        assertEquals(OWNER_PHONE, appointment.getOwnerPhone());
        assertEquals("BuBu", appointment.getPetName());
        assertEquals(DATE, appointment.getDate());
        assertEquals(new StartTime("10:30"), appointment.getStartTime());
        assertEquals(new EndTime("12:00"), appointment.getEndTime());
        assertEquals(Service.FULL_GROOM, appointment.getService());
        assertEquals(new EndTime("20:00"), create("19:30", "20:00").getEndTime());
    }

    @Test
    public void constructor_invalidDuration_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> create("10:30", "10:30"));
        assertThrows(IllegalArgumentException.class, () -> create("10:30", "10:00"));
    }

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        StartTime start = new StartTime("10:30");
        EndTime end = new EndTime("12:00");
        Service service = Service.FULL_GROOM;
        assertThrows(NullPointerException.class, () -> new Appointment(null, "BuBu", DATE, start, end, service));
        assertThrows(NullPointerException.class, () -> new Appointment(OWNER_PHONE, null, DATE, start, end, service));
        assertThrows(NullPointerException.class, () -> new Appointment(
                OWNER_PHONE, "BuBu", null, start, end, service));
        assertThrows(NullPointerException.class, () -> new Appointment(OWNER_PHONE, "BuBu", DATE, null, end, service));
        assertThrows(NullPointerException.class, () -> new Appointment(
                OWNER_PHONE, "BuBu", DATE, start, null, service));
        assertThrows(NullPointerException.class, () -> new Appointment(OWNER_PHONE, "BuBu", DATE, start, end, null));
    }

    @Test
    public void constructor_petName_normalisesWhitespaceAndValidates() {
        Appointment appointment = new Appointment(OWNER_PHONE, "  BuBu   Junior  ", DATE,
                new StartTime("10:30"), new EndTime("12:00"), Service.FULL_GROOM);
        assertEquals("BuBu Junior", appointment.getPetName());
        for (String name : new String[]{"", "   ", "Bu/Bu", "a".repeat(41)}) {
            assertThrows(IllegalArgumentException.class, () -> new Appointment(OWNER_PHONE, name, DATE,
                    new StartTime("10:30"), new EndTime("12:00"), Service.FULL_GROOM));
        }
    }

    @Test
    public void overlaps_intersectionsDetectedAndAdjacentSlotsAllowed() {
        Appointment appointment = create("10:30", "12:00");
        for (Appointment other : new Appointment[]{create("10:30", "12:00"), create("10:00", "11:00"),
                create("11:30", "12:30"), create("10:00", "12:30"), create("11:00", "11:30")}) {
            assertTrue(appointment.overlaps(other));
            assertTrue(other.overlaps(appointment));
        }
        for (Appointment other : new Appointment[]{create("09:00", "10:30"), create("12:00", "13:30")}) {
            assertFalse(appointment.overlaps(other));
            assertFalse(other.overlaps(appointment));
        }
        Appointment differentOwner = new Appointment(new Phone("81234567"), "Mochi", DATE,
                new StartTime("11:00"), new EndTime("12:30"), Service.NAIL_TRIM);
        assertTrue(appointment.overlaps(differentOwner));
        Appointment nextDay = new Appointment(OWNER_PHONE, "BuBu", new AppointmentDate("19-09-2026"),
                new StartTime("10:30"), new EndTime("12:00"), Service.FULL_GROOM);
        assertFalse(appointment.overlaps(nextDay));
    }

    @Test
    public void matches_includesStartAndInteriorButExcludesEnd() {
        Appointment appointment = create("10:30", "12:00");
        assertTrue(appointment.matches(DATE, LocalTime.of(10, 30)));
        assertTrue(appointment.matches(DATE, LocalTime.of(10, 50)));
        assertFalse(appointment.matches(DATE, LocalTime.of(10, 29)));
        assertFalse(appointment.matches(DATE, LocalTime.of(12, 0)));
        assertFalse(appointment.matches(new AppointmentDate("19-09-2026"), LocalTime.of(10, 30)));
        assertTrue(create("12:00", "13:30").matches(DATE, LocalTime.of(12, 0)));
    }

    @Test
    public void equalsAndIdentity_distinguishSlotFromFullDetails() {
        Appointment appointment = create("10:30", "12:00");
        Appointment copy = create("10:30", "12:00");
        assertEquals(appointment, copy);
        assertEquals(appointment.hashCode(), copy.hashCode());
        assertTrue(appointment.isSameAppointment(create("10:30", "13:00")));
        assertNotEquals(appointment, create("10:30", "13:00"));
        assertFalse(appointment.isSameAppointment(create("11:00", "12:00")));
        assertFalse(appointment.isSameAppointment(null));
        assertNotEquals(null, appointment);
        Appointment otherService = new Appointment(OWNER_PHONE, "BuBu", DATE,
                new StartTime("10:30"), new EndTime("12:00"), Service.OTHER);
        assertTrue(appointment.isSameAppointment(otherService));
        assertNotEquals(appointment, otherService);
    }

    private Appointment create(String start, String end) {
        return new Appointment(OWNER_PHONE, "BuBu", DATE,
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }
}

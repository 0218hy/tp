package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.model.appointment.exceptions.OverlappingAppointmentException;
import seedu.address.model.person.Phone;

public class AppointmentBookTest {
    private static final Appointment APPOINTMENT = create("10:00", "11:00");
    private static final Appointment ADJACENT = create("11:00", "11:30");

    private final AppointmentBook book = new AppointmentBook();

    @Test
    public void constructor_emptyBook_hasNoAppointments() {
        assertTrue(book.getAppointmentList().isEmpty());
        assertFalse(book.hasOverlappingAppointment(APPOINTMENT));
    }

    @Test
    public void constructor_copy_doesNotShareMutableList() {
        book.addAppointment(APPOINTMENT);
        AppointmentBook copy = new AppointmentBook(book);
        book.addAppointment(ADJACENT);
        assertEquals(List.of(APPOINTMENT), copy.getAppointmentList());
        copy.setAppointments(List.of());
        assertEquals(List.of(APPOINTMENT, ADJACENT), book.getAppointmentList());
    }

    @Test
    public void addAppointment_historicalAndAdjacentAppointments_preservesInsertionOrder() {
        book.addAppointment(ADJACENT);
        book.addAppointment(APPOINTMENT);
        Appointment earlier = create("09:30", "10:00");
        book.addAppointment(earlier);
        assertEquals(List.of(ADJACENT, APPOINTMENT, earlier), book.getAppointmentList());
    }

    @Test
    public void addAppointment_duplicateOrOverlappingSlot_rejectedWithoutMutation() {
        book.addAppointment(APPOINTMENT);
        for (Appointment overlap : List.of(APPOINTMENT, create("10:00", "11:00"),
                create("09:30", "10:30"), create("10:30", "11:30"),
                create("09:30", "11:30"), create("10:00", "10:30"))) {
            assertTrue(book.hasOverlappingAppointment(overlap));
            assertThrows(OverlappingAppointmentException.class, () -> book.addAppointment(overlap));
            assertEquals(List.of(APPOINTMENT), book.getAppointmentList());
        }
    }

    @Test
    public void addAppointment_differentOwnerAndPet_stillRejectsOverlap() {
        book.addAppointment(APPOINTMENT);
        Appointment otherPet = new Appointment(new Phone("81234567"), "Mochi", APPOINTMENT.getDate(),
                new StartTime("10:30"), new EndTime("11:30"), Service.NAIL_TRIM);
        assertThrows(OverlappingAppointmentException.class, () -> book.addAppointment(otherPet));
        assertEquals(List.of(APPOINTMENT), book.getAppointmentList());
    }

    @Test
    public void addAppointment_sameTimeDifferentDay_allowed() {
        book.addAppointment(APPOINTMENT);
        Appointment nextDay = new Appointment(APPOINTMENT.getOwnerPhone(), "BuBu", new AppointmentDate("02-01-2020"),
                APPOINTMENT.getStartTime(), APPOINTMENT.getEndTime(), Service.FULL_GROOM);
        assertFalse(book.hasOverlappingAppointment(nextDay));
        assertFalse(book.hasOverlappingAppointment(ADJACENT));
        book.addAppointment(nextDay);
        assertEquals(List.of(APPOINTMENT, nextDay), book.getAppointmentList());
    }

    @Test
    public void nullArguments_rejectedWithoutMutation() {
        book.addAppointment(APPOINTMENT);
        assertThrows(NullPointerException.class, () -> new AppointmentBook(null));
        assertThrows(NullPointerException.class, () -> book.addAppointment(null));
        assertThrows(NullPointerException.class, () -> book.hasOverlappingAppointment(null));
        assertThrows(NullPointerException.class, () -> book.setAppointments(null));
        assertThrows(NullPointerException.class, () -> book.resetData(null));
        assertEquals(List.of(APPOINTMENT), book.getAppointmentList());
    }

    @Test
    public void setAppointments_validReplacement_copiesAndReplacesList() {
        book.addAppointment(APPOINTMENT);
        List<Appointment> replacement = new ArrayList<>(List.of(ADJACENT));
        book.setAppointments(replacement);
        replacement.clear();
        assertEquals(List.of(ADJACENT), book.getAppointmentList());
        book.setAppointments(List.of());
        assertTrue(book.getAppointmentList().isEmpty());
    }

    @Test
    public void setAppointments_invalidReplacement_preservesExistingAppointments() {
        book.addAppointment(ADJACENT);
        assertThrows(OverlappingAppointmentException.class, () ->
                book.setAppointments(List.of(APPOINTMENT, create("10:30", "11:30"))));
        assertThrows(NullPointerException.class, () -> book.setAppointments(Arrays.asList(APPOINTMENT, null)));
        assertEquals(List.of(ADJACENT), book.getAppointmentList());
    }

    @Test
    public void resetData_validBookAndSelf_preservesData() {
        AppointmentBook source = new AppointmentBook();
        source.addAppointment(APPOINTMENT);
        book.addAppointment(ADJACENT);
        book.resetData(source);
        assertEquals(source, book);
        book.resetData(book);
        assertEquals(source, book);
        book.setAppointments(book.getAppointmentList());
        assertEquals(source, book);
        source.setAppointments(List.of());
        assertEquals(List.of(APPOINTMENT), book.getAppointmentList());
    }

    @Test
    public void getAppointmentList_unmodifiableView_observesChanges() {
        ObservableList<Appointment> view = book.getAppointmentList();
        assertThrows(UnsupportedOperationException.class, () -> view.add(APPOINTMENT));
        book.addAppointment(APPOINTMENT);
        assertEquals(List.of(APPOINTMENT), view);
        assertThrows(UnsupportedOperationException.class, () -> view.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> view.set(0, ADJACENT));
        book.setAppointments(List.of(ADJACENT));
        assertEquals(List.of(ADJACENT), view);
    }

    @Test
    public void equalsAndHashCode_compareAppointmentsAndTheirOrder() {
        book.setAppointments(List.of(APPOINTMENT, ADJACENT));
        AppointmentBook copy = new AppointmentBook(book);
        assertEquals(book, book);
        assertEquals(book, copy);
        assertEquals(book.hashCode(), copy.hashCode());
        assertNotEquals(book, null);
        assertNotEquals(book, "appointment book");
        assertNotEquals(book, new AppointmentBook());
        copy.setAppointments(List.of(ADJACENT, APPOINTMENT));
        assertNotEquals(book, copy);
    }

    private static Appointment create(String start, String end) {
        return new Appointment(new Phone("91234567"), "BuBu", new AppointmentDate("01-01-2020"),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }
}

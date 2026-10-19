package seedu.address.logic.commands.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.model.ModelManager;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Phone;

public class ListAppointmentsCommandTest {
    // 10:00 in Singapore, although the supplied clock uses UTC.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-07T02:00:00Z"), ZoneOffset.UTC);
    private final ModelManager model = new ModelManager();

    @Test
    public void execute_unsortedSchedule_listsOngoingAndFutureChronologicallyWithoutMutation() {
        Appointment tomorrow = create("08-10-2026", "08:00", "09:00");
        Appointment later = create("07-10-2026", "12:00", "13:00");
        Appointment ongoing = create("07-10-2026", "09:30", "10:30");
        Appointment nextMonth = create("01-11-2026", "08:00", "09:00");
        Appointment completed = create("06-10-2026", "12:00", "13:00");
        List<Appointment> saved = List.of(nextMonth, tomorrow, later, completed, ongoing);
        model.setAppointments(saved);

        CommandResult result = new ListAppointmentsCommand(CLOCK).execute(model);
        assertEquals("Ongoing and future appointments\n"
                + "07-10-2026 09:30-10:30; Pet: BuBu; Owner phone: 91234567; Service: Full groom\n"
                + "07-10-2026 12:00-13:00; Pet: BuBu; Owner phone: 91234567; Service: Full groom\n"
                + "08-10-2026 08:00-09:00; Pet: BuBu; Owner phone: 91234567; Service: Full groom\n"
                + "01-11-2026 08:00-09:00; Pet: BuBu; Owner phone: 91234567; Service: Full groom",
                result.getFeedbackToUser());
        assertEquals(CommandResult.DisplayType.APPOINTMENTS, result.getDisplayType());
        assertEquals(saved, model.getBubuBook().getAppointmentList());
        assertEquals(List.of(ongoing, later, tomorrow, nextMonth), model.getFilteredAppointmentList());
    }

    @Test
    public void execute_exactBoundaries_excludesEndingNowAndIncludesStartingNow() {
        model.setAppointments(List.of(create("07-10-2026", "09:00", "10:00"),
                create("07-10-2026", "10:00", "11:00")));

        assertEquals("Ongoing and future appointments\n"
                + "07-10-2026 10:00-11:00; Pet: BuBu; Owner phone: 91234567; Service: Full groom",
                new ListAppointmentsCommand(CLOCK).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_emptyOrCompletedSchedule_returnsEmptyMessage() {
        ListAppointmentsCommand command = new ListAppointmentsCommand(CLOCK);
        CommandResult emptyResult = command.execute(model);
        assertEquals(ListAppointmentsCommand.MESSAGE_NO_APPOINTMENTS, emptyResult.getFeedbackToUser());
        assertEquals(CommandResult.DisplayType.APPOINTMENTS, emptyResult.getDisplayType());
        model.setAppointments(List.of(create("06-10-2026", "19:00", "20:00"),
                create("07-10-2026", "08:00", "09:00")));
        CommandResult completedResult = command.execute(model);
        assertEquals(ListAppointmentsCommand.MESSAGE_NO_APPOINTMENTS, completedResult.getFeedbackToUser());
        assertEquals(CommandResult.DisplayType.APPOINTMENTS, completedResult.getDisplayType());
        assertEquals(2, model.getBubuBook().getAppointmentList().size());
        assertEquals(List.of(), model.getFilteredAppointmentList());
    }

    @Test
    public void execute_singaporeDateAheadOfUtc_usesSingaporeDate() {
        Clock midnight = Clock.fixed(Instant.parse("2026-10-07T16:00:00Z"), ZoneOffset.UTC);
        model.setAppointments(List.of(create("07-10-2026", "19:00", "20:00"),
                create("08-10-2026", "08:00", "09:00")));

        assertEquals("Ongoing and future appointments\n"
                + "08-10-2026 08:00-09:00; Pet: BuBu; Owner phone: 91234567; Service: Full groom",
                new ListAppointmentsCommand(midnight).execute(model).getFeedbackToUser());
    }

    @Test
    public void constructorAndExecute_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListAppointmentsCommand(null));
        assertThrows(NullPointerException.class, () -> new ListAppointmentsCommand(CLOCK).execute(null));
    }

    private static Appointment create(String date, String start, String end) {
        return new Appointment(new Phone("91234567"), "BuBu", new AppointmentDate(date),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }
}

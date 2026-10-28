package seedu.address.logic.commands.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;

import java.time.Clock;
import java.time.Duration;
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
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

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
        assertEquals("Ongoing or future appointments found: 4.",
                result.getFeedbackToUser());
        assertEquals(CommandResult.DisplayType.APPOINTMENTS, result.getDisplayType());
        assertEquals(saved, model.getBubuBook().getAppointmentList());
        assertEquals(List.of(ongoing, later, tomorrow, nextMonth), model.getFilteredAppointmentList());
    }

    //@@author 0218hy
    @Test
    public void execute_exactBoundaries_excludesEndingNowAndIncludesStartingNow() {
        // BVA: end == now is completed; start == now is ongoing. Assert identity, not just the count.
        Appointment endingNow = create("07-10-2026", "09:00", "10:00");
        Appointment startingNow = create("07-10-2026", "10:00", "11:00");
        model.setAppointments(List.of(endingNow, startingNow));

        assertEquals("Ongoing or future appointments found: 1.",
                new ListAppointmentsCommand(CLOCK).execute(model).getFeedbackToUser());
        assertEquals(List.of(startingNow), model.getFilteredAppointmentList());
        assertEquals(List.of(endingNow, startingNow), model.getBubuBook().getAppointmentList());
    }

    @Test
    public void execute_aroundEndBoundary_excludesAppointmentOnlyOnceItEnds() {
        Appointment appointment = create("07-10-2026", "09:00", "10:00");
        model.setAppointments(List.of(appointment));
        // BVA: one second before/after the end. The exact end is covered above.
        Clock beforeEnd = Clock.offset(CLOCK, Duration.ofSeconds(-1));
        new ListAppointmentsCommand(beforeEnd).execute(model);
        assertEquals(List.of(appointment), model.getFilteredAppointmentList());
        Clock afterEnd = Clock.offset(CLOCK, Duration.ofSeconds(1));
        new ListAppointmentsCommand(afterEnd).execute(model);
        assertEquals(List.of(), model.getFilteredAppointmentList());
        assertEquals(List.of(appointment), model.getBubuBook().getAppointmentList());
    }
    //@@author

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

    //@@author 0218hy
    @Test
    public void execute_singaporeDateAheadOfUtc_usesSingaporeDate() {
        // Clock-state EP: UTC and Singapore have different dates (catches reliance on the clock's own zone).
        Clock midnight = Clock.fixed(Instant.parse("2026-10-07T16:00:00Z"), ZoneOffset.UTC);
        Appointment nextDay = create("08-10-2026", "08:00", "09:00");
        model.setAppointments(List.of(create("07-10-2026", "19:00", "20:00"),
                nextDay));

        assertEquals("Ongoing or future appointments found: 1.",
                new ListAppointmentsCommand(midnight).execute(model).getFeedbackToUser());
        assertEquals(List.of(nextDay), model.getFilteredAppointmentList());
    }

    @Test
    public void execute_participantsRemoved_excludesRelatedAppointments() {
        Client owner = new ClientBuilder().withName("Jane Tan").withPhone("81234567")
                .withAddress("123 Clementi Road, #04-56").build();
        Pet pet = new PetBuilder(owner).withName("Mochi")
                .withRequirement("Sensitive skin; use fragrance-free shampoo and avoid the ears").build();
        Appointment appointment = new Appointment(owner, pet, new AppointmentDate("08-10-2026"),
                new StartTime("10:00"), new EndTime("11:00"), Service.NAIL_TRIM);
        model.addClient(owner);
        model.addPet(pet);
        model.addAppointment(appointment);
        model.deletePet(pet);
        model.deleteClient(owner);

        CommandResult result = new ListAppointmentsCommand(CLOCK).execute(model);

        assertEquals(ListAppointmentsCommand.MESSAGE_NO_APPOINTMENTS, result.getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredAppointmentList());
    }

    @Test
    public void execute_dateFilter_listsEntireDayChronologicallyWithoutMutation() {
        Appointment completed = create("07-10-2026", "08:00", "09:00");
        Appointment endingNow = create("07-10-2026", "09:00", "10:00");
        Appointment ongoing = create("07-10-2026", "10:00", "11:00");
        Appointment later = create("07-10-2026", "12:00", "13:00");
        Appointment yesterday = create("06-10-2026", "12:00", "13:00");
        Appointment tomorrow = create("08-10-2026", "12:00", "13:00");
        List<Appointment> saved = List.of(later, tomorrow, endingNow, yesterday, ongoing, completed);
        model.setAppointments(saved);
        new ListAppointmentsCommand(CLOCK).execute(model);

        CommandResult result = new ListAppointmentsCommand(new AppointmentDate("07-10-2026"), CLOCK).execute(model);

        assertEquals("Appointments on 07-10-2026 found: 4.", result.getFeedbackToUser());
        assertEquals(CommandResult.DisplayType.APPOINTMENTS, result.getDisplayType());
        assertEquals(List.of(completed, endingNow, ongoing, later), model.getFilteredAppointmentList());
        assertEquals(saved, model.getBubuBook().getAppointmentList());

        new ListAppointmentsCommand(CLOCK).execute(model);
        assertEquals(List.of(ongoing, later, tomorrow), model.getFilteredAppointmentList());
        assertEquals(saved, model.getBubuBook().getAppointmentList());
    }

    @Test
    public void execute_pastAndFutureDateFilters_showOnlyMatchingDate() {
        Appointment past = create("06-10-2026", "12:00", "13:00");
        Appointment future = create("08-10-2026", "12:00", "13:00");
        model.setAppointments(List.of(past, future));

        new ListAppointmentsCommand(new AppointmentDate("06-10-2026"), CLOCK).execute(model);
        assertEquals(List.of(past), model.getFilteredAppointmentList());

        new ListAppointmentsCommand(new AppointmentDate("08-10-2026"), CLOCK).execute(model);
        assertEquals(List.of(future), model.getFilteredAppointmentList());
    }

    @Test
    public void execute_dateWithoutAppointments_returnsDateSpecificEmptyMessage() {
        model.setAppointments(List.of(create("07-10-2026", "12:00", "13:00")));

        CommandResult result = new ListAppointmentsCommand(new AppointmentDate("06-10-2026"), CLOCK).execute(model);

        assertEquals("No appointments on 06-10-2026.", result.getFeedbackToUser());
        assertEquals(CommandResult.DisplayType.APPOINTMENTS, result.getDisplayType());
        assertEquals(List.of(), model.getFilteredAppointmentList());
        assertEquals(1, model.getBubuBook().getAppointmentList().size());
    }

    @Test
    public void constructorAndExecute_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListAppointmentsCommand((Clock) null));
        assertThrows(NullPointerException.class, () -> new ListAppointmentsCommand((AppointmentDate) null));
        assertThrows(NullPointerException.class, () -> new ListAppointmentsCommand(CLOCK).execute(null));
    }

    private static Appointment create(String date, String start, String end) {
        return createAppointment(new Phone("91234567"), "BuBu", new AppointmentDate(date),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }
    //@@author
}

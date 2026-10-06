package seedu.address.logic.commands.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentBook;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentParticipantLookup;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.appointment.exceptions.OverlappingAppointmentException;
import seedu.address.model.person.Phone;

public class ScheduleCommandTest {
    private static final Phone OWNER_PHONE = new Phone("91234567");
    // 10:00 on 7 October in Singapore, even though the supplied clock uses UTC.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-07T02:00:00Z"), ZoneOffset.UTC);
    private static final Appointment VALID_APPOINTMENT = create("07-10-2026", "10:30", "11:00");

    private final ModelManager model = new ModelManager();
    private final ParticipantLookupStub lookup = new ParticipantLookupStub();

    @Test
    public void execute_validAppointment_addsToModelAndReturnsDetails() throws Exception {
        CommandResult result = command(VALID_APPOINTMENT).execute(model);
        assertEquals(List.of(VALID_APPOINTMENT), model.getAppointmentBook().getAppointmentList());
        assertEquals("Appointment scheduled: BuBu; Owner phone: 91234567; Date: 07-10-2026; "
                + "Time: 10:30-11:00; Service: Full groom", result.getFeedbackToUser());
        assertFalse(result.isExit());
        assertFalse(result.isShowHelp());
        assertEquals(OWNER_PHONE, lookup.queriedOwnerPhone);
        assertEquals(OWNER_PHONE, lookup.queriedPetOwnerPhone);
        assertEquals("BuBu", lookup.queriedPetName);
    }

    @Test
    public void execute_ownerMissingOrNotUnique_rejectsBeforePetLookup() {
        lookup.ownerExists = false;
        assertFailure(VALID_APPOINTMENT, CLOCK, ScheduleCommand.MESSAGE_OWNER_NOT_FOUND);
        assertEquals(0, lookup.petLookupCalls);
    }

    @Test
    public void execute_petNotRegisteredForOwner_noMutation() {
        lookup.petBelongsToOwner = false;
        assertFailure(VALID_APPOINTMENT, CLOCK, ScheduleCommand.MESSAGE_PET_NOT_FOUND);
        assertEquals(OWNER_PHONE, lookup.queriedPetOwnerPhone);
        assertEquals("BuBu", lookup.queriedPetName);
    }

    @Test
    public void execute_usesLookupStateAtExecutionTime() {
        ScheduleCommand command = command(VALID_APPOINTMENT);
        lookup.ownerExists = false;
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(ScheduleCommand.MESSAGE_OWNER_NOT_FOUND, exception.getMessage());
        assertTrue(model.getAppointmentBook().getAppointmentList().isEmpty());
    }

    @Test
    public void execute_pastDateEarlierTodayAndExactNow_noMutation() {
        for (Appointment appointment : List.of(create("06-10-2026", "19:30", "20:00"),
                create("07-10-2026", "09:30", "10:30"), create("07-10-2026", "10:00", "11:00"))) {
            assertFailure(appointment, CLOCK, ScheduleCommand.MESSAGE_PAST_START);
        }
    }

    @Test
    public void execute_futureDateWithEarlierTimeOfDay_success() throws Exception {
        Appointment tomorrow = create("08-10-2026", "08:00", "08:30");
        command(tomorrow).execute(model);
        assertEquals(List.of(tomorrow), model.getAppointmentBook().getAppointmentList());
    }

    @Test
    public void execute_clockInDifferentZone_usesSingaporeDate() {
        // UTC still reads 7 October, but Singapore has reached 8 October.
        Clock midnight = Clock.fixed(Instant.parse("2026-10-07T16:00:00Z"), ZoneOffset.UTC);
        assertFailure(create("07-10-2026", "19:30", "20:00"), midnight, ScheduleCommand.MESSAGE_PAST_START);
    }

    @Test
    public void execute_overlapForDifferentOwnerAndPet_noMutation() {
        model.addAppointment(new Appointment(new Phone("81234567"), "Mochi", new AppointmentDate("07-10-2026"),
                new StartTime("11:00"), new EndTime("12:00"), Service.NAIL_TRIM));
        for (Appointment appointment : List.of(create("07-10-2026", "11:00", "12:00"),
                create("07-10-2026", "10:30", "11:30"), create("07-10-2026", "11:30", "12:30"),
                create("07-10-2026", "10:30", "12:30"), create("07-10-2026", "11:00", "11:30"))) {
            CommandException exception = assertFailure(appointment, CLOCK, ScheduleCommand.MESSAGE_OVERLAP);
            assertTrue(exception.getCause() instanceof OverlappingAppointmentException);
        }
    }

    @Test
    public void execute_backToBackAndDifferentDate_success() throws Exception {
        model.addAppointment(create("07-10-2026", "11:00", "12:00"));
        for (Appointment appointment : List.of(VALID_APPOINTMENT,
                create("07-10-2026", "12:00", "12:30"), create("08-10-2026", "11:00", "12:00"))) {
            command(appointment).execute(model);
        }
        assertEquals(4, model.getAppointmentBook().getAppointmentList().size());
    }

    @Test
    public void execute_repeatedCommand_rejectsDuplicate() throws Exception {
        ScheduleCommand command = command(VALID_APPOINTMENT);
        command.execute(model);
        assertFailure(VALID_APPOINTMENT, CLOCK, ScheduleCommand.MESSAGE_OVERLAP);
        assertEquals(List.of(VALID_APPOINTMENT), model.getAppointmentBook().getAppointmentList());
    }

    @Test
    public void execute_normalisedPetName_passedToLookup() throws Exception {
        Appointment appointment = new Appointment(OWNER_PHONE, "  BuBu   Junior  ", VALID_APPOINTMENT.getDate(),
                VALID_APPOINTMENT.getStartTime(), VALID_APPOINTMENT.getEndTime(), Service.FULL_GROOM);
        command(appointment).execute(model);
        assertEquals("BuBu Junior", lookup.queriedPetName);
    }

    @Test
    public void constructorAndExecute_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new ScheduleCommand(null, lookup));
        assertThrows(NullPointerException.class, () -> new ScheduleCommand(VALID_APPOINTMENT, null));
        assertThrows(NullPointerException.class, () -> new ScheduleCommand(VALID_APPOINTMENT, lookup, null));
        assertThrows(NullPointerException.class, () -> command(VALID_APPOINTMENT).execute(null));
        assertTrue(model.getAppointmentBook().getAppointmentList().isEmpty());
    }

    @Test
    public void equals_comparesAppointmentDetails() {
        ScheduleCommand command = command(VALID_APPOINTMENT);
        assertEquals(command, command);
        assertEquals(command, command(create("07-10-2026", "10:30", "11:00")));
        assertNotEquals(command, command(create("07-10-2026", "11:00", "12:00")));
        assertNotEquals(command, null);
        assertNotEquals(command, "schedule");
    }

    @Test
    public void toString_includesAppointment() {
        assertEquals(ScheduleCommand.class.getCanonicalName() + "{toSchedule=" + VALID_APPOINTMENT + "}",
                command(VALID_APPOINTMENT).toString());
    }

    private ScheduleCommand command(Appointment appointment) {
        return new ScheduleCommand(appointment, lookup, CLOCK);
    }

    private CommandException assertFailure(Appointment appointment, Clock clock, String message) {
        AppointmentBook before = new AppointmentBook(model.getAppointmentBook());
        CommandException exception = assertThrows(CommandException.class, () ->
                new ScheduleCommand(appointment, lookup, clock).execute(model));
        assertEquals(message, exception.getMessage());
        assertEquals(before, model.getAppointmentBook());
        return exception;
    }

    private static Appointment create(String date, String start, String end) {
        return new Appointment(OWNER_PHONE, "BuBu", new AppointmentDate(date),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }

    /**
     * Supplies participant lookup outcomes without implementing the owner or pet registries.
     */
    private static class ParticipantLookupStub implements AppointmentParticipantLookup {
        private boolean ownerExists = true;
        private boolean petBelongsToOwner = true;
        private Phone queriedOwnerPhone;
        private Phone queriedPetOwnerPhone;
        private String queriedPetName;
        private int petLookupCalls;

        @Override
        public boolean hasOwner(Phone ownerPhone) {
            queriedOwnerPhone = ownerPhone;
            return ownerExists;
        }

        @Override
        public boolean hasPet(Phone ownerPhone, String petName) {
            queriedPetOwnerPhone = ownerPhone;
            queriedPetName = petName;
            petLookupCalls++;
            return petBelongsToOwner;
        }
    }
}

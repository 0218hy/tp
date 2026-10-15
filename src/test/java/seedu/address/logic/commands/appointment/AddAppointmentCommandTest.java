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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.GroomingBook;
import seedu.address.model.ModelManager;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.appointment.exceptions.OverlappingAppointmentException;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

public class AddAppointmentCommandTest {
    private static final Phone OWNER_PHONE = new Phone("91234567");
    // 10:00 on 7 October in Singapore, even though the supplied clock uses UTC.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-07T02:00:00Z"), ZoneOffset.UTC);
    private static final Appointment VALID_APPOINTMENT = create("07-10-2026", "10:30", "11:00");

    private final ModelManager model = new ModelManager();
    private final Client owner = new ClientBuilder().withPhone(OWNER_PHONE.value).build();
    private final Pet pet = new PetBuilder(owner).withName("BuBu").build();

    @BeforeEach
    public void setUp() {
        model.addClient(owner);
        model.addPet(pet);
    }

    @Test
    public void execute_validAppointment_addsToModelAndReturnsDetails() throws Exception {
        CommandResult result = command(VALID_APPOINTMENT).execute(model);
        assertEquals(List.of(VALID_APPOINTMENT), model.getGroomingBook().getAppointmentList());
        assertEquals("Appointment scheduled: BuBu; Owner phone: 91234567; Date: 07-10-2026; "
                + "Time: 10:30-11:00; Service: Full groom", result.getFeedbackToUser());
        assertFalse(result.isExit());
        assertFalse(result.isShowHelp());
    }

    @Test
    public void execute_missingOwner_noMutation() {
        model.clearGroomingBook();
        assertFailure(VALID_APPOINTMENT, CLOCK, AddAppointmentCommand.MESSAGE_OWNER_NOT_FOUND);
    }

    @Test
    public void execute_petNotRegisteredForOwner_noMutation() {
        model.deletePet(pet);
        assertFailure(VALID_APPOINTMENT, CLOCK, AddAppointmentCommand.MESSAGE_PET_NOT_FOUND);
    }

    @Test
    public void execute_ownerRemovedBeforeExecution_rejectsAppointment() {
        AddAppointmentCommand command = command(VALID_APPOINTMENT);
        model.clearGroomingBook();
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(AddAppointmentCommand.MESSAGE_OWNER_NOT_FOUND, exception.getMessage());
        assertTrue(model.getGroomingBook().getAppointmentList().isEmpty());
    }

    @Test
    public void execute_petBelongsToDifferentOwner_noMutation() {
        model.deletePet(pet);
        Client otherOwner = new ClientBuilder().withPhone("81234567").withEmail("other@example.com").build();
        model.addClient(otherOwner);
        model.addPet(new PetBuilder(otherOwner).withName("BuBu").build());
        assertFailure(VALID_APPOINTMENT, CLOCK, AddAppointmentCommand.MESSAGE_PET_NOT_FOUND);
    }

    @Test
    public void execute_filteredOutParticipants_usesAllSavedRecords() throws Exception {
        model.updateFilteredClientList(client -> false);
        model.updateFilteredPetList(savedPet -> false);
        command(VALID_APPOINTMENT).execute(model);
        assertEquals(List.of(VALID_APPOINTMENT), model.getGroomingBook().getAppointmentList());
        assertTrue(model.getFilteredClientList().isEmpty());
        assertTrue(model.getFilteredPetList().isEmpty());
    }

    @Test
    public void execute_petRemovedBeforeExecution_rejectsAppointment() {
        AddAppointmentCommand command = command(VALID_APPOINTMENT);
        model.deletePet(pet);
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(AddAppointmentCommand.MESSAGE_PET_NOT_FOUND, exception.getMessage());
        assertTrue(model.getGroomingBook().getAppointmentList().isEmpty());
    }

    @Test
    public void execute_ownerPhoneEdited_usesUpdatedPetOwner() throws Exception {
        Client editedOwner = new ClientBuilder(owner).withPhone("81234567").build();
        model.setClient(owner, editedOwner);
        assertFailure(VALID_APPOINTMENT, CLOCK, AddAppointmentCommand.MESSAGE_OWNER_NOT_FOUND);
        Appointment appointment = new Appointment(editedOwner.getPhone(), "BuBu", VALID_APPOINTMENT.getDate(),
                VALID_APPOINTMENT.getStartTime(), VALID_APPOINTMENT.getEndTime(), Service.FULL_GROOM);
        command(appointment).execute(model);
        assertEquals(List.of(appointment), model.getGroomingBook().getAppointmentList());
    }

    @Test
    public void execute_pastDateEarlierTodayAndExactNow_noMutation() {
        for (Appointment appointment : List.of(create("06-10-2026", "19:30", "20:00"),
                create("07-10-2026", "09:30", "10:30"), create("07-10-2026", "10:00", "11:00"))) {
            assertFailure(appointment, CLOCK, AddAppointmentCommand.MESSAGE_PAST_START);
        }
    }

    @Test
    public void execute_futureDateWithEarlierTimeOfDay_success() throws Exception {
        Appointment tomorrow = create("08-10-2026", "08:00", "08:30");
        command(tomorrow).execute(model);
        assertEquals(List.of(tomorrow), model.getGroomingBook().getAppointmentList());
    }

    @Test
    public void execute_clockInDifferentZone_usesSingaporeDate() {
        // UTC still reads 7 October, but Singapore has reached 8 October.
        Clock midnight = Clock.fixed(Instant.parse("2026-10-07T16:00:00Z"), ZoneOffset.UTC);
        assertFailure(create("07-10-2026", "19:30", "20:00"), midnight, AddAppointmentCommand.MESSAGE_PAST_START);
    }

    @Test
    public void execute_overlapForDifferentOwnerAndPet_noMutation() {
        model.addAppointment(new Appointment(new Phone("81234567"), "Mochi", new AppointmentDate("07-10-2026"),
                new StartTime("11:00"), new EndTime("12:00"), Service.NAIL_TRIM));
        for (Appointment appointment : List.of(create("07-10-2026", "11:00", "12:00"),
                create("07-10-2026", "10:30", "11:30"), create("07-10-2026", "11:30", "12:30"),
                create("07-10-2026", "10:30", "12:30"), create("07-10-2026", "11:00", "11:30"))) {
            CommandException exception = assertFailure(appointment, CLOCK, AddAppointmentCommand.MESSAGE_OVERLAP);
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
        assertEquals(4, model.getGroomingBook().getAppointmentList().size());
    }

    @Test
    public void execute_repeatedCommand_rejectsDuplicate() throws Exception {
        AddAppointmentCommand command = command(VALID_APPOINTMENT);
        command.execute(model);
        assertFailure(VALID_APPOINTMENT, CLOCK, AddAppointmentCommand.MESSAGE_OVERLAP);
        assertEquals(List.of(VALID_APPOINTMENT), model.getGroomingBook().getAppointmentList());
    }

    @Test
    public void execute_caseAndWhitespaceInPetName_matchesRegisteredPet() throws Exception {
        model.addPet(new PetBuilder(owner).withName("bubu   JUNIOR").build());
        Appointment appointment = new Appointment(OWNER_PHONE, "  BuBu   Junior  ", VALID_APPOINTMENT.getDate(),
                VALID_APPOINTMENT.getStartTime(), VALID_APPOINTMENT.getEndTime(), Service.FULL_GROOM);
        command(appointment).execute(model);
        assertEquals(List.of(appointment), model.getGroomingBook().getAppointmentList());
    }

    @Test
    public void constructorAndExecute_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new AddAppointmentCommand(null));
        assertThrows(NullPointerException.class, () -> new AddAppointmentCommand(VALID_APPOINTMENT, null));
        assertThrows(NullPointerException.class, () -> command(VALID_APPOINTMENT).execute(null));
        assertTrue(model.getGroomingBook().getAppointmentList().isEmpty());
    }

    @Test
    public void equals_comparesAppointmentDetails() {
        AddAppointmentCommand command = command(VALID_APPOINTMENT);
        assertEquals(command, command);
        assertEquals(command, command(create("07-10-2026", "10:30", "11:00")));
        assertNotEquals(command, command(create("07-10-2026", "11:00", "12:00")));
        assertNotEquals(command, null);
        assertNotEquals(command, "schedule");
    }

    @Test
    public void toString_includesAppointment() {
        assertEquals(AddAppointmentCommand.class.getCanonicalName() + "{toSchedule=" + VALID_APPOINTMENT + "}",
                command(VALID_APPOINTMENT).toString());
    }

    private AddAppointmentCommand command(Appointment appointment) {
        return new AddAppointmentCommand(appointment, CLOCK);
    }

    private CommandException assertFailure(Appointment appointment, Clock clock, String message) {
        GroomingBook before = new GroomingBook(model.getGroomingBook());
        CommandException exception = assertThrows(CommandException.class, () ->
                new AddAppointmentCommand(appointment, clock).execute(model));
        assertEquals(message, exception.getMessage());
        assertEquals(before, model.getGroomingBook());
        return exception;
    }

    private static Appointment create(String date, String start, String end) {
        return new Appointment(OWNER_PHONE, "BuBu", new AppointmentDate(date),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }

}

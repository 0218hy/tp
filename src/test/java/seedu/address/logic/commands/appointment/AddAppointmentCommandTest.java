package seedu.address.logic.commands.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.BubuBook;
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

    //@@author 0218hy
    @BeforeEach
    public void setUp() {
        model.addClient(owner);
        model.addPet(pet);
    }

    @Test
    public void execute_validAppointment_addsToModelAndReturnsDetails() throws Exception {
        CommandResult result = command(VALID_APPOINTMENT).execute(model);
        assertEquals(List.of(VALID_APPOINTMENT), model.getBubuBook().getAppointmentList());
        assertEquals("Appointment scheduled: BuBu; Owner phone: 91234567; Date: 07-10-2026; "
                + "Time: 10:30-11:00; Service: Full groom", result.getFeedbackToUser());
        Appointment saved = model.getBubuBook().getAppointmentList().get(0);
        assertSame(model.getBubuBook().getClientList().get(0), saved.getOwner());
        assertSame(model.getBubuBook().getPetList().get(0), saved.getPet());
        assertFalse(result.isExit());
        assertFalse(result.isShowHelp());
    }

    @Test
    public void execute_missingOwner_noMutation() {
        model.clearBubuBook();
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
        model.clearBubuBook();
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(AddAppointmentCommand.MESSAGE_OWNER_NOT_FOUND, exception.getMessage());
        assertTrue(model.getBubuBook().getAppointmentList().isEmpty());
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
        assertEquals(List.of(VALID_APPOINTMENT), model.getBubuBook().getAppointmentList());
        assertTrue(model.getFilteredClientList().isEmpty());
        assertTrue(model.getFilteredPetList().isEmpty());
    }

    @Test
    public void execute_petRemovedBeforeExecution_rejectsAppointment() {
        AddAppointmentCommand command = command(VALID_APPOINTMENT);
        model.deletePet(pet);
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(AddAppointmentCommand.MESSAGE_PET_NOT_FOUND, exception.getMessage());
        assertTrue(model.getBubuBook().getAppointmentList().isEmpty());
    }

    @Test
    public void execute_participantDetailsEditedBeforeExecution_usesLatestObjects() throws Exception {
        AddAppointmentCommand command = command(VALID_APPOINTMENT);
        Client editedOwner = new ClientBuilder(owner).withAddress("New address").withTags("friend").build();
        model.setClient(owner, editedOwner);
        Pet editedPet = new PetBuilder(pet).withOwner(editedOwner).withBreed("Poodle")
                .withRequirement("Sensitive skin").build();
        model.setPet(model.getBubuBook().getPetList().get(0), editedPet);

        command.execute(model);

        Appointment saved = model.getBubuBook().getAppointmentList().get(0);
        assertSame(editedOwner, saved.getOwner());
        assertSame(model.getBubuBook().getPetList().get(0), saved.getPet());
        assertEquals(editedPet, saved.getPet());
    }

    @Test
    public void execute_ownerPhoneEdited_usesUpdatedPetOwner() throws Exception {
        Client editedOwner = new ClientBuilder(owner).withPhone("81234567").build();
        model.setClient(owner, editedOwner);
        assertFailure(VALID_APPOINTMENT, CLOCK, AddAppointmentCommand.MESSAGE_OWNER_NOT_FOUND);
        Appointment appointment = createAppointment(editedOwner.getPhone(), "BuBu", VALID_APPOINTMENT.getDate(),
                VALID_APPOINTMENT.getStartTime(), VALID_APPOINTMENT.getEndTime(), Service.FULL_GROOM);
        command(appointment).execute(model);
        assertEquals(List.of(appointment), model.getBubuBook().getAppointmentList());
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
        assertEquals(List.of(tomorrow), model.getBubuBook().getAppointmentList());
    }

    @Test
    public void execute_clockInDifferentZone_usesSingaporeDate() {
        // UTC still reads 7 October, but Singapore has reached 8 October.
        Clock midnight = Clock.fixed(Instant.parse("2026-10-07T16:00:00Z"), ZoneOffset.UTC);
        assertFailure(create("07-10-2026", "19:30", "20:00"), midnight, AddAppointmentCommand.MESSAGE_PAST_START);
    }

    @Test
    public void execute_overlapForDifferentOwnerAndPet_noMutation() {
        model.addAppointment(createAppointment(new Phone("81234567"), "Mochi", new AppointmentDate("07-10-2026"),
                new StartTime("11:00"), new EndTime("12:00"), Service.NAIL_TRIM));
        for (Appointment appointment : List.of(create("07-10-2026", "11:00", "12:00"),
                create("07-10-2026", "10:30", "11:30"), create("07-10-2026", "11:30", "12:30"),
                create("07-10-2026", "10:30", "12:30"), create("07-10-2026", "11:00", "11:30"))) {
            CommandException exception = assertFailure(appointment, CLOCK,
                    "This appointment overlaps an existing booking for Mochi on 07-10-2026, 11:00–12:00.");
            assertTrue(exception.getCause() instanceof OverlappingAppointmentException);
        }
    }

    @Test
    public void execute_hiddenConflicts_reportsExistingBookingAndPreservesFilter() {
        Appointment unrelated = create("08-10-2026", "11:00", "12:00");
        Appointment conflict = createAppointment(new Phone("81234567"), "Mochi",
                new AppointmentDate("07-10-2026"), new StartTime("11:00"), new EndTime("12:00"),
                Service.NAIL_TRIM);
        Appointment laterConflict = create("07-10-2026", "12:00", "13:00");
        model.setAppointments(List.of(unrelated, conflict, laterConflict));
        model.updateFilteredAppointmentList(appointment -> appointment.equals(unrelated));

        CommandException exception = assertFailure(create("07-10-2026", "10:30", "12:30"), CLOCK,
                "This appointment overlaps an existing booking for Mochi on 07-10-2026, 11:00–12:00.");

        assertSame(conflict, ((OverlappingAppointmentException) exception.getCause()).getConflictingAppointment());
        assertEquals(List.of(unrelated), model.getFilteredAppointmentList());
    }

    @Test
    public void execute_backToBackAndDifferentDate_success() throws Exception {
        model.addAppointment(create("07-10-2026", "11:00", "12:00"));
        for (Appointment appointment : List.of(VALID_APPOINTMENT,
                create("07-10-2026", "12:00", "12:30"), create("08-10-2026", "11:00", "12:00"))) {
            command(appointment).execute(model);
        }
        assertEquals(4, model.getBubuBook().getAppointmentList().size());
    }

    @Test
    public void execute_repeatedCommand_rejectsDuplicate() throws Exception {
        AddAppointmentCommand command = command(VALID_APPOINTMENT);
        command.execute(model);
        assertFailure(VALID_APPOINTMENT, CLOCK,
                "This appointment overlaps an existing booking for BuBu on 07-10-2026, 10:30–11:00.");
        assertEquals(List.of(VALID_APPOINTMENT), model.getBubuBook().getAppointmentList());
    }

    @Test
    public void execute_caseAndWhitespaceInPetName_matchesRegisteredPet() throws Exception {
        model.addPet(new PetBuilder(owner).withName("bubu   JUNIOR").build());
        Appointment appointment = createAppointment(OWNER_PHONE, "  BuBu   Junior  ", VALID_APPOINTMENT.getDate(),
                VALID_APPOINTMENT.getStartTime(), VALID_APPOINTMENT.getEndTime(), Service.FULL_GROOM);
        command(appointment).execute(model);
        Appointment saved = model.getBubuBook().getAppointmentList().get(0);
        assertEquals("bubu   JUNIOR", saved.getPet().getName().value);
        assertSame(model.getBubuBook().getPetList().get(1), saved.getPet());
    }

    @Test
    public void constructorAndExecute_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new AddAppointmentCommand(null, "BuBu",
                VALID_APPOINTMENT.getDate(), VALID_APPOINTMENT.getStartTime(), VALID_APPOINTMENT.getEndTime(),
                Service.FULL_GROOM));
        assertThrows(NullPointerException.class, () -> command(VALID_APPOINTMENT, null));
        assertThrows(NullPointerException.class, () -> command(VALID_APPOINTMENT).execute(null));
        assertTrue(model.getBubuBook().getAppointmentList().isEmpty());
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
    public void equals_oneDifferentBookingField_notEqual() {
        AddAppointmentCommand original = command(VALID_APPOINTMENT);
        Appointment[] differentAppointments = {
            createAppointment(new Phone("81234567"), "BuBu", VALID_APPOINTMENT.getDate(),
                    VALID_APPOINTMENT.getStartTime(), VALID_APPOINTMENT.getEndTime(), Service.FULL_GROOM),
            createAppointment(OWNER_PHONE, "Mochi", VALID_APPOINTMENT.getDate(),
                    VALID_APPOINTMENT.getStartTime(), VALID_APPOINTMENT.getEndTime(), Service.FULL_GROOM),
            create("08-10-2026", "10:30", "11:00"),
            create("07-10-2026", "10:00", "11:00"),
            create("07-10-2026", "10:30", "11:30"),
            createAppointment(OWNER_PHONE, "BuBu", VALID_APPOINTMENT.getDate(),
                    VALID_APPOINTMENT.getStartTime(), VALID_APPOINTMENT.getEndTime(), Service.NAIL_TRIM)
        };

        for (Appointment appointment : differentAppointments) {
            AddAppointmentCommand different = command(appointment);
            assertNotEquals(original, different);
            assertNotEquals(different, original);
        }
    }

    @Test
    public void toString_includesAppointment() {
        assertEquals(AddAppointmentCommand.class.getCanonicalName()
                + "{ownerPhone=91234567, petName=BuBu, date=07-10-2026, "
                + "startTime=10:30, endTime=11:00, service=Full groom}", command(VALID_APPOINTMENT).toString());
    }

    private AddAppointmentCommand command(Appointment appointment) {
        return command(appointment, CLOCK);
    }

    private AddAppointmentCommand command(Appointment appointment, Clock clock) {
        return new AddAppointmentCommand(appointment.getOwnerPhone(), appointment.getPetName().value,
                appointment.getDate(), appointment.getStartTime(), appointment.getEndTime(),
                appointment.getService(), clock);
    }

    private CommandException assertFailure(Appointment appointment, Clock clock, String message) {
        BubuBook before = new BubuBook(model.getBubuBook());
        CommandException exception = assertThrows(CommandException.class, () ->
                command(appointment, clock).execute(model));
        assertEquals(message, exception.getMessage());
        assertEquals(before, model.getBubuBook());
        return exception;
    }

    private static Appointment create(String date, String start, String end) {
        return createAppointment(OWNER_PHONE, "BuBu", new AppointmentDate(date),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }
    //@@author

}

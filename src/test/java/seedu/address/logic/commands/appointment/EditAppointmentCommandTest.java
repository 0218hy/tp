package seedu.address.logic.commands.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.BubuBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
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

/** Contains integration and unit tests for {@link EditAppointmentCommand}. */
public class EditAppointmentCommandTest {
    private static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-10-07T02:00:00Z"), SINGAPORE);
    private static final Phone OWNER_PHONE = new Phone("91234567");
    private static final AppointmentDate ORIGINAL_DATE = new AppointmentDate("07-10-2026");
    private static final StartTime ORIGINAL_START = new StartTime("10:30");
    private static final EndTime ORIGINAL_END = new EndTime("11:00");
    private static final Appointment ORIGINAL_APPOINTMENT = createAppointment(OWNER_PHONE, "BuBu",
            ORIGINAL_DATE, ORIGINAL_START, ORIGINAL_END, Service.FULL_GROOM);

    private ModelManager model;
    private Client owner;
    private Pet pet;

    //@@author chunghean
    @BeforeEach
    public void setUp() {
        owner = new ClientBuilder().withPhone(OWNER_PHONE.value).build();
        pet = new PetBuilder(owner).withName("BuBu").build();
        BubuBook book = new BubuBook();
        book.addClient(owner);
        book.addPet(pet);
        book.addAppointment(new Appointment(owner, pet, ORIGINAL_DATE, ORIGINAL_START, ORIGINAL_END,
                Service.FULL_GROOM));
        model = new ModelManager(book, new UserPrefs());
    }

    @Test
    public void execute_identifierEdit_success() throws Exception {
        EditAppointmentCommand.EditAppointmentDescriptor descriptor = descriptorWithEndTime("11:30");
        EditAppointmentCommand command = new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START,
                descriptor, CLOCK);

        CommandResult result = command.execute(model);

        Appointment edited = model.getBubuBook().getAppointmentList().get(0);
        assertEquals(new EndTime("11:30"), edited.getEndTime());
        assertEquals("Edited appointment: BuBu; Owner phone: 91234567; Date: 07-10-2026; "
                + "Time: 10:30-11:30; Service: Full groom", result.getFeedbackToUser());
        assertEquals(1, model.getFilteredAppointmentList().size());
        assertFalse(result.isShowHelp());
    }

    @Test
    public void execute_indexEdit_success() throws Exception {
        EditAppointmentCommand.EditAppointmentDescriptor descriptor =
                new EditAppointmentCommand.EditAppointmentDescriptor();
        descriptor.setService(Service.NAIL_TRIM);
        EditAppointmentCommand command = new EditAppointmentCommand(Index.fromOneBased(1), descriptor, CLOCK);

        CommandResult result = command.execute(model);

        assertEquals(Service.NAIL_TRIM, model.getBubuBook().getAppointmentList().get(0).getService());
        assertEquals("Edited appointment: BuBu; Owner phone: 91234567; Date: 07-10-2026; "
                + "Time: 10:30-11:00; Service: Nail trim", result.getFeedbackToUser());
        assertEquals(CommandResult.DisplayType.APPOINTMENTS, command.getRequiredDisplayType().orElseThrow());
    }

    @Test
    public void execute_identifierEdit_canChangeAllFields() throws Exception {
        Client newOwner = new ClientBuilder().withName("Mochi Owner").withPhone("81234567")
                .withEmail("mochi@example.com").build();
        Pet newPet = new PetBuilder(newOwner).withName("Mochi").build();
        model.addClient(newOwner);
        model.addPet(newPet);

        EditAppointmentCommand.EditAppointmentDescriptor descriptor =
                new EditAppointmentCommand.EditAppointmentDescriptor();
        descriptor.setOwnerPhone(newOwner.getPhone());
        descriptor.setPetName("mochi");
        descriptor.setDate(new AppointmentDate("09-10-2026"));
        descriptor.setStartTime(new StartTime("14:00"));
        descriptor.setEndTime(new EndTime("15:00"));
        descriptor.setService(Service.NAIL_TRIM);

        new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START, descriptor, CLOCK).execute(model);

        Appointment edited = model.getBubuBook().getAppointmentList().get(0);
        assertEquals(newOwner, edited.getOwner());
        assertEquals(newPet, edited.getPet());
        assertEquals(new AppointmentDate("09-10-2026"), edited.getDate());
        assertEquals(new StartTime("14:00"), edited.getStartTime());
        assertEquals(new EndTime("15:00"), edited.getEndTime());
        assertEquals(Service.NAIL_TRIM, edited.getService());
    }

    @Test
    public void execute_identifierEdit_worksWhenAppointmentIsFilteredOut() throws Exception {
        model.updateFilteredAppointmentList(appointment -> false);
        EditAppointmentCommand command = new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START,
                descriptorWithEndTime("11:30"), CLOCK);

        command.execute(model);

        assertEquals(new EndTime("11:30"), model.getBubuBook().getAppointmentList().get(0).getEndTime());
        assertTrue(command.getRequiredDisplayType().isEmpty());
    }

    @Test
    public void execute_unknownIdentifier_failure() {
        EditAppointmentCommand command = new EditAppointmentCommand(new AppointmentDate("08-10-2026"),
                ORIGINAL_START, descriptorWithEndTime("11:30"), CLOCK);

        assertFailure(command, EditAppointmentCommand.MESSAGE_APPOINTMENT_NOT_FOUND);
    }

    @Test
    public void execute_invalidIndex_failure() {
        EditAppointmentCommand command = new EditAppointmentCommand(Index.fromOneBased(2),
                descriptorWithEndTime("11:30"), CLOCK);

        assertFailure(command, EditAppointmentCommand.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredListIndex_failure() {
        model.updateFilteredAppointmentList(appointment -> false);
        EditAppointmentCommand command = new EditAppointmentCommand(Index.fromOneBased(1),
                descriptorWithEndTime("11:30"), CLOCK);

        assertFailure(command, EditAppointmentCommand.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_ownerNotFound_failure() {
        EditAppointmentCommand.EditAppointmentDescriptor descriptor = descriptorWithEndTime("11:30");
        descriptor.setOwnerPhone(new Phone("89998888"));

        assertFailure(new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START, descriptor, CLOCK),
                EditAppointmentCommand.MESSAGE_OWNER_NOT_FOUND);
    }

    @Test
    public void execute_petNotFound_failure() {
        EditAppointmentCommand.EditAppointmentDescriptor descriptor = descriptorWithEndTime("11:30");
        descriptor.setPetName("Mochi");

        assertFailure(new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START, descriptor, CLOCK),
                EditAppointmentCommand.MESSAGE_PET_NOT_FOUND);
    }

    @Test
    public void execute_pastStart_failure() {
        EditAppointmentCommand.EditAppointmentDescriptor descriptor =
                new EditAppointmentCommand.EditAppointmentDescriptor();
        descriptor.setDate(new AppointmentDate("07-10-2026"));
        descriptor.setStartTime(new StartTime("09:30"));
        descriptor.setEndTime(new EndTime("10:00"));

        assertFailure(new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START, descriptor, CLOCK),
                EditAppointmentCommand.MESSAGE_PAST_START);
    }

    @Test
    public void execute_overlapFailure_preservesModel() {
        Appointment other = createAppointment(OWNER_PHONE, "BuBu", new AppointmentDate("07-10-2026"),
                new StartTime("11:00"), new EndTime("12:00"), Service.NAIL_TRIM);
        model.addAppointment(other);
        BubuBook before = new BubuBook(model.getBubuBook());
        EditAppointmentCommand.EditAppointmentDescriptor descriptor =
                new EditAppointmentCommand.EditAppointmentDescriptor();
        descriptor.setStartTime(new StartTime("11:30"));
        descriptor.setEndTime(new EndTime("12:30"));

        CommandException exception = assertThrows(CommandException.class, () ->
                new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START, descriptor, CLOCK).execute(model));

        assertEquals(EditAppointmentCommand.MESSAGE_OVERLAP, exception.getMessage());
        assertTrue(exception.getCause() instanceof OverlappingAppointmentException);
        assertEquals(before, model.getBubuBook());
    }

    @Test
    public void execute_nullArguments_rejected() {
        EditAppointmentCommand.EditAppointmentDescriptor descriptor = descriptorWithEndTime("11:30");
        assertThrows(NullPointerException.class, () -> new EditAppointmentCommand(null, descriptor, CLOCK));
        assertThrows(NullPointerException.class, () -> new EditAppointmentCommand(ORIGINAL_DATE, null,
                descriptor, CLOCK));
        assertThrows(NullPointerException.class, () -> new EditAppointmentCommand(Index.fromOneBased(1), null,
                CLOCK));
        assertThrows(NullPointerException.class, () -> new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START,
                descriptor, null));
        assertThrows(NullPointerException.class, () -> new EditAppointmentCommand(Index.fromOneBased(1),
                descriptor, null));
        assertThrows(NullPointerException.class, () -> new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START,
                descriptor, CLOCK).execute(null));
    }

    @Test
    public void defaultConstructors_createCommands() {
        EditAppointmentCommand identifierCommand = new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START,
                descriptorWithEndTime("11:30"));
        EditAppointmentCommand indexCommand = new EditAppointmentCommand(Index.fromOneBased(1),
                descriptorWithEndTime("11:30"));

        assertTrue(identifierCommand.getRequiredDisplayType().isEmpty());
        assertEquals(CommandResult.DisplayType.APPOINTMENTS,
                indexCommand.getRequiredDisplayType().orElseThrow());
    }

    @Test
    public void equals_comparesTargetAndDescriptor() {
        EditAppointmentCommand.EditAppointmentDescriptor descriptor = descriptorWithEndTime("11:30");
        EditAppointmentCommand command = new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START, descriptor, CLOCK);
        EditAppointmentCommand same = new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START,
                new EditAppointmentCommand.EditAppointmentDescriptor(descriptor), CLOCK);

        assertEquals(command, command);
        assertEquals(command, same);
        assertNotEquals(command, new EditAppointmentCommand(new AppointmentDate("08-10-2026"), ORIGINAL_START,
                descriptor, CLOCK));
        assertNotEquals(command, new EditAppointmentCommand(ORIGINAL_DATE, new StartTime("11:00"), descriptor, CLOCK));
        assertNotEquals(command, new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START,
                new EditAppointmentCommand.EditAppointmentDescriptor(), CLOCK));
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, new EditAppointmentCommand(Index.fromOneBased(1), descriptor, CLOCK));
    }

    @Test
    public void toString_containsIdentifierOrIndex() {
        EditAppointmentCommand.EditAppointmentDescriptor descriptor = descriptorWithEndTime("11:30");
        EditAppointmentCommand identifierCommand = new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START,
                descriptor, CLOCK);
        EditAppointmentCommand indexCommand = new EditAppointmentCommand(Index.fromOneBased(1), descriptor, CLOCK);

        assertTrue(identifierCommand.toString().contains("targetDate=" + ORIGINAL_DATE));
        assertTrue(identifierCommand.toString().contains("targetStartTime=" + ORIGINAL_START));
        assertTrue(indexCommand.toString().contains("targetIndex="));
    }

    @Test
    public void descriptor_accessorsCopyAndEquality() {
        EditAppointmentCommand.EditAppointmentDescriptor descriptor =
                new EditAppointmentCommand.EditAppointmentDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        descriptor.setOwnerPhone(OWNER_PHONE);
        descriptor.setPetName("BuBu");
        descriptor.setDate(ORIGINAL_DATE);
        descriptor.setStartTime(ORIGINAL_START);
        descriptor.setEndTime(ORIGINAL_END);
        descriptor.setService(Service.FULL_GROOM);
        EditAppointmentCommand.EditAppointmentDescriptor copy =
                new EditAppointmentCommand.EditAppointmentDescriptor(descriptor);

        assertTrue(descriptor.isAnyFieldEdited());
        assertEquals(List.of(OWNER_PHONE), descriptor.getOwnerPhone().stream().toList());
        assertEquals("BuBu", descriptor.getPetName().orElseThrow().value);
        assertEquals(ORIGINAL_DATE, descriptor.getDate().orElseThrow());
        assertEquals(ORIGINAL_START, descriptor.getStartTime().orElseThrow());
        assertEquals(ORIGINAL_END, descriptor.getEndTime().orElseThrow());
        assertEquals(Service.FULL_GROOM, descriptor.getService().orElseThrow());
        assertEquals(descriptor, copy);
        assertEquals(descriptor.toString(), copy.toString());
        assertNotEquals(descriptor, new EditAppointmentCommand.EditAppointmentDescriptor());
        assertNotEquals(descriptor, null);

        descriptor.setPetName((String) null);
        assertTrue(descriptor.getPetName().isEmpty());
        assertThrows(NullPointerException.class, () ->
                new EditAppointmentCommand.EditAppointmentDescriptor(null));
    }

    @Test
    public void getRequiredDisplayType_identifierDoesNotRequireAppointmentDisplay() {
        EditAppointmentCommand command = new EditAppointmentCommand(ORIGINAL_DATE, ORIGINAL_START,
                descriptorWithEndTime("11:30"), CLOCK);
        assertTrue(command.getRequiredDisplayType().isEmpty());
    }

    private static EditAppointmentCommand.EditAppointmentDescriptor descriptorWithEndTime(String endTime) {
        EditAppointmentCommand.EditAppointmentDescriptor descriptor =
                new EditAppointmentCommand.EditAppointmentDescriptor();
        descriptor.setEndTime(new EndTime(endTime));
        return descriptor;
    }

    private void assertFailure(EditAppointmentCommand command, String expectedMessage) {
        BubuBook before = new BubuBook(model.getBubuBook());
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(before, model.getBubuBook());
    }
    //@@author
}

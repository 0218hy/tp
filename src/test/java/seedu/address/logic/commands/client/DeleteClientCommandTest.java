package seedu.address.logic.commands.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showClientAtIndex;
import static seedu.address.testutil.TypicalClients.getTypicalBubuBook;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.BubuBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
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

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteClientCommand}.
 */
public class DeleteClientCommandTest {

    private Model model = new ModelManager(getTypicalBubuBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Client clientToDelete = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());
        DeleteClientCommand deleteClientCommand = new DeleteClientCommand(INDEX_FIRST_CLIENT);

        String expectedMessage = String.format(DeleteClientCommand.MESSAGE_DELETE_CLIENT_SUCCESS,
                Messages.format(clientToDelete), "0 pets", "0 appointments");

        ModelManager expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.deleteClient(clientToDelete);

        assertCommandSuccess(deleteClientCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredClientList().size() + 1);
        DeleteClientCommand deleteClientCommand = new DeleteClientCommand(outOfBoundIndex);

        assertCommandFailure(deleteClientCommand, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showClientAtIndex(model, INDEX_FIRST_CLIENT);

        Client clientToDelete = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());
        DeleteClientCommand deleteClientCommand = new DeleteClientCommand(INDEX_FIRST_CLIENT);

        String expectedMessage = String.format(DeleteClientCommand.MESSAGE_DELETE_CLIENT_SUCCESS,
                Messages.format(clientToDelete), "0 pets", "0 appointments");

        Model expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.deleteClient(clientToDelete);
        showNoClient(expectedModel);

        assertCommandSuccess(deleteClientCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showClientAtIndex(model, INDEX_FIRST_CLIENT);

        Index outOfBoundIndex = INDEX_SECOND_CLIENT;
        // ensures that outOfBoundIndex is still in bounds of BuBu list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getBubuBook().getClientList().size());

        DeleteClientCommand deleteCommand = new DeleteClientCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validPhoneNumber_success() {
        Client clientToDelete = model.getBubuBook().getClientList().get(0);
        DeleteClientCommand deleteClientCommand = new DeleteClientCommand(clientToDelete.getPhone());

        String expectedMessage = String.format(DeleteClientCommand.MESSAGE_DELETE_CLIENT_SUCCESS,
                Messages.format(clientToDelete), "0 pets", "0 appointments");

        ModelManager expectedModel = new ModelManager(model.getBubuBook(), new UserPrefs());
        expectedModel.deleteClient(clientToDelete);

        assertCommandSuccess(deleteClientCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_clientWithPetsAndAppointments_reportsRemovedCounts() throws Exception {
        Client client = new ClientBuilder().withName("Jane Tan").withPhone("81234567")
                .withEmail("jane@example.com").withAddress("123 Clementi Road").build();
        BubuBook book = new BubuBook();
        book.addClient(client);
        Pet firstPet = new PetBuilder(client).withName("Milo").build();
        Pet secondPet = new PetBuilder(client).withName("Mochi").build();
        book.addPet(firstPet);
        book.addPet(secondPet);
        book.addAppointment(new Appointment(client, firstPet, new AppointmentDate("01-01-9999"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM));
        book.addAppointment(new Appointment(client, secondPet, new AppointmentDate("01-01-9999"),
                new StartTime("11:00"), new EndTime("12:00"), Service.NAIL_TRIM));
        Model deletionModel = new ModelManager(book, new UserPrefs());

        CommandResult result = new DeleteClientCommand(client.getPhone()).execute(deletionModel);

        assertEquals(String.format(DeleteClientCommand.MESSAGE_DELETE_CLIENT_SUCCESS,
                Messages.format(client), "2 pets", "2 appointments"), result.getFeedbackToUser());
        assertTrue(deletionModel.getBubuBook().getClientList().isEmpty());
        assertTrue(deletionModel.getBubuBook().getPetList().isEmpty());
        assertTrue(deletionModel.getBubuBook().getAppointmentList().isEmpty());
    }

    //@@author chunghean
    @Test
    public void execute_clientWithChangedEmail_deletesHistoricalAppointmentByPhone() throws Exception {
        Client historicalOwner = new ClientBuilder().withName("Alice Tan").withPhone("91234567")
                .withEmail("alice@example.com").build();
        Client registeredOwner = new ClientBuilder(historicalOwner).withEmail("new@example.com").build();
        Pet historicalPet = new PetBuilder(historicalOwner).withName("Milo").build();
        Appointment historicalAppointment = new Appointment(historicalOwner, historicalPet,
                new AppointmentDate("01-01-2000"), new StartTime("10:00"), new EndTime("11:00"),
                Service.FULL_GROOM);
        BubuBook book = new BubuBook();
        book.addClient(registeredOwner);
        book.addAppointment(historicalAppointment);
        Model deletionModel = new ModelManager(book, new UserPrefs());

        String expectedMessage = String.format(DeleteClientCommand.MESSAGE_DELETE_CLIENT_SUCCESS,
                Messages.format(registeredOwner), "0 pets", "1 appointment");

        assertEquals(expectedMessage, new DeleteClientCommand(registeredOwner.getPhone())
                .execute(deletionModel).getFeedbackToUser());
        assertTrue(deletionModel.getBubuBook().getAppointmentList().isEmpty());
    }
    //@@author

    //@@author chunghean
    @Test
    public void execute_unknownPhoneNumber_throwsCommandException() {
        DeleteClientCommand deleteClientCommand = new DeleteClientCommand(new Phone("81111112"));

        assertCommandFailure(deleteClientCommand, model, DeleteClientCommand.MESSAGE_CLIENT_NOT_FOUND);
    }

    @Test
    public void getRequiredDisplayType_dependsOnTarget() {
        assertEquals(java.util.Optional.of(CommandResult.DisplayType.CLIENTS),
                new DeleteClientCommand(INDEX_FIRST_CLIENT).getRequiredDisplayType());
        assertEquals(java.util.Optional.empty(),
                new DeleteClientCommand(model.getBubuBook().getClientList().get(0).getPhone())
                        .getRequiredDisplayType());
    }
    //@@author

    @Test
    public void equals() {
        DeleteClientCommand deleteFirstCommand = new DeleteClientCommand(INDEX_FIRST_CLIENT);
        DeleteClientCommand deleteSecondCommand = new DeleteClientCommand(INDEX_SECOND_CLIENT);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteClientCommand deleteFirstCommandCopy = new DeleteClientCommand(INDEX_FIRST_CLIENT);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different client -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));

        Phone phone = model.getBubuBook().getClientList().get(0).getPhone();
        assertTrue(new DeleteClientCommand(phone).equals(new DeleteClientCommand(phone)));
        assertFalse(new DeleteClientCommand(phone).equals(new DeleteClientCommand(new Phone("81111112"))));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteClientCommand deleteCommand = new DeleteClientCommand(targetIndex);
        String expected = DeleteClientCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, deleteCommand.toString());

        Phone phone = model.getBubuBook().getClientList().get(0).getPhone();
        DeleteClientCommand phoneDeleteCommand = new DeleteClientCommand(phone);
        String phoneExpected = DeleteClientCommand.class.getCanonicalName() + "{phoneNumber=" + phone + "}";
        assertEquals(phoneExpected, phoneDeleteCommand.toString());
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoClient(Model model) {
        model.updateFilteredClientList(p -> false);

        assertTrue(model.getFilteredClientList().isEmpty());
    }
}

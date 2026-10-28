package seedu.address.logic.commands.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showClientAtIndex;
import static seedu.address.testutil.TypicalClients.getTypicalBubuBook;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_CLIENT;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.client.EditClientCommand.EditClientDescriptor;
import seedu.address.model.BubuBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.EditClientDescriptorBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditClientCommand.
 */
public class EditClientCommandTest {

    private Model model = new ModelManager(getTypicalBubuBook(), new UserPrefs());
    //@@author anderschow
    private final Phone firstPhone = model.getBubuBook().getClientList().get(0).getPhone();
    private final Phone secondPhone = model.getBubuBook().getClientList().get(1).getPhone();

    @Test
    public void execute_indexInFilteredList_preservesFilterAndUsesDisplayedOrder() throws Exception {
        Client first = model.getBubuBook().getClientList().get(0);
        Client second = model.getBubuBook().getClientList().get(1);
        showClientAtIndex(model, INDEX_SECOND_CLIENT);
        new EditClientCommand(INDEX_FIRST_CLIENT,
                new EditClientDescriptorBuilder().withName("Zoe Tan").build()).execute(model);
        assertEquals(first, model.getBubuBook().getClientList().get(0));
        assertEquals(new ClientBuilder(second).withName("Zoe Tan").build(),
                model.getBubuBook().getClientList().get(1));
        assertTrue(model.getFilteredClientList().isEmpty());
    }

    @Test
    public void execute_indexOutOfRange_failure() {
        assertCommandFailure(new EditClientCommand(Index.fromOneBased(model.getFilteredClientList().size() + 1),
                DESC_AMY), model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
        showClientAtIndex(model, INDEX_FIRST_CLIENT);
        assertCommandFailure(new EditClientCommand(INDEX_SECOND_CLIENT, DESC_AMY),
                model, Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_indexDuplicateHiddenClient_failure() {
        Client hidden = model.getBubuBook().getClientList().get(1);
        showClientAtIndex(model, INDEX_FIRST_CLIENT);
        assertCommandFailure(new EditClientCommand(INDEX_FIRST_CLIENT,
                new EditClientDescriptorBuilder().withPhone(hidden.getPhone().value).build()),
                model, EditClientCommand.MESSAGE_DUPLICATE_PHONE);
    }

    @Test
    public void equals_indexTargets() {
        EditClientCommand command = new EditClientCommand(INDEX_FIRST_CLIENT, DESC_AMY);
        assertEquals(command, new EditClientCommand(INDEX_FIRST_CLIENT, DESC_AMY));
        assertFalse(command.equals(new EditClientCommand(INDEX_SECOND_CLIENT, DESC_AMY)));
        assertFalse(command.equals(new EditClientCommand(firstPhone, DESC_AMY)));
        assertFalse(new EditClientCommand(firstPhone, DESC_AMY).equals(command));
        assertFalse(command.equals(new EditClientCommand(INDEX_FIRST_CLIENT, DESC_BOB)));
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Client editedClient = new ClientBuilder().build();
        EditClientDescriptor descriptor = new EditClientDescriptorBuilder(editedClient).build();
        EditClientCommand editCommand = new EditClientCommand(firstPhone, descriptor);

        String expectedMessage = String.format(EditClientCommand.MESSAGE_EDIT_CLIENT_SUCCESS,
                Messages.format(editedClient));

        Model expectedModel = new ModelManager(new BubuBook(model.getBubuBook()), new UserPrefs());
        expectedModel.setClient(model.getFilteredClientList().get(0), editedClient);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel, CommandResult.DisplayType.CLIENTS);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastClient = Index.fromOneBased(model.getFilteredClientList().size());
        Client lastClient = model.getFilteredClientList().get(indexLastClient.getZeroBased());

        ClientBuilder clientInList = new ClientBuilder(lastClient);
        Client editedClient = clientInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditClientDescriptor descriptor = new EditClientDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();
        EditClientCommand editCommand = new EditClientCommand(lastClient.getPhone(), descriptor);

        String expectedMessage = String.format(EditClientCommand.MESSAGE_EDIT_CLIENT_SUCCESS,
                Messages.format(editedClient));

        Model expectedModel = new ModelManager(new BubuBook(model.getBubuBook()), new UserPrefs());
        expectedModel.setClient(lastClient, editedClient);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel, CommandResult.DisplayType.CLIENTS);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditClientCommand editCommand = new EditClientCommand(firstPhone, new EditClientDescriptor());
        Client editedClient = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());

        String expectedMessage = String.format(EditClientCommand.MESSAGE_EDIT_CLIENT_SUCCESS,
                Messages.format(editedClient));

        Model expectedModel = new ModelManager(new BubuBook(model.getBubuBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel, CommandResult.DisplayType.CLIENTS);
    }

    @Test
    public void execute_filteredList_success() {
        showClientAtIndex(model, INDEX_FIRST_CLIENT);

        Client clientInFilteredList = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());
        Client editedClient = new ClientBuilder(clientInFilteredList).withName(VALID_NAME_BOB).build();
        EditClientCommand editCommand = new EditClientCommand(firstPhone,
                new EditClientDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditClientCommand.MESSAGE_EDIT_CLIENT_SUCCESS,
                Messages.format(editedClient));

        Model expectedModel = new ModelManager(new BubuBook(model.getBubuBook()), new UserPrefs());
        expectedModel.updateFilteredClientList(clientInFilteredList::equals);
        expectedModel.setClient(model.getFilteredClientList().get(0), editedClient);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel, CommandResult.DisplayType.CLIENTS);
    }

    @Test
    public void execute_duplicateClientUnfilteredList_failure() {
        Client firstClient = model.getFilteredClientList().get(INDEX_FIRST_CLIENT.getZeroBased());
        EditClientDescriptor descriptor = new EditClientDescriptorBuilder(firstClient).build();
        EditClientCommand editCommand = new EditClientCommand(secondPhone, descriptor);

        assertCommandFailure(editCommand, model, EditClientCommand.MESSAGE_DUPLICATE_CLIENT);
    }

    @Test
    public void execute_duplicateClientFilteredList_failure() {
        showClientAtIndex(model, INDEX_FIRST_CLIENT);

        // edit client in filtered list into a duplicate in BuBu
        Client clientInList = model.getBubuBook().getClientList().get(INDEX_SECOND_CLIENT.getZeroBased());
        EditClientCommand editCommand = new EditClientCommand(firstPhone,
                new EditClientDescriptorBuilder(clientInList).build());

        assertCommandFailure(editCommand, model, EditClientCommand.MESSAGE_DUPLICATE_CLIENT);
    }
    //@@author

    //@@author anderschow
    @Test
    public void execute_unknownPhone_failure() {
        EditClientCommand command = new EditClientCommand(new Phone("89998888"),
                new EditClientDescriptorBuilder().withName(VALID_NAME_BOB).build());
        assertCommandFailure(command, model, EditClientCommand.MESSAGE_CLIENT_NOT_FOUND);
    }

    @Test
    public void execute_hiddenClient_success() throws Exception {
        Client hidden = model.getFilteredClientList().get(1);
        showClientAtIndex(model, INDEX_FIRST_CLIENT);
        new EditClientCommand(hidden.getPhone(),
                new EditClientDescriptorBuilder().withName("Zoe Tan").build()).execute(model);
        assertEquals("Zoe Tan", model.getBubuBook().getClientList().get(1).getName().fullName);
    }
    //@@author
    //@@author anderschow
    @Test
    public void equals() {
        final EditClientCommand standardCommand = new EditClientCommand(firstPhone, DESC_AMY);

        // same values -> returns true
        EditClientDescriptor copyDescriptor = new EditClientDescriptor(DESC_AMY);
        EditClientCommand commandWithSameValues = new EditClientCommand(firstPhone, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different identifier -> returns false
        assertFalse(standardCommand.equals(new EditClientCommand(secondPhone, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditClientCommand(firstPhone, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Phone identifier = new Phone("91234567");
        EditClientDescriptor editClientDescriptor = new EditClientDescriptor();
        EditClientCommand editCommand = new EditClientCommand(identifier, editClientDescriptor);
        String expected = EditClientCommand.class.getCanonicalName() + "{identifier=" + identifier
                + ", editClientDescriptor=" + editClientDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }
    //@@author


    //@@author anderschow
    @Test
    public void execute_changeOnlyPhoneOrEmailToHiddenClient_failure() {
        Client other = model.getBubuBook().getClientList().get(1);
        showClientAtIndex(model, INDEX_FIRST_CLIENT);
        EditClientCommand duplicateEmail = new EditClientCommand(firstPhone,
                new EditClientDescriptorBuilder().withEmail(other.getEmail().value).build());
        EditClientCommand duplicatePhone = new EditClientCommand(firstPhone,
                new EditClientDescriptorBuilder().withPhone(other.getPhone().value).build());
        assertCommandFailure(duplicateEmail, model, EditClientCommand.MESSAGE_DUPLICATE_EMAIL);
        assertCommandFailure(duplicatePhone, model, EditClientCommand.MESSAGE_DUPLICATE_PHONE);
    }
    //@@author

}

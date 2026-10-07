package seedu.address.logic.commands.client;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalClients.getTypicalClientBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.Client;
import seedu.address.testutil.ClientBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddClientCommand}.
 */
public class AddClientCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalClientBook(), new UserPrefs());
    }

    @Test
    public void execute_newClient_success() {
        Client validClient = new ClientBuilder().build();

        Model expectedModel = new ModelManager(model.getClientBook(), model.getPetBook(), new UserPrefs());
        expectedModel.addClient(validClient);

        assertCommandSuccess(new AddClientCommand(validClient), model,
                String.format(AddClientCommand.MESSAGE_SUCCESS, validClient.getName(), validClient.getPhone()),
                expectedModel);
    }

    @Test
    public void execute_duplicateClient_throwsCommandException() {
        Client clientInList = model.getClientBook().getClientList().get(0);
        assertCommandFailure(new AddClientCommand(clientInList), model,
                AddClientCommand.MESSAGE_DUPLICATE_CLIENT);
    }


    @Test
    public void execute_sharedNameAndAddress_success() {
        Client existing = model.getClientBook().getClientList().get(0);
        Client client = new ClientBuilder(existing).withPhone("81234567")
                .withEmail("another@example.com").build();
        Model expectedModel = new ModelManager(model.getClientBook(), model.getPetBook(), new UserPrefs());
        expectedModel.addClient(client);
        assertCommandSuccess(new AddClientCommand(client), model,
                String.format(AddClientCommand.MESSAGE_SUCCESS, client.getName(), client.getPhone()), expectedModel);
    }

    @Test
    public void execute_sharedPhoneOrEmailInFilteredList_failure() {
        Client existing = model.getClientBook().getClientList().get(0);
        model.updateFilteredClientList(client -> false);
        Client samePhone = new ClientBuilder().withPhone(existing.getPhone().value).build();
        Client sameEmail = new ClientBuilder().withEmail(existing.getEmail().value).build();
        assertCommandFailure(new AddClientCommand(samePhone), model, AddClientCommand.MESSAGE_DUPLICATE_CLIENT);
        assertCommandFailure(new AddClientCommand(sameEmail), model, AddClientCommand.MESSAGE_DUPLICATE_CLIENT);
    }

}

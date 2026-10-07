package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalClients.getTypicalClientBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.ClientBook;
import seedu.address.testutil.PetBuilder;
import seedu.address.testutil.TypicalClients;

public class ClearCommandTest {

    @Test
    public void execute_emptyClientBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyClientBook_success() {
        Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalClientBook(), new UserPrefs());
        expectedModel.setClientBook(new ClientBook());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }


    @Test
    public void execute_clientsAndPets_clearsBothBooks() {
        Model model = new ModelManager();
        model.addClient(TypicalClients.ALICE);
        model.addPet(new PetBuilder(TypicalClients.ALICE).build());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, new ModelManager());
    }
}

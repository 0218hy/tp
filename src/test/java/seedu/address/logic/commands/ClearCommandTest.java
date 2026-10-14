package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalClients.getTypicalGroomingBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.GroomingBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyGroomingBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyGroomingBook_success() {
        Model model = new ModelManager(getTypicalGroomingBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalGroomingBook(), new UserPrefs());
        expectedModel.setGroomingBook(new GroomingBook());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

}

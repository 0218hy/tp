package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class HelpCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void execute_help_success() {
        CommandResult expectedCommandResult = new CommandResult(HelpCommand.getCommandReference(), true, false);
        assertCommandSuccess(new HelpCommand(), model, expectedCommandResult, expectedModel);
    }

    @Test
    public void getCommandReference_containsAllDocumentedCommands() {
        String commandReference = HelpCommand.getCommandReference();

        assertTrue(commandReference.contains("help"));
        assertTrue(commandReference.contains("add-client n/NAME i/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]..."));
        assertTrue(commandReference.contains("list"));
        assertTrue(commandReference.contains("edit INDEX [n/NAME] [i/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]..."));
        assertTrue(commandReference.contains("find KEYWORD [MORE_KEYWORDS]"));
        assertTrue(commandReference.contains("delete-client INDEX"));
        assertTrue(commandReference.contains("delete-client i/PHONE_NUMBER"));
        assertTrue(commandReference.contains(
                "add-pet p/PET_NAME i/OWNER_IDENTIFIER s/SPECIES [b/BREED] r/REQUIREMENTS"));
        assertTrue(commandReference.contains("delete-pet p/PET_NAME i/OWNER_IDENTIFIER"));
        assertTrue(commandReference.contains("clear"));
        assertTrue(commandReference.contains("exit"));
    }
}

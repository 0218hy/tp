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

    //@@author anderschow
    @Test
    public void getCommandReference_containsAllDocumentedCommands() {
        String commandReference = HelpCommand.getCommandReference();

        assertTrue(commandReference.contains("help"));
        assertTrue(commandReference.contains("add-client n/NAME i/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]..."));
        assertTrue(commandReference.contains(
                "edit-client i/CURRENT_PHONE [n/NAME] [p/NEW_PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]..."));
        assertTrue(commandReference.contains("delete-client INDEX"));
        assertTrue(commandReference.contains("delete-client i/PHONE_NUMBER"));
        assertTrue(commandReference.contains(
                "add-pet p/PET_NAME i/OWNER_IDENTIFIER s/SPECIES [b/BREED] r/REQUIREMENTS"));
        assertTrue(commandReference.contains("list-clients [q/KEYWORD]"));
        assertTrue(commandReference.contains("list-pets [i/OWNER_IDENTIFIER]"));
        assertTrue(commandReference.contains(
                "edit-pet p/PET_NAME i/OWNER_IDENTIFIER [n/NEW_PET_NAME] [s/SPECIES] [b/BREED] [r/REQUIREMENTS]"));
        assertTrue(commandReference.contains("delete-pet INDEX"));
        assertTrue(commandReference.contains("delete-pet p/PET_NAME i/OWNER_IDENTIFIER"));
        assertTrue(commandReference.contains(
                "add-appointment i/OWNER_IDENTIFIER p/PET_NAME d/DD-MM-YYYY st/HH:mm et/HH:mm s/SERVICE"));
        assertTrue(commandReference.contains("list-appointments"));
        assertTrue(commandReference.contains("delete-appointment INDEX"));
        assertTrue(commandReference.contains("delete-appointment d/DD-MM-YYYY st/HH:mm"));
        assertTrue(commandReference.contains("clear"));
        assertTrue(commandReference.contains("exit"));
    }
    //@@author
}

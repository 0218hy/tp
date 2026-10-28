package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;

/** Contains unit tests for command-history navigation in {@code CommandBox}. */
public class CommandBoxTest {

    //@@author khangsian0412
    @Test
    public void commandHistory_emptyHistory_doesNotConsumeArrowKeys() {
        JavaFxTestUtils.runAndWait(() -> {
            List<String> executedCommands = new ArrayList<>();
            CommandBox commandBox = new CommandBox(command -> {
                executedCommands.add(command);
                return new CommandResult("Executed");
            });
            TextField commandTextField = getCommandTextField(commandBox);

            submit(commandTextField, "");
            commandTextField.setText("draft command");
            KeyEvent upEvent = press(commandTextField, KeyCode.UP);
            KeyEvent downEvent = press(commandTextField, KeyCode.DOWN);

            assertEquals("draft command", commandTextField.getText());
            assertFalse(upEvent.isConsumed());
            assertFalse(downEvent.isConsumed());
            assertTrue(executedCommands.isEmpty());
        });
    }

    @Test
    public void commandHistory_navigatesInOrderAndStopsAtBothEnds() {
        JavaFxTestUtils.runAndWait(() -> {
            List<String> executedCommands = new ArrayList<>();
            CommandBox commandBox = new CommandBox(command -> {
                executedCommands.add(command);
                return new CommandResult("Executed");
            });
            TextField commandTextField = getCommandTextField(commandBox);

            submit(commandTextField, "list-pets");
            submit(commandTextField, "help");
            submit(commandTextField, "list-pets");
            press(commandTextField, KeyCode.LEFT);
            assertCommandText(commandTextField, "");

            press(commandTextField, KeyCode.UP);
            assertCommandText(commandTextField, "list-pets");
            press(commandTextField, KeyCode.UP);
            assertCommandText(commandTextField, "help");
            press(commandTextField, KeyCode.UP);
            assertCommandText(commandTextField, "list-pets");
            press(commandTextField, KeyCode.UP);
            assertCommandText(commandTextField, "list-pets");

            press(commandTextField, KeyCode.DOWN);
            assertCommandText(commandTextField, "help");
            press(commandTextField, KeyCode.DOWN);
            assertCommandText(commandTextField, "list-pets");
            press(commandTextField, KeyCode.DOWN);
            assertCommandText(commandTextField, "");
            press(commandTextField, KeyCode.DOWN);
            assertCommandText(commandTextField, "");

            assertEquals(List.of("list-pets", "help", "list-pets"), executedCommands);
        });
    }

    @Test
    public void commandHistory_navigatingUp_preservesUnsubmittedDraft() {
        JavaFxTestUtils.runAndWait(() -> {
            CommandBox commandBox = new CommandBox(command -> new CommandResult("Executed"));
            TextField commandTextField = getCommandTextField(commandBox);

            submit(commandTextField, "list-pets");
            submit(commandTextField, "help");
            commandTextField.setText("add-pet p/BuBu");

            press(commandTextField, KeyCode.UP);
            assertCommandText(commandTextField, "help");
            press(commandTextField, KeyCode.DOWN);
            assertCommandText(commandTextField, "add-pet p/BuBu");
            press(commandTextField, KeyCode.DOWN);
            assertCommandText(commandTextField, "");
        });
    }

    @Test
    public void commandHistory_failedSubmissionIsRecordedAndIndicatesFailure() {
        JavaFxTestUtils.runAndWait(() -> {
            CommandBox commandBox = new CommandBox(command -> {
                if (command.equals("invalid-command")) {
                    throw new CommandException("Invalid command");
                }
                return new CommandResult("Executed");
            });
            TextField commandTextField = getCommandTextField(commandBox);

            submit(commandTextField, "list-pets");
            submit(commandTextField, "invalid-command");
            submit(commandTextField, "invalid-command");

            assertEquals("invalid-command", commandTextField.getText());
            assertTrue(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

            press(commandTextField, KeyCode.UP);
            assertCommandText(commandTextField, "invalid-command");
            assertTrue(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            press(commandTextField, KeyCode.UP);
            assertCommandText(commandTextField, "invalid-command");
            assertTrue(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            press(commandTextField, KeyCode.UP);
            assertCommandText(commandTextField, "list-pets");
            assertFalse(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    private static void submit(TextField commandTextField, String command) {
        commandTextField.setText(command);
        commandTextField.fireEvent(new ActionEvent());
    }

    private static KeyEvent press(TextField commandTextField, KeyCode keyCode) {
        KeyEvent event = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode,
                false, false, false, false);
        commandTextField.fireEvent(event);
        return event;
    }

    private static void assertCommandText(TextField commandTextField, String expectedText) {
        assertEquals(expectedText, commandTextField.getText());
        assertEquals(expectedText.length(), commandTextField.getCaretPosition());
    }

    private static TextField getCommandTextField(CommandBox commandBox) {
        try {
            Field commandTextField = CommandBox.class.getDeclaredField("commandTextField");
            commandTextField.setAccessible(true);
            return (TextField) commandTextField.get(commandBox);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new AssertionError(exception);
        }
    }
    //@@author
}

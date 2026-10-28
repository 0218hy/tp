package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex;

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleHistoryNavigation);
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.equals("")) {
            return;
        }

        commandHistory.add(commandText);
        historyIndex = commandHistory.size();

        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    //@@author khangsian0412
    /**
     * Loads earlier or later submitted commands when the user presses the Up or Down arrow key.
     * The history stops at its oldest entry and at the blank position after its newest entry.
     */
    private void handleHistoryNavigation(KeyEvent event) {
        if (commandHistory.isEmpty()) {
            return;
        }

        if (event.getCode() == KeyCode.UP) {
            saveCurrentCommandBeforeLoadingPrevious();
            loadPreviousCommand();
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN) {
            loadNextCommand();
            event.consume();
        }
    }

    /**
     * Saves a non-empty command being drafted before navigating away from it.
     * An unchanged recalled command is already present in the history and is not added again.
     */
    private void saveCurrentCommandBeforeLoadingPrevious() {
        String currentCommand = commandTextField.getText();
        boolean isAtBlankPosition = historyIndex == commandHistory.size();
        int currentEntryIndex = isAtBlankPosition ? commandHistory.size() - 1 : historyIndex;
        boolean isNewOrEditedCommand = !currentCommand.equals(commandHistory.get(currentEntryIndex));
        if (currentCommand.isEmpty() || !isNewOrEditedCommand) {
            return;
        }

        commandHistory.add(currentCommand);
        historyIndex = commandHistory.size() - 1;
    }

    /** Loads the next oldest command, if one exists. */
    private void loadPreviousCommand() {
        if (historyIndex == 0) {
            return;
        }

        historyIndex--;
        setCommandText(commandHistory.get(historyIndex));
    }

    /** Loads the next newest command, or clears the field after the newest command. */
    private void loadNextCommand() {
        if (historyIndex >= commandHistory.size()) {
            return;
        }

        historyIndex++;
        if (historyIndex == commandHistory.size()) {
            setCommandText("");
            return;
        }

        setCommandText(commandHistory.get(historyIndex));
    }

    /** Replaces the command field text and positions its caret at the end for immediate editing. */
    private void setCommandText(String commandText) {
        commandTextField.setText(commandText);
        commandTextField.positionCaret(commandText.length());
    }
    //@@author

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}

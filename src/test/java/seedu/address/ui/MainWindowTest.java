package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.client.Client;

@EnabledOnOs(OS.LINUX)
public class MainWindowTest {

    @Test
    public void executeCommand_helpAndRegularCommand_updatesResultDisplay() {
        JavaFxTestUtils.runAndWait(() -> {
            StubLogic logic = new StubLogic();
            MainWindow mainWindow = new MainWindow(new Stage(), logic, Path.of("addressbook.json"));
            mainWindow.fillInnerParts();

            logic.nextResult = new CommandResult("AVAILABLE COMMANDS\n\nHelp\n  help", true, false);
            mainWindow.handleHelp();

            StackPane resultDisplayPlaceholder = getField(mainWindow, "resultDisplayPlaceholder", StackPane.class);
            ResultDisplay resultDisplay = getField(mainWindow, "resultDisplay", ResultDisplay.class);
            ScrollPane helpDisplay = getField(resultDisplay, "helpDisplay", ScrollPane.class);
            assertEquals(HelpCommand.COMMAND_WORD, logic.lastCommand);
            assertTrue(helpDisplay.isVisible());
            assertEquals(300, resultDisplayPlaceholder.getPrefHeight());

            logic.nextResult = new CommandResult("Listed all clients.");
            executeCommand(mainWindow, "list");

            assertFalse(helpDisplay.isVisible());
            assertEquals(100, resultDisplayPlaceholder.getPrefHeight());
        });
    }

    private static void executeCommand(MainWindow mainWindow, String commandText) {
        try {
            Method executeCommand = MainWindow.class.getDeclaredMethod("executeCommand", String.class);
            executeCommand.setAccessible(true);
            executeCommand.invoke(mainWindow, commandText);
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw new AssertionError(exception);
        } catch (InvocationTargetException exception) {
            throw new AssertionError(exception.getCause());
        }
    }

    private static <T> T getField(Object target, String fieldName, Class<T> fieldType) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return fieldType.cast(field.get(target));
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new AssertionError(exception);
        }
    }

    private static class StubLogic implements Logic {
        private final ObservableList<Client> clients = FXCollections.observableArrayList();
        private CommandResult nextResult = new CommandResult("");
        private GuiSettings guiSettings = new GuiSettings();
        private String lastCommand;

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            lastCommand = commandText;
            return nextResult;
        }

        @Override
        public ObservableList<Client> getFilteredClientList() {
            return clients;
        }

        @Override
        public GuiSettings getGuiSettings() {
            return guiSettings;
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            this.guiSettings = guiSettings;
        }
    }
}

package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
//@@author anderschow
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
//@@author

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.CommandResult.DisplayType;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.testutil.PetBuilder;
import seedu.address.testutil.TypicalClients;
import seedu.address.ui.appointment.AppointmentListPanel;
import seedu.address.ui.client.ClientListPanel;
import seedu.address.ui.pet.PetListPanel;

public class MainWindowTest {

    //@@author anderschow
    @Test
    public void handleHelp_commandFailure_logsCauseAndAllowsRetry() {
        JavaFxTestUtils.runAndWait(() -> {
            CommandException failure = new CommandException("Help unavailable");
            StubLogic logic = new StubLogic();
            logic.failure = failure;
            MainWindow window = new MainWindow(new Stage(), logic, Path.of("bubu.json"));
            window.fillInnerParts();
            List<LogRecord> records = new ArrayList<>();
            Logger logger = Logger.getLogger("ab3.MainWindow");
            Handler handler = new Handler() {
                @Override
                public void publish(LogRecord record) {
                    records.add(record);
                }

                @Override
                public void flush() {}

                @Override
                public void close() {}
            };
            logger.addHandler(handler);
            try {
                window.handleHelp();
                assertTrue(records.stream().anyMatch(record -> record.getThrown() == failure
                        && record.getMessage().equals("Unable to display the command reference")));
                logic.failure = null;
                logic.nextResult = new CommandResult("AVAILABLE COMMANDS", true, false);
                window.handleHelp();
                ResultDisplay display = getField(window, "resultDisplay", ResultDisplay.class);
                assertTrue(getField(display, "helpDisplay", ScrollPane.class).isVisible());
            } finally {
                logger.removeHandler(handler);
            }
        });
    }
    //@@author

    //@@author khangsian0412
    @Test
    public void executeCommand_helpAndRegularCommand_updatesResultDisplay() {
        JavaFxTestUtils.runAndWait(() -> {
            StubLogic logic = new StubLogic();
            MainWindow mainWindow = new MainWindow(new Stage(), logic, Path.of("bubu.json"));
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
            executeCommand(mainWindow, "list-clients");

            assertFalse(helpDisplay.isVisible());
            assertEquals(100, resultDisplayPlaceholder.getPrefHeight());
        });
    }
    //@@author

    //@@author chunghean
    @Test
    public void executeCommand_displayTypeSwitchesVisibleListPanel() {
        JavaFxTestUtils.runAndWait(() -> {
            StubLogic logic = new StubLogic();
            MainWindow mainWindow = new MainWindow(new Stage(), logic, Path.of("bubu.json"));
            mainWindow.fillInnerParts();

            ClientListPanel clientListPanel = getField(mainWindow, "clientListPanel", ClientListPanel.class);
            PetListPanel petListPanel = getField(mainWindow, "petListPanel", PetListPanel.class);
            AppointmentListPanel appointmentListPanel =
                    getField(mainWindow, "appointmentListPanel", AppointmentListPanel.class);
            assertTrue(clientListPanel.getRoot().isVisible());
            assertFalse(petListPanel.getRoot().isVisible());
            assertFalse(appointmentListPanel.getRoot().isVisible());

            logic.nextResult = new CommandResult("Listed pets.", DisplayType.PETS);
            executeCommand(mainWindow, "list-pets");
            assertFalse(clientListPanel.getRoot().isVisible());
            assertTrue(petListPanel.getRoot().isVisible());
            assertFalse(appointmentListPanel.getRoot().isVisible());

            logic.nextResult = new CommandResult("Listed appointments.", DisplayType.APPOINTMENTS);
            executeCommand(mainWindow, "list-appointments");
            assertFalse(clientListPanel.getRoot().isVisible());
            assertFalse(petListPanel.getRoot().isVisible());
            assertTrue(appointmentListPanel.getRoot().isVisible());
        });
    }
    //@@author

    // @@author xinyu-zxy
    @Test
    public void executeCommand_recordHeading_tracksVisibleListAndCount() {
        JavaFxTestUtils.runAndWait(() -> {
            StubLogic logic = new StubLogic();
            MainWindow mainWindow = new MainWindow(new Stage(), logic, Path.of("bubu.json"));
            mainWindow.fillInnerParts();
            Label title = getField(mainWindow, "recordTitle", Label.class);
            Label count = getField(mainWindow, "recordCount", Label.class);

            // EP: empty displayed list
            // BVA: zero displayed records
            assertEquals("👥 Clients", title.getText());
            assertEquals("0 records", count.getText());

            logic.nextResult = new CommandResult("Listed all pets.", DisplayType.PETS);
            executeCommand(mainWindow, "list-pets");
            assertEquals("🐾 Pets", title.getText());

            // EP: displayed list containing exactly one record
            // BVA: one record, the smallest non-empty list
            logic.pets.add(new PetBuilder(TypicalClients.ALICE).build());
            assertEquals("1 record", count.getText());

            // EP: displayed list containing multiple records
            logic.pets.add(new PetBuilder(TypicalClients.ALICE).withName("Luna").build());
            assertEquals("2 records", count.getText());
            logic.pets.clear();
            assertEquals("0 records", count.getText());

            logic.nextResult = new CommandResult("Appointments", DisplayType.APPOINTMENTS);
            executeCommand(mainWindow, "list-appointments");
            assertEquals("📅 Appointments", title.getText());

            // EP: update to a hidden list does not change the visible list's count
            logic.pets.add(new PetBuilder(TypicalClients.ALICE).build());
            assertEquals("0 records", count.getText());

            logic.nextResult = new CommandResult("Help", true, false);
            mainWindow.handleHelp();

            // EP: command that leaves the current display type unchanged
            assertEquals("📅 Appointments", title.getText());
        });
    }
    // @@author

    //@@author khangsian0412
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
    //@@author

    private static class StubLogic implements Logic {
        //@@author anderschow
        private CommandException failure;
        //@@author
        private final ObservableList<Client> clients = FXCollections.observableArrayList();
        private final ObservableList<Pet> pets = FXCollections.observableArrayList();
        private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        private CommandResult nextResult = new CommandResult("");
        private GuiSettings guiSettings = new GuiSettings();
        private String lastCommand;

        //@@author khangsian0412
        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            lastCommand = commandText;
            //@@author anderschow
            if (failure != null) {
                throw failure;
            }
            //@@author khangsian0412
            return nextResult;
        }
        //@@author

        @Override
        public ObservableList<Client> getFilteredClientList() {
            return clients;
        }

        @Override
        public ObservableList<Pet> getFilteredPetList() {
            return pets;
        }

        @Override
        public ObservableList<Appointment> getFilteredAppointmentList() {
            return appointments;
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

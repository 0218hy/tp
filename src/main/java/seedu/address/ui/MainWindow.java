package seedu.address.ui;

import java.nio.file.Path;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.CommandResult.DisplayType;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.ui.appointment.AppointmentListPanel;
import seedu.address.ui.client.ClientListPanel;
import seedu.address.ui.pet.PetListPanel;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final String CLIENTS_TITLE = "👥 Clients";
    private static final String PETS_TITLE = "🐾 Pets";
    private static final String APPOINTMENTS_TITLE = "📅 Appointments";
    private static final String SINGLE_RECORD_FORMAT = "%d record";
    private static final String MULTIPLE_RECORDS_FORMAT = "%d records";
    private static final double DEFAULT_RESULT_DISPLAY_HEIGHT = 100;
    private static final double HELP_RESULT_DISPLAY_HEIGHT = 300;

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private ClientListPanel clientListPanel;
    private PetListPanel petListPanel;
    private AppointmentListPanel appointmentListPanel;
    private ResultDisplay resultDisplay;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane recordListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private Label recordTitle;

    @FXML
    private Label recordCount;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and execute
         * the Help command purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        clientListPanel = new ClientListPanel(logic.getFilteredClientList());
        petListPanel = new PetListPanel(logic.getFilteredPetList());
        appointmentListPanel = new AppointmentListPanel(logic.getFilteredAppointmentList());
        recordListPanelPlaceholder.getChildren().addAll(clientListPanel.getRoot(), petListPanel.getRoot(),
                appointmentListPanel.getRoot());
        showList(DisplayType.CLIENTS);

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());
        setResultDisplayHeight(DEFAULT_RESULT_DISPLAY_HEIGHT);

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Displays the command reference in the result display.
     */
    @FXML
    public void handleHelp() {
        try {
            executeCommand(HelpCommand.COMMAND_WORD);
        } catch (CommandException | ParseException exception) {
            logger.warning("Unable to display the command reference: " + exception.getMessage());
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        primaryStage.hide();
    }

    public ClientListPanel getClientListPanel() {
        return clientListPanel;
    }

    private void showList(DisplayType displayType) {
        if (displayType == DisplayType.UNCHANGED) {
            return;
        }

        setListVisibility(clientListPanel.getRoot(), displayType == DisplayType.CLIENTS);
        setListVisibility(petListPanel.getRoot(), displayType == DisplayType.PETS);
        setListVisibility(appointmentListPanel.getRoot(), displayType == DisplayType.APPOINTMENTS);
        switch (displayType) {
            case CLIENTS:
                updateRecordHeading(CLIENTS_TITLE, logic.getFilteredClientList());
                break;
            case PETS:
                updateRecordHeading(PETS_TITLE, logic.getFilteredPetList());
                break;
            case APPOINTMENTS:
                updateRecordHeading(APPOINTMENTS_TITLE, logic.getFilteredAppointmentList());
                break;
            default:
                break;
        }
    }

    // @@author xinyu-zxy
    /**
     * Updates the heading and keeps its count synchronized with the visible list.
     */
    private void updateRecordHeading(String title, ObservableList<?> records) {
        recordTitle.setText(title);
        recordCount.textProperty().unbind();
        recordCount.textProperty().bind(Bindings.createStringBinding(() -> formatRecordCount(records.size()), records));
    }

    private String formatRecordCount(int count) {
        String format = count == 1 ? SINGLE_RECORD_FORMAT : MULTIPLE_RECORDS_FORMAT;
        return String.format(format, count);
    }
    // @@author

    private static void setListVisibility(Region listRoot, boolean visible) {
        listRoot.setVisible(visible);
        listRoot.setManaged(visible);
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            showList(commandResult.getDisplayType());
            if (commandResult.isShowHelp()) {
                resultDisplay.setHelpContent(commandResult.getFeedbackToUser());
            } else {
                resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            }
            setResultDisplayHeight(commandResult.isShowHelp()
                    ? HELP_RESULT_DISPLAY_HEIGHT : DEFAULT_RESULT_DISPLAY_HEIGHT);

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setErrorFeedbackToUser(e.getMessage());
            setResultDisplayHeight(DEFAULT_RESULT_DISPLAY_HEIGHT);
            throw e;
        }
    }

    /**
     * Sets the height of the result display area.
     */
    private void setResultDisplayHeight(double height) {
        resultDisplayPlaceholder.setMinHeight(height);
        resultDisplayPlaceholder.setPrefHeight(height);
        resultDisplayPlaceholder.setMaxHeight(height);
    }
}

package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.CommandResult.DisplayType;
import seedu.address.logic.commands.appointment.ListAppointmentsCommand;
import seedu.address.logic.commands.client.AddClientCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.commands.pet.AddPetCommand;
import seedu.address.logic.commands.pet.ListPetsCommand;
import seedu.address.logic.parser.BuBuParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.BubuBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyBubuBook;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String MESSAGE_CORRUPTED_RECORDS =
            "Unable to verify existing records. Please contact support.";
    public static final String MESSAGE_UNABLE_TO_SAVE_PET = "Unable to save a pet. Please try again.";
    public static final String MESSAGE_CLIENT_SAVE_FAILURE = "Unable to save client.";
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";
    public static final String MESSAGE_INVALID_DISPLAY_TYPE =
            "The current displayed list does not match the record type selected by the index.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final BuBuParser buBuParser;
    private final boolean isBubuBookCorrupted;
    private DisplayType currentDisplayType = DisplayType.CLIENTS;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, false);
    }

    /**
     * Constructs logic with the BubuBook corruption state recorded at startup.
     * Commands cannot save records when this state is true.
     */
    public LogicManager(Model model, Storage storage, boolean isBubuBookCorrupted) {
        this.model = model;
        this.storage = storage;
        this.buBuParser = new BuBuParser();
        this.isBubuBookCorrupted = isBubuBookCorrupted;
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = buBuParser.parseCommand(commandText);
        if (isBubuBookCorrupted) {
            throw new CommandException(MESSAGE_CORRUPTED_RECORDS);
        }
        ensureRequiredDisplayTypeIsCurrent(command);
        if (command instanceof ListPetsCommand || command instanceof ListAppointmentsCommand) {
            CommandResult commandResult = command.execute(model);
            updateCurrentDisplayType(commandResult);
            return commandResult;
        }
        ReadOnlyBubuBook previousBubuBook = new BubuBook(model.getBubuBook());
        CommandResult commandResult = command.execute(model);
        saveCommandResult(command, previousBubuBook);
        updateCurrentDisplayType(commandResult);
        return commandResult;
    }

    //@@author chunghean
    /**
     * Rejects index-based commands when their required record list is not displayed.
     */
    private void ensureRequiredDisplayTypeIsCurrent(Command command) throws CommandException {
        Optional<DisplayType> requiredDisplayType = command.getRequiredDisplayType();
        if (requiredDisplayType.isPresent() && requiredDisplayType.get() != currentDisplayType) {
            throw new CommandException(MESSAGE_INVALID_DISPLAY_TYPE);
        }
    }

    /**
     * Records the list selected by a successful command for subsequent index-based commands.
     */
    private void updateCurrentDisplayType(CommandResult commandResult) {
        if (commandResult.getDisplayType() != DisplayType.UNCHANGED) {
            currentDisplayType = commandResult.getDisplayType();
        }
    }
    //@@author

    /**
     * Saves the completed command and restores its previous data if saving fails.
     */
    private void saveCommandResult(Command command, ReadOnlyBubuBook previousBubuBook)
            throws CommandException {
        try {
            storage.saveBubuBook(model.getBubuBook());
            logger.info(() -> "Records saved successfully after " + command.getClass().getSimpleName());
        } catch (IOException ioe) {
            model.setBubuBook(previousBubuBook);
            logger.log(Level.WARNING, "Saving records failed; previous model state restored", ioe);
            if (command instanceof AddPetCommand) {
                throw new CommandException(MESSAGE_UNABLE_TO_SAVE_PET, ioe);
            }
            if (command instanceof AddClientCommand) {
                throw new CommandException(MESSAGE_CLIENT_SAVE_FAILURE, ioe);
            }
            if (ioe instanceof AccessDeniedException) {
                throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, ioe.getMessage()), ioe);
            }
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }
    }

    @Override
    public ObservableList<Client> getFilteredClientList() {
        return model.getFilteredClientList();
    }

    @Override
    public ObservableList<Pet> getFilteredPetList() {
        return model.getFilteredPetList();
    }

    @Override
    public ObservableList<Appointment> getFilteredAppointmentList() {
        return model.getFilteredAppointmentList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}

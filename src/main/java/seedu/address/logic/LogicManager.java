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
//@@author anderschow
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
//@@author
import seedu.address.logic.commands.appointment.ListAppointmentsCommand;
import seedu.address.logic.commands.client.AddClientCommand;
//@@author anderschow
import seedu.address.logic.commands.client.EditClientCommand;
import seedu.address.logic.commands.client.ListClientsCommand;
//@@author
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
import seedu.address.storage.RecordSaveConflictException;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String MESSAGE_CORRUPTED_RECORDS =
            "Unable to verify existing records. Please contact support.";
    public static final String MESSAGE_UNABLE_TO_SAVE_PET = "Unable to save a pet. Please try again.";
    //@@author anderschow
    public static final String MESSAGE_CLIENT_SAVE_FAILURE =
            "New client was not saved due to a storage error. Previous records were restored in memory. "
                    + "Check that the data folder is available and has free space, then retry.";
    public static final String MESSAGE_CLIENT_SAVE_PERMISSION_FAILURE =
            "New client was not saved because write access was denied. Previous records were restored in memory. "
                    + "Check write permissions for the data file and folder, then retry.";
    public static final String MESSAGE_CLIENT_EDIT_SAVE_FAILURE =
            "Client update was not saved due to a storage error. Previous records were restored in memory. "
                    + "Check that the data folder is available and has free space, then retry.";
    public static final String MESSAGE_CLIENT_EDIT_SAVE_PERMISSION_FAILURE =
            "Client update was not saved because write access was denied. Previous records were restored in memory. "
                    + "Check write permissions for the data file and folder, then retry.";
    //@@author
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
        //@@author anderschow
        String commandType = CommandLog.commandType(commandText);
        Command command;
        try {
            command = buBuParser.parseCommand(commandText);
            if (isBubuBookCorrupted && !(command instanceof HelpCommand)) {
                throw new CommandException(MESSAGE_CORRUPTED_RECORDS);
            }
            ensureRequiredDisplayTypeIsCurrent(command);
        } catch (ParseException | CommandException e) {
            logger.fine(commandType + " rejected: " + CommandLog.rejectionReason(e));
            throw e;
            //@@author
        }
        ReadOnlyBubuBook previousBubuBook = new BubuBook(model.getBubuBook());
        //@@author anderschow
        CommandResult commandResult;
        try {
            commandResult = command.execute(model);
        } catch (CommandException e) {
            logger.fine(commandType + " rejected: " + CommandLog.rejectionReason(e));
            throw e;
        }
        if (isReadOnly(command) || previousBubuBook.equals(model.getBubuBook())) {
            logger.fine(commandType + " completed");
        } else {
            saveCommandResult(command, previousBubuBook);
            logger.info(commandType + " saved" + CommandLog.changedClientFields(previousBubuBook, model.getBubuBook()));
        }
        //@@author
        updateCurrentDisplayType(commandResult);
        return commandResult;
    }

    /** Returns whether a command only changes the view or requests a UI action. */
    //@@author anderschow
    private static boolean isReadOnly(Command command) {
        return command instanceof HelpCommand || command instanceof ExitCommand
                || command instanceof ListClientsCommand || command instanceof ListPetsCommand
                || command instanceof ListAppointmentsCommand;
    }
    //@@author

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

    //@@author anderschow
    /**
     * Saves the completed command and restores its previous data if saving fails.
     */
    private void saveCommandResult(Command command, ReadOnlyBubuBook previousBubuBook)
            throws CommandException {
        try {
            storage.saveBubuBook(model.getBubuBook());
        } catch (IOException ioe) {
            model.setBubuBook(previousBubuBook);
            logger.log(Level.WARNING, command.getClass().getSimpleName()
                    + " save failed; previous model state restored", ioe);
            //@@author 0218hy
            if (ioe instanceof RecordSaveConflictException) {
                throw new CommandException(ioe.getMessage(), ioe);
            }
            //@@author
            if (command instanceof AddPetCommand) {
                throw new CommandException(MESSAGE_UNABLE_TO_SAVE_PET, ioe);
            }
            if (command instanceof AddClientCommand) {
                throw new CommandException(ioe instanceof AccessDeniedException
                        ? MESSAGE_CLIENT_SAVE_PERMISSION_FAILURE : MESSAGE_CLIENT_SAVE_FAILURE, ioe);
            }
            if (command instanceof EditClientCommand) {
                throw new CommandException(ioe instanceof AccessDeniedException
                        ? MESSAGE_CLIENT_EDIT_SAVE_PERMISSION_FAILURE : MESSAGE_CLIENT_EDIT_SAVE_FAILURE, ioe);
            }
            if (ioe instanceof AccessDeniedException) {
                throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, ioe.getMessage()), ioe);
            }
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }
    }
    //@@author

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

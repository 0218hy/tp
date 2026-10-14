package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.appointment.AddAppointmentCommand;
import seedu.address.logic.commands.client.AddClientCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.commands.pet.AddPetCommand;
import seedu.address.logic.parser.BuBuParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.GroomingBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyGroomingBook;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.client.Client;
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

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final BuBuParser buBuParser;
    private final boolean isGroomingBookCorrupted;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, new BuBuParser(), false);
    }

    /**
     * Constructs logic with the grooming book corruption state recorded at startup.
     * Add-pet commands cannot verify existing records when this state is true.
     */
    public LogicManager(Model model, Storage storage, boolean isGroomingBookCorrupted) {
        this(model, storage, new BuBuParser(), isGroomingBookCorrupted);
    }

    /** Constructs logic with a parser configured for the available feature dependencies. */
    public LogicManager(Model model, Storage storage, BuBuParser buBuParser) {
        this(model, storage, buBuParser, false);
    }

    /**
     * Constructs logic with parser dependencies and the grooming book corruption state.
     * Add-pet commands report a data error if existing records could not be loaded.
     */
    public LogicManager(Model model, Storage storage, BuBuParser buBuParser,
            boolean isGroomingBookCorrupted) {
        this.model = model;
        this.storage = storage;
        this.buBuParser = buBuParser;
        this.isGroomingBookCorrupted = isGroomingBookCorrupted;
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = buBuParser.parseCommand(commandText);
        if (command instanceof AddPetCommand && isGroomingBookCorrupted) {
            throw new CommandException(MESSAGE_CORRUPTED_RECORDS);
        }
        ReadOnlyGroomingBook previousGroomingBook = command instanceof AddPetCommand
                ? new GroomingBook(model.getGroomingBook()) : null;
        List<Appointment> previousAppointments = command instanceof AddAppointmentCommand
                ? List.copyOf(model.getGroomingBook().getAppointmentList()) : null;
        CommandResult commandResult = command.execute(model);
        saveCommandResult(command, previousGroomingBook, previousAppointments);
        return commandResult;
    }

    /**
     * Saves the completed command and restores its previous data if saving fails.
     */
    private void saveCommandResult(Command command, ReadOnlyGroomingBook previousGroomingBook,
            List<Appointment> previousAppointments) throws CommandException {
        try {
            if (previousAppointments != null) {
                storage.saveAppointmentData(model.getGroomingBook());
            } else {
                storage.saveClientPetData(model.getGroomingBook());
            }
        } catch (IOException ioe) {
            if (previousGroomingBook != null) {
                model.setGroomingBook(previousGroomingBook);
                throw new CommandException(MESSAGE_UNABLE_TO_SAVE_PET, ioe);
            }
            if (previousAppointments != null) {
                model.setAppointments(previousAppointments);
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
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}

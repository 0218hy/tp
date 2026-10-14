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
import seedu.address.logic.commands.appointment.ScheduleAppointmentCommand;
import seedu.address.logic.commands.client.AddClientCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.BuBuParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.client.Client;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String MESSAGE_CLIENT_SAVE_FAILURE = "Unable to save client.";
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final BuBuParser buBuParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, new BuBuParser());
    }

    /** Constructs logic with a parser configured for the available feature dependencies. */
    public LogicManager(Model model, Storage storage, BuBuParser buBuParser) {
        this.model = model;
        this.storage = storage;
        this.buBuParser = buBuParser;
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        CommandResult commandResult;
        Command command = buBuParser.parseCommand(commandText);
        List<Appointment> previousAppointments = command instanceof ScheduleAppointmentCommand
                ? List.copyOf(model.getGroomingBook().getAppointmentList()) : null;
        commandResult = command.execute(model);

        try {
            if (previousAppointments != null) {
                storage.saveAppointmentData(model.getGroomingBook());
            } else {
                storage.saveClientPetData(model.getGroomingBook());
            }
        } catch (IOException ioe) {
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

        return commandResult;
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

package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.appointment.ScheduleCommand;
import seedu.address.logic.commands.client.AddClientCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.commands.pet.AddPetCommand;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.appointment.AppointmentBook;
import seedu.address.model.appointment.ReadOnlyAppointmentBook;
import seedu.address.model.person.Person;
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
    private final AddressBookParser addressBookParser;
    private final boolean isAddressBookCorrupted;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, new AddressBookParser(), false);
    }

    /**
     * Constructs logic with the address book corruption state recorded at startup.
     * Add-pet commands cannot verify existing records when this state is true.
     */
    public LogicManager(Model model, Storage storage, boolean isAddressBookCorrupted) {
        this(model, storage, new AddressBookParser(), isAddressBookCorrupted);
    }

    /** Constructs logic with a parser configured for the available feature dependencies. */
    public LogicManager(Model model, Storage storage, AddressBookParser addressBookParser) {
        this(model, storage, addressBookParser, false);
    }

    /**
     * Constructs logic with parser dependencies and the address book corruption state.
     * Add-pet commands report a data error if existing records could not be loaded.
     */
    public LogicManager(Model model, Storage storage, AddressBookParser addressBookParser,
            boolean isAddressBookCorrupted) {
        this.model = model;
        this.storage = storage;
        this.addressBookParser = addressBookParser;
        this.isAddressBookCorrupted = isAddressBookCorrupted;
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = addressBookParser.parseCommand(commandText);
        if (command instanceof AddPetCommand && isAddressBookCorrupted) {
            throw new CommandException(MESSAGE_CORRUPTED_RECORDS);
        }
        ReadOnlyAddressBook previousAddressBook = command instanceof AddPetCommand
                ? new AddressBook(model.getAddressBook()) : null;
        ReadOnlyAppointmentBook previousAppointments = command instanceof ScheduleCommand
                ? new AppointmentBook(model.getAppointmentBook()) : null;
        CommandResult commandResult = command.execute(model);
        saveCommandResult(command, previousAddressBook, previousAppointments);
        return commandResult;
    }

    /**
     * Saves the completed command and restores its previous data if saving fails.
     */
    private void saveCommandResult(Command command, ReadOnlyAddressBook previousAddressBook,
            ReadOnlyAppointmentBook previousAppointments) throws CommandException {
        try {
            if (previousAppointments != null) {
                storage.saveAppointmentBook(model.getAppointmentBook());
            } else {
                storage.saveAddressBook(model.getAddressBook());
            }
        } catch (IOException ioe) {
            if (previousAddressBook != null) {
                model.setAddressBook(previousAddressBook);
                throw new CommandException(MESSAGE_UNABLE_TO_SAVE_PET, ioe);
            }
            if (previousAppointments != null) {
                model.setAppointmentBook(previousAppointments);
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
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
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

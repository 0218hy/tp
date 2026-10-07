package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.AppointmentBook;
import seedu.address.model.client.ClientBook;
import seedu.address.model.pet.PetBook;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.ClientPetData;
import seedu.address.storage.JsonClientPetStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V0.5.1";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path CLIENT_PET_FILE_PATH = Paths.get("data", "addressbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing BuBu ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonClientPetStorage clientPetStorage = new JsonClientPetStorage(CLIENT_PET_FILE_PATH);
        storage = new StorageManager(clientPetStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getClientPetFilePath());
    }

    /**
     * Returns a {@code ModelManager} with the client and pet data from {@code storage} and {@code userPrefs}. <br>
     * Sample clients are used if the client/pet data file is not found,
     * or empty client and pet books are used if errors occur when loading it.
     * Appointments load independently; a missing or invalid appointment file starts an empty schedule.
     */
    protected Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getClientPetFilePath());

        Optional<ClientPetData> clientPetDataOptional;
        ClientPetData initialData;
        try {
            clientPetDataOptional = storage.readClientPetData();
            if (clientPetDataOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getClientPetFilePath()
                        + " populated with sample clients.");
            }
            initialData = clientPetDataOptional.orElseGet(() ->
                    new ClientPetData(SampleDataUtil.getSampleClientBook(), new PetBook()));
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getClientPetFilePath() + " could not be loaded."
                    + " Will be starting with empty client and pet books.");
            initialData = new ClientPetData(new ClientBook(), new PetBook());
        }

        ModelManager modelManager = new ModelManager(initialData.getClientBook(), initialData.getPetBook(), userPrefs);
        try {
            modelManager.setAppointmentBook(storage.readAppointmentBook().orElseGet(AppointmentBook::new));
        } catch (DataLoadingException e) {
            logger.warning("Appointment file at " + storage.getAppointmentBookFilePath()
                    + " could not be loaded. Will be starting with an empty schedule. "
                    + "The existing file is left unchanged.");
        }
        return modelManager;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting BuBu " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping BuBu ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}

package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentBook;
import seedu.address.model.appointment.ReadOnlyAppointmentBook;
import seedu.address.model.client.Client;
import seedu.address.model.client.ClientBook;
import seedu.address.model.client.ReadOnlyClientBook;
import seedu.address.model.client.exceptions.ClientNotFoundException;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetBook;
import seedu.address.model.pet.ReadOnlyPetBook;

/**
 * Owns the client, pet, and appointment books and coordinates relationships between them.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final ClientBook clientBook;
    private final PetBook petBook;
    private final AppointmentBook appointmentBook = new AppointmentBook();
    private final UserPrefs userPrefs;
    private final FilteredList<Client> filteredClients;
    private final FilteredList<Pet> filteredPets;

    /** Initializes the model with clients, pets, and user preferences. */
    public ModelManager(ReadOnlyClientBook clientBook, ReadOnlyPetBook petBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(clientBook, petBook, userPrefs);
        logger.fine("Initializing client and pet books with user prefs " + userPrefs);
        this.clientBook = new ClientBook(clientBook);
        validateOwners(petBook, this.clientBook);
        this.petBook = new PetBook(petBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredClients = new FilteredList<>(this.clientBook.getClientList());
        filteredPets = new FilteredList<>(this.petBook.getPetList());
    }

    public ModelManager(ReadOnlyClientBook clientBook, ReadOnlyUserPrefs userPrefs) {
        this(clientBook, new PetBook(), userPrefs);
    }

    public ModelManager() {
        this(new ClientBook(), new PetBook(), new UserPrefs());
    }

    private static void validateOwners(ReadOnlyPetBook pets, ReadOnlyClientBook clients) {
        for (Pet pet : pets.getPetList()) {
            if (clients.getClientList().stream().noneMatch(client -> client.isSameClient(pet.getOwner()))) {
                throw new ClientNotFoundException();
            }
        }
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== ClientBook ================================================================================

    @Override
    public void setClientBook(ReadOnlyClientBook clientBook) {
        ClientBook replacement = new ClientBook(clientBook);
        validateOwners(petBook, replacement);
        this.clientBook.resetData(replacement);
        for (Pet pet : petBook.getPetList()) {
            Client owner = replacement.getClientList().stream()
                    .filter(client -> client.isSameClient(pet.getOwner())).findFirst().orElseThrow();
            petBook.setPet(pet, pet.withOwner(owner));
        }
    }

    @Override
    public ReadOnlyClientBook getClientBook() {
        return clientBook;
    }

    @Override
    public boolean hasClient(Client client) {
        requireNonNull(client);
        return clientBook.hasClient(client);
    }

    @Override
    public void deleteClient(Client target) {
        clientBook.removeClient(target);
    }

    @Override
    public void addClient(Client client) {
        clientBook.addClient(client);
        updateFilteredClientList(PREDICATE_SHOW_ALL_CLIENTS);
    }

    @Override
    public void setClient(Client target, Client editedClient) {
        requireAllNonNull(target, editedClient);

        clientBook.setClient(target, editedClient);
        petBook.updateOwner(target, editedClient);
    }

    //=========== Pet ================================================================================

    @Override
    public boolean hasPet(Pet pet) {
        requireNonNull(pet);
        return petBook.hasPet(pet);
    }

    @Override
    public void deletePet(Pet pet) {
        petBook.removePet(pet);
    }

    @Override
    public void addPet(Pet pet) {
        requireNonNull(pet);
        requireOwner(pet);
        petBook.addPet(pet);
        updateFilteredPetList(PREDICATE_SHOW_ALL_PETS);
    }

    @Override
    public void setPet(Pet target, Pet editedPet) {
        requireAllNonNull(target, editedPet);
        requireOwner(editedPet);
        petBook.setPet(target, editedPet);
    }

    @Override
    public ReadOnlyPetBook getPetBook() {
        return petBook;
    }

    @Override
    public void setPetBook(ReadOnlyPetBook petBook) {
        requireNonNull(petBook);
        validateOwners(petBook, clientBook);
        this.petBook.resetData(petBook);
    }

    private void requireOwner(Pet pet) {
        if (!clientBook.hasClient(pet.getOwner())) {
            throw new ClientNotFoundException();
        }
    }

    //=========== Appointments ==============================================================================

    @Override
    public ReadOnlyAppointmentBook getAppointmentBook() {
        return appointmentBook;
    }

    @Override
    public void setAppointmentBook(ReadOnlyAppointmentBook appointmentBook) {
        this.appointmentBook.resetData(appointmentBook);
    }

    @Override
    public void addAppointment(Appointment appointment) {
        appointmentBook.addAppointment(appointment);
    }

    //=========== Filtered Client List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Client} backed by the internal list of
     * {@code clientBook}
     */
    @Override
    public ObservableList<Client> getFilteredClientList() {
        return filteredClients;
    }

    @Override
    public void updateFilteredClientList(Predicate<Client> predicate) {
        requireNonNull(predicate);
        filteredClients.setPredicate(predicate);
    }

    //=========== Filtered Pet List Accessors ========================================================

    @Override
    public ObservableList<Pet> getFilteredPetList() {
        return filteredPets;
    }

    @Override
    public void updateFilteredPetList(Predicate<Pet> predicate) {
        requireNonNull(predicate);
        filteredPets.setPredicate(predicate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return clientBook.equals(otherModelManager.clientBook)
                && petBook.equals(otherModelManager.petBook)
                && appointmentBook.equals(otherModelManager.appointmentBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredClients.equals(otherModelManager.filteredClients)
                && filteredPets.equals(otherModelManager.filteredPets);
    }

}

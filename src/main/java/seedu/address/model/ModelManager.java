package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;

/**
 * Represents the in-memory model of the grooming book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final GroomingBook groomingBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Client> filteredClients;
    private final SortedList<Client> sortedClients;
    private final FilteredList<Pet> filteredPets;
    private final SortedList<Pet> sortedPets;
    private final FilteredList<Appointment> filteredAppointments;
    private final SortedList<Appointment> sortedAppointments;

    /**
     * Initializes a ModelManager with the given groomingBook and userPrefs.
     */
    public ModelManager(ReadOnlyGroomingBook groomingBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(groomingBook, userPrefs);

        logger.fine("Initializing with grooming book: " + groomingBook + " and user prefs " + userPrefs);

        this.groomingBook = new GroomingBook(groomingBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredClients = new FilteredList<>(this.groomingBook.getClientList());
        sortedClients = new SortedList<>(filteredClients);
        filteredPets = new FilteredList<>(this.groomingBook.getPetList());
        sortedPets = new SortedList<>(filteredPets, Comparator
                .comparing((Pet pet) -> pet.getOwner().getName().fullName.toLowerCase(Locale.ROOT))
                .thenComparing(pet -> pet.getOwner().getPhone().value)
                .thenComparing(pet -> pet.getName().getNormalizedName()));
        filteredAppointments = new FilteredList<>(this.groomingBook.getAppointmentList());
        sortedAppointments = new SortedList<>(filteredAppointments,
                Comparator.comparing((Appointment appointment) -> appointment.getDate().value)
                        .thenComparing(appointment -> appointment.getStartTime().value));
    }

    public ModelManager() {
        this(new GroomingBook(), new UserPrefs());
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

    //=========== GroomingBook ================================================================================

    @Override
    public void setGroomingBook(ReadOnlyGroomingBook groomingBook) {
        this.groomingBook.resetData(groomingBook);
    }

    @Override
    public ReadOnlyGroomingBook getGroomingBook() {
        return groomingBook;
    }

    //=========== Client ======================================================================================
    @Override
    public boolean hasClient(Client client) {
        requireNonNull(client);
        return groomingBook.hasClient(client);
    }

    @Override
    public void deleteClient(Client target) {
        groomingBook.removeClient(target);
    }

    @Override
    public void addClient(Client client) {
        groomingBook.addClient(client);
        updateFilteredClientList(PREDICATE_SHOW_ALL_CLIENTS);
    }

    @Override
    public void setClient(Client target, Client editedClient) {
        requireAllNonNull(target, editedClient);

        groomingBook.setClient(target, editedClient);
    }

    //=========== Pet ======================================================================================

    @Override
    public boolean hasPet(Pet pet) {
        requireNonNull(pet);
        return groomingBook.hasPet(pet);
    }

    @Override
    public void deletePet(Pet pet) {
        groomingBook.removePet(pet);
    }

    @Override
    public void addPet(Pet pet) {
        groomingBook.addPet(pet);
        updateFilteredPetList(PREDICATE_SHOW_ALL_PETS);
    }

    @Override
    public void setPet(Pet target, Pet editedPet) {
        requireAllNonNull(target, editedPet);
        groomingBook.setPet(target, editedPet);
    }

    //=========== Appointments ==============================================================================

    @Override
    public void setAppointments(List<Appointment> appointments) {
        groomingBook.setAppointments(appointments);
    }

    @Override
    public void clearGroomingBook() {
        groomingBook.clear();
    }

    @Override
    public void addAppointment(Appointment appointment) {
        groomingBook.addAppointment(appointment);
    }

    @Override
    public void deleteAppointment(Appointment appointment) {
        groomingBook.removeAppointment(appointment);
    }

    //=========== Filtered Client List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Client} backed by the internal list of
     * {@code groomingBook}
     */
    @Override
    public ObservableList<Client> getFilteredClientList() {
        return sortedClients;
    }

    @Override
    public void updateFilteredClientList(Predicate<Client> predicate) {
        requireNonNull(predicate);
        filteredClients.setPredicate(predicate);
    }

    /**
     * Sorts the live displayed client list by name, ignoring case, without reordering stored clients.
     * Subsequent changes to the filtered list retain this sort order.
     */
    @Override
    public void sortFilteredClientList() {
        sortedClients.setComparator(Comparator.comparing(person -> person.getName().fullName,
                String.CASE_INSENSITIVE_ORDER));
    }

    //=========== Filtered Pet List Accessors ========================================================

    @Override
    public ObservableList<Pet> getFilteredPetList() {
        return sortedPets;
    }

    @Override
    public void updateFilteredPetList(Predicate<Pet> predicate) {
        requireNonNull(predicate);
        filteredPets.setPredicate(predicate);
    }

    //=========== Filtered Appointment List Accessors ========================================================

    @Override
    public ObservableList<Appointment> getFilteredAppointmentList() {
        return sortedAppointments;
    }

    @Override
    public void updateFilteredAppointmentList(Predicate<Appointment> predicate) {
        requireNonNull(predicate);
        filteredAppointments.setPredicate(predicate);
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

        return groomingBook.equals(otherModelManager.groomingBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredClients.equals(otherModelManager.filteredClients)
                && filteredPets.equals(otherModelManager.filteredPets);
    }

}

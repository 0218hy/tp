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
 * Represents the in-memory model of the BubuBook data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final BubuBook bubuBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Client> filteredClients;
    private final SortedList<Client> sortedClients;
    private final FilteredList<Pet> filteredPets;
    private final SortedList<Pet> sortedPets;
    private final FilteredList<Appointment> filteredAppointments;
    private final SortedList<Appointment> sortedAppointments;

    /**
     * Initializes a ModelManager with the given bubuBook and userPrefs.
     */
    public ModelManager(ReadOnlyBubuBook bubuBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(bubuBook, userPrefs);

        logger.fine("Initializing with BubuBook: " + bubuBook + " and user prefs " + userPrefs);

        this.bubuBook = new BubuBook(bubuBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredClients = new FilteredList<>(this.bubuBook.getClientList());
        sortedClients = new SortedList<>(filteredClients);
        filteredPets = new FilteredList<>(this.bubuBook.getPetList());
        sortedPets = new SortedList<>(filteredPets, createPetDisplayComparator());
        filteredAppointments = new FilteredList<>(this.bubuBook.getAppointmentList());
        sortedAppointments = new SortedList<>(filteredAppointments,
                Comparator.comparing((Appointment appointment) -> appointment.getDate().value)
                        .thenComparing(appointment -> appointment.getStartTime().value));
    }

    public ModelManager() {
        this(new BubuBook(), new UserPrefs());
    }

    // @@author xinyu-zxy
    /** Returns a comparator that groups pets by owner, then orders them by normalized name. */
    private static Comparator<Pet> createPetDisplayComparator() {
        Comparator<Pet> byOwnerName = Comparator.comparing(
                pet -> pet.getOwner().getName().fullName.toLowerCase(Locale.ROOT));
        Comparator<Pet> byOwner = byOwnerName.thenComparing(pet -> pet.getOwner().getPhone().value);
        return byOwner.thenComparing(pet -> pet.getName().getNormalizedName());
    }
    // @@author

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

    //=========== BubuBook ================================================================================

    @Override
    public void setBubuBook(ReadOnlyBubuBook bubuBook) {
        this.bubuBook.resetData(bubuBook);
    }

    @Override
    public ReadOnlyBubuBook getBubuBook() {
        return bubuBook;
    }

    //=========== Client ======================================================================================
    @Override
    public boolean hasClient(Client client) {
        requireNonNull(client);
        return bubuBook.hasClient(client);
    }

    @Override
    public void deleteClient(Client target) {
        bubuBook.removeClient(target);
    }

    @Override
    public void addClient(Client client) {
        bubuBook.addClient(client);
        updateFilteredClientList(PREDICATE_SHOW_ALL_CLIENTS);
    }

    @Override
    public void setClient(Client target, Client editedClient) {
        requireAllNonNull(target, editedClient);

        bubuBook.setClient(target, editedClient);
    }

    //=========== Pet ======================================================================================

    @Override
    public boolean hasPet(Pet pet) {
        requireNonNull(pet);
        return bubuBook.hasPet(pet);
    }

    @Override
    public void deletePet(Pet pet) {
        bubuBook.removePet(pet);
    }

    @Override
    public void addPet(Pet pet) {
        bubuBook.addPet(pet);
        updateFilteredPetList(PREDICATE_SHOW_ALL_PETS);
    }

    @Override
    public void setPet(Pet target, Pet editedPet) {
        requireAllNonNull(target, editedPet);
        bubuBook.setPet(target, editedPet);
    }

    //=========== Appointments ==============================================================================

    //@@author 0218hy
    @Override
    public void setAppointments(List<Appointment> appointments) {
        bubuBook.setAppointments(appointments);
    }

    @Override
    public void clearBubuBook() {
        bubuBook.clear();
    }

    @Override
    public void addAppointment(Appointment appointment) {
        bubuBook.addAppointment(appointment);
    }
    //@@author

    @Override
    public void deleteAppointment(Appointment appointment) {
        bubuBook.removeAppointment(appointment);
    }

    //=========== Filtered Client List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Client} backed by the internal list of
     * {@code bubuBook}
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

    //@@author anderschow
    /**
     * Sorts the live displayed client list by name, ignoring case, without reordering stored clients.
     * Subsequent changes to the filtered list retain this sort order.
     */
    @Override
    public void sortFilteredClientList() {
        sortedClients.setComparator(Comparator.comparing(person -> person.getName().fullName,
                String.CASE_INSENSITIVE_ORDER));
    }
    //@@author

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

        return bubuBook.equals(otherModelManager.bubuBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredClients.equals(otherModelManager.filteredClients)
                && filteredPets.equals(otherModelManager.filteredPets);
    }

}

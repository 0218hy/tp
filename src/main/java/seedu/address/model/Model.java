package seedu.address.model;

import java.util.List;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Client> PREDICATE_SHOW_ALL_CLIENTS = unused -> true;
    Predicate<Pet> PREDICATE_SHOW_ALL_PETS = unused -> true;
    Predicate<Appointment> PREDICATE_SHOW_ALL_APPOINTMENTS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces BubuBook data with the data in {@code bubuBook}.
     */
    void setBubuBook(ReadOnlyBubuBook bubuBook);

    /** Returns the BubuBook */
    ReadOnlyBubuBook getBubuBook();

    /** Replaces the appointment schedule after validating all slots. */
    void setAppointments(List<Appointment> appointments);

    /** Clears clients and pets together, retaining historical appointments. */
    void clearClientsAndPets();

    /**
     * Adds an appointment that does not overlap an existing appointment.
     * Participant and future-start checks are performed by the scheduling command.
     *
     * @throws seedu.address.model.appointment.exceptions.OverlappingAppointmentException if a slot overlaps.
     */
    void addAppointment(Appointment appointment);

    /**
     * Deletes the given appointment.
     * The appointment must exist in the BubuBook.
     */
    void deleteAppointment(Appointment appointment);

    /**
     * Returns true if a client with the same identity as {@code client} exists in the BubuBook.
     */
    boolean hasClient(Client client);

    /**
     * Deletes the given client.
     * The client must exist in the BubuBook.
     */
    void deleteClient(Client target);

    /**
     * Adds the given client.
     * {@code client} must not already exist in the BubuBook.
     */
    void addClient(Client client);

    /**
     * Replaces the given client {@code target} with {@code editedClient}.
     * {@code target} must exist in the BubuBook.
     * The client identity of {@code editedClient} must not be the same as another existing client in the BubuBook.
     */
    void setClient(Client target, Client editedClient);

    /**
     * Returns true if a pet with the same identity as {@code pet} exists in the BubuBook.
     */
    boolean hasPet(Pet pet);

    /**
     * Deletes the given pet.
     * The pet must exist in the BubuBook.
     */
    void deletePet(Pet pet);

    /**
     * Adds the given pet.
     * The pet must not already exist in the BubuBook.
     */
    void addPet(Pet pet);

    /**
     * Replaces the given pet {@code target} with {@code editedPet}.
     * The target pet must exist in the BubuBook.
     */
    void setPet(Pet target, Pet editedPet);

    /** Returns an unmodifiable view of the filtered client list */
    ObservableList<Client> getFilteredClientList();

    /**
     * Updates the filter of the filtered client list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredClientList(Predicate<Client> predicate);

    /** Sorts the displayed clients by name, ignoring case, without modifying stored records. */
    void sortFilteredClientList();

    /** Returns an unmodifiable view of the filtered pet list. */
    ObservableList<Pet> getFilteredPetList();

    /**
     * Updates the filter of the filtered pet list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPetList(Predicate<Pet> predicate);

    /** Returns an unmodifiable view of the filtered appointment list. */
    ObservableList<Appointment> getFilteredAppointmentList();

    /**
     * Updates the filter of the filtered appointment list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredAppointmentList(Predicate<Appointment> predicate);
}

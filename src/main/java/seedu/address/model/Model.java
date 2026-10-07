package seedu.address.model;

import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.ReadOnlyAppointmentBook;
import seedu.address.model.client.Client;
import seedu.address.model.client.ReadOnlyClientBook;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.ReadOnlyPetBook;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Client> PREDICATE_SHOW_ALL_CLIENTS = unused -> true;
    /** {@code Predicate} that always evaluates to true */
    Predicate<Pet> PREDICATE_SHOW_ALL_PETS = unused -> true;

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
     * Replaces client book data with the data in {@code clientBook}.
     */
    void setClientBook(ReadOnlyClientBook clientBook);

    /** Returns the ClientBook */
    ReadOnlyClientBook getClientBook();

    /** Returns the read-only pet book. */
    ReadOnlyPetBook getPetBook();

    /** Replaces the pet book after validating that every owner exists. */
    void setPetBook(ReadOnlyPetBook petBook);

    /** Returns the read-only appointment schedule. */
    ReadOnlyAppointmentBook getAppointmentBook();

    /** Replaces the appointment schedule with a validated copy of the supplied data. */
    void setAppointmentBook(ReadOnlyAppointmentBook appointmentBook);

    /**
     * Adds an appointment that does not overlap an existing appointment.
     * Participant and future-start checks are performed by the scheduling command.
     *
     * @throws seedu.address.model.appointment.exceptions.OverlappingAppointmentException if a slot overlaps.
     */
    void addAppointment(Appointment appointment);

    /**
     * Returns true if a client with the same identity as {@code client} exists in the client book.
     */
    boolean hasClient(Client client);

    /**
     * Deletes the given client.
     * The client must exist in the client book.
     */
    void deleteClient(Client target);

    /**
     * Adds the given client.
     * {@code client} must not already exist in the client book.
     */
    void addClient(Client client);

    /**
     * Replaces the given client {@code target} with {@code editedClient}.
     * {@code target} must exist in the client book.
     * The client identity of {@code editedClient} must not be the same as another existing client in the client book.
     */
    void setClient(Client target, Client editedClient);

    /**
     * Returns true if a pet with the same identity as {@code pet} exists in the pet book.
     */
    boolean hasPet(Pet pet);

    /**
     * Deletes the given pet.
     * The pet must exist in the pet book.
     */
    void deletePet(Pet pet);

    /**
     * Adds the given pet.
     * The pet must not already exist in the pet book.
     */
    void addPet(Pet pet);

    /**
     * Replaces the given pet {@code target} with {@code editedPet}.
     * The target pet must exist in the pet book.
     */
    void setPet(Pet target, Pet editedPet);

    /** Returns an unmodifiable view of the filtered client list */
    ObservableList<Client> getFilteredClientList();

    /**
     * Updates the filter of the filtered client list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredClientList(Predicate<Client> predicate);

    /** Returns an unmodifiable view of the filtered pet list. */
    ObservableList<Pet> getFilteredPetList();

    /**
     * Updates the filter of the filtered pet list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPetList(Predicate<Pet> predicate);
}

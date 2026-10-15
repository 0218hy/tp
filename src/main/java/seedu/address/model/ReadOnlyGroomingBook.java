package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;

/** Read-only access to all records used by the grooming business. */
public interface ReadOnlyGroomingBook {
    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Client> getClientList();

    /**
     * Returns an unmodifiable view of the pets list.
     * This list will not contain duplicate pets.
     */
    ObservableList<Pet> getPetList();

    /**
     * Returns an unmodifiable view of the appointments list.
     * This list will not contain overlapping appointment slots.
     */
    ObservableList<Appointment> getAppointmentList();
}

package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;

/** Read-only access to all records used by the grooming business. */
public interface ReadOnlyGroomingBook {
    ObservableList<Client> getClientList();

    ObservableList<Pet> getPetList();

    ObservableList<Appointment> getAppointmentList();
}

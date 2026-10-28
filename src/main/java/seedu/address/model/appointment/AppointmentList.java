package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.appointment.exceptions.AppointmentNotFoundException;
import seedu.address.model.appointment.exceptions.OverlappingAppointmentException;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;

/**
 * Stores appointments independently of owner records, rejecting nulls and overlaps.
 * Back-to-back appointments are allowed. Past appointments can be stored for historical records;
 * the scheduling command is responsible for checking owners, pets and future start times.
 */
public class AppointmentList {
    private final ObservableList<Appointment> internalList = FXCollections.observableArrayList();
    private final ObservableList<Appointment> unmodifiableAppointments =
            FXCollections.unmodifiableObservableList(internalList);

    //@@author 0218hy
    /**
     * Replaces the appointments, preserving their order.
     * Validates the entire replacement before changing the current list.
     *
     * @throws OverlappingAppointmentException if any two appointments overlap.
     * @throws NullPointerException if the list or any entry is null.
     */
    public void setAppointments(List<Appointment> replacement) {
        requireNonNull(replacement);
        AppointmentList validated = new AppointmentList();
        replacement.forEach(validated::addAppointment);
        internalList.setAll(validated.getAppointmentList());
    }

    /**
     * Returns whether an existing appointment overlaps {@code appointment}, regardless of owner or pet.
     */
    public boolean hasOverlappingAppointment(Appointment appointment) {
        requireNonNull(appointment);
        return internalList.stream().anyMatch(appointment::overlaps);
    }

    /**
     * Adds an appointment at the end of the list.
     *
     * @throws OverlappingAppointmentException if the appointment overlaps an existing appointment.
     * @throws NullPointerException if the appointment is null.
     */
    public void addAppointment(Appointment appointment) {
        requireNonNull(appointment);
        for (Appointment existing : internalList) {
            if (appointment.overlaps(existing)) {
                throw new OverlappingAppointmentException(existing);
            }
        }
        internalList.add(appointment);
    }
    //@@author

    /**
     * Removes the equivalent appointment from the list.
     * The appointment must exist in the list.
     */
    public void removeAppointment(Appointment toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new AppointmentNotFoundException();
        }
    }

    //@@author chunghean
    /** Removes every appointment belonging to the given owner phone number. */
    public void removeAppointmentsForOwner(Phone ownerPhone) {
        requireNonNull(ownerPhone);
        internalList.removeIf(appointment -> appointment.getOwnerPhone().equals(ownerPhone));
    }

    /** Updates appointments when their owner's client record is replaced. */
    public void updateOwner(Client currentOwner, Client editedOwner) {
        requireNonNull(currentOwner);
        requireNonNull(editedOwner);
        if (currentOwner.getPhone().equals(editedOwner.getPhone())) {
            return;
        }
        for (int index = 0; index < internalList.size(); index++) {
            Appointment appointment = internalList.get(index);
            if (appointment.getOwnerPhone().equals(currentOwner.getPhone())) {
                internalList.set(index, appointment.withOwnerPhone(editedOwner.getPhone()));
            }
        }
    }
    //@@author

    //@@author khangsian0412
    /**
     * Updates every appointment belonging to {@code currentPet} with its replacement name.
     * Other appointment snapshot details remain unchanged.
     */
    public void updatePetName(Pet currentPet, PetName newPetName) {
        requireNonNull(currentPet);
        requireNonNull(newPetName);
        for (int index = 0; index < internalList.size(); index++) {
            Appointment appointment = internalList.get(index);
            if (appointment.isForPet(currentPet)) {
                internalList.set(index, appointment.withPetName(newPetName));
            }
        }
    }
    //@@author

    //@@author chunghean
    /** Removes every appointment belonging to the given pet. */
    public void removeAppointmentsForPet(Pet pet) {
        requireNonNull(pet);
        internalList.removeIf(appointment -> appointment.isForPet(pet));
    }
    //@@author

    //@@author 0218hy
    public ObservableList<Appointment> getAppointmentList() {
        return unmodifiableAppointments;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof AppointmentList otherList
                && internalList.equals(otherList.internalList));
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }
    //@@author

    @Override
    public String toString() {
        return internalList.toString();
    }
}

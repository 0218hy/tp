package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.appointment.exceptions.AppointmentNotFoundException;
import seedu.address.model.appointment.exceptions.OverlappingAppointmentException;

/**
 * Stores appointments independently of owner records, rejecting nulls and overlaps.
 * Back-to-back appointments are allowed. Past appointments can be stored for historical records;
 * the scheduling command is responsible for checking owners, pets and future start times.
 */
public class AppointmentList {
    private final ObservableList<Appointment> internalList = FXCollections.observableArrayList();
    private final ObservableList<Appointment> unmodifiableAppointments =
            FXCollections.unmodifiableObservableList(internalList);

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
        if (hasOverlappingAppointment(appointment)) {
            throw new OverlappingAppointmentException();
        }
        internalList.add(appointment);
    }

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

    @Override
    public String toString() {
        return internalList.toString();
    }
}

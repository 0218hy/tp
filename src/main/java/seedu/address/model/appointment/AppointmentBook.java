package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.appointment.exceptions.OverlappingAppointmentException;

/**
 * Stores appointments independently of owner records, rejecting nulls and overlaps.
 * Back-to-back appointments are allowed. Past appointments can be stored for historical records;
 * the scheduling command is responsible for checking owners, pets and future start times.
 */
public class AppointmentBook implements ReadOnlyAppointmentBook {
    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private final ObservableList<Appointment> unmodifiableAppointments =
            FXCollections.unmodifiableObservableList(appointments);

    public AppointmentBook() {}

    /**
     * Creates an independent copy of the given appointment book.
     */
    public AppointmentBook(ReadOnlyAppointmentBook source) {
        resetData(source);
    }

    /**
     * Replaces this book's data with a validated copy of {@code source}.
     * Invalid data leaves the current appointments unchanged.
     */
    public void resetData(ReadOnlyAppointmentBook source) {
        requireNonNull(source);
        setAppointments(source.getAppointmentList());
    }

    /**
     * Replaces the appointments, preserving their order.
     * Validates the entire replacement before changing the current list.
     *
     * @throws OverlappingAppointmentException if any two appointments overlap.
     * @throws NullPointerException if the list or any entry is null.
     */
    public void setAppointments(List<Appointment> replacement) {
        requireNonNull(replacement);
        AppointmentBook validated = new AppointmentBook();
        replacement.forEach(validated::addAppointment);
        appointments.setAll(validated.getAppointmentList());
    }

    /**
     * Returns whether an existing appointment overlaps {@code appointment}, regardless of owner or pet.
     */
    public boolean hasOverlappingAppointment(Appointment appointment) {
        requireNonNull(appointment);
        return appointments.stream().anyMatch(appointment::overlaps);
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
        appointments.add(appointment);
    }

    @Override
    public ObservableList<Appointment> getAppointmentList() {
        return unmodifiableAppointments;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof AppointmentBook otherBook
                && appointments.equals(otherBook.appointments));
    }

    @Override
    public int hashCode() {
        return appointments.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("appointments", appointments).toString();
    }
}

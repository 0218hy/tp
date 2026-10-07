package seedu.address.model.appointment;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalTime;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Phone;

/**
 * Represents an immutable grooming appointment in BuBu.
 * The scheduling command must verify the owner and pet, a future start in Singapore,
 * and the absence of overlaps before saving. Past appointments can still be loaded.
 */
public class Appointment {
    public static final String MESSAGE_PET_NAME_CONSTRAINTS =
            "Pet name must be 1-40 characters and use valid name characters.";

    // Identity fields: date and start time identify a slot in the non-overlapping schedule.
    private final AppointmentDate date;
    private final StartTime startTime;

    // Data fields
    private final Phone ownerPhone;
    // TODO: Replace String with PetName when the pet model is available.
    private final String petName;
    private final EndTime endTime;
    private final Service service;

    /**
     * Constructs an appointment with non-null fields and a duration of at least 30 minutes.
     * Pet-name whitespace is normalised while its letter case is preserved for display.
     */
    public Appointment(Phone ownerPhone, String petName, AppointmentDate date,
                       StartTime startTime, EndTime endTime, Service service) {
        requireAllNonNull(ownerPhone, petName, date, startTime, endTime, service);
        // TODO: Do not require triming of name after the pet model is available
        String normalisedPetName = petName.trim().replaceAll("\\s+", " ");
        checkArgument(normalisedPetName.matches("[A-Za-z0-9 '.-]{1,40}")
                && !normalisedPetName.isBlank(), MESSAGE_PET_NAME_CONSTRAINTS);
        checkArgument(endTime.isAtLeastThirtyMinutesAfter(startTime), EndTime.MESSAGE_CONSTRAINTS);

        this.ownerPhone = ownerPhone;
        this.petName = normalisedPetName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.service = service;
    }

    public Phone getOwnerPhone() {
        return ownerPhone;
    }

    public String getPetName() {
        return petName;
    }

    public AppointmentDate getDate() {
        return date;
    }

    public StartTime getStartTime() {
        return startTime;
    }

    public EndTime getEndTime() {
        return endTime;
    }

    public Service getService() {
        return service;
    }

    /**
     * Returns whether both appointments have the same date and start time.
     * This is weaker than full equality and does not replace overlap detection.
     */
    public boolean isSameAppointment(Appointment otherAppointment) {
        return otherAppointment == this
                || (otherAppointment != null
                && date.equals(otherAppointment.date)
                && startTime.equals(otherAppointment.startTime));
    }

    /**
     * Returns whether appointments overlap on the same date, regardless of owner or pet.
     * Back-to-back appointments do not overlap.
     */
    public boolean overlaps(Appointment otherAppointment) {
        requireAllNonNull(otherAppointment);
        return date.equals(otherAppointment.date)
                && startTime.value.isBefore(otherAppointment.endTime.value)
                && endTime.value.isAfter(otherAppointment.startTime.value);
    }

    /**
     * Returns whether this appointment contains the requested date and time for deletion.
     * Includes the start and excludes the end. The query time need not be on a half-hour boundary.
     */
    public boolean matches(AppointmentDate queryDate, LocalTime queryTime) {
        requireAllNonNull(queryDate, queryTime);
        return date.equals(queryDate)
                && !queryTime.isBefore(startTime.value)
                && queryTime.isBefore(endTime.value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Appointment otherAppointment)) {
            return false;
        }
        return ownerPhone.equals(otherAppointment.ownerPhone)
                && petName.equals(otherAppointment.petName)
                && date.equals(otherAppointment.date)
                && startTime.equals(otherAppointment.startTime)
                && endTime.equals(otherAppointment.endTime)
                && service == otherAppointment.service;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerPhone, petName, date, startTime, endTime, service);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("ownerPhone", ownerPhone)
                .add("petName", petName)
                .add("date", date)
                .add("startTime", startTime)
                .add("endTime", endTime)
                .add("service", service)
                .toString();
    }
}

package seedu.address.model.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalTime;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;

//@@author anderschow
/**
 * Represents an immutable grooming appointment in BuBu.
 * The scheduling command must verify the owner and pet, a future start in Singapore,
 * and the absence of overlaps before saving. Past appointments can still be loaded.
 * Participant objects are immutable snapshots, refreshed by client edits for ongoing and future bookings.
 * Completed bookings retain their details, with owner phone changes keeping related records identifiable.
 */
//@@author
public class Appointment {
    // Identity fields: date and start time identify a slot in the non-overlapping schedule.
    private final AppointmentDate date;
    private final StartTime startTime;

    // Data fields
    private final Client owner;
    private final Pet pet;
    private final EndTime endTime;
    private final Service service;

    //@@author 0218hy
    /**
     * Constructs an appointment with non-null fields and a duration of at least 30 minutes.
     * The supplied pet must belong to the supplied owner.
     */
    public Appointment(Client owner, Pet pet, AppointmentDate date,
                       StartTime startTime, EndTime endTime, Service service) {
        requireAllNonNull(owner, pet, date, startTime, endTime, service);
        checkArgument(owner.equals(pet.getOwner()), "Appointment pet must belong to its owner.");
        checkArgument(endTime.isAtLeastThirtyMinutesAfter(startTime), EndTime.MESSAGE_CONSTRAINTS);

        this.owner = owner;
        this.pet = pet;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.service = service;
    }

    public Client getOwner() {
        return owner;
    }

    public Pet getPet() {
        return pet;
    }

    public Phone getOwnerPhone() {
        return owner.getPhone();
    }

    //@@author chunghean
    /**
     * Returns whether this appointment is linked to {@code candidate}.
     *
     * <p>The owner's phone is the shared identifier for clients, pets, and appointments. Historical
     * profile fields, including email, may differ after an edit and do not determine ownership.</p>
     */
    public boolean isForOwner(Client candidate) {
        requireNonNull(candidate);
        return owner.getPhone().equals(candidate.getPhone());
    }
    //@@author

    public PetName getPetName() {
        return pet.getName();
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
    //@@author

    //@@author anderschow
    /** Returns a copy with current owner details, preserving the booked pet and service details. */
    Appointment withOwner(Client updatedOwner) {
        requireNonNull(updatedOwner);
        return new Appointment(updatedOwner, pet.withOwner(updatedOwner), date, startTime, endTime, service);
    }
    //@@author

    /** Returns a copy with the new owner phone, preserving the remaining participant details. */
    Appointment withOwnerPhone(Phone newOwnerPhone) {
        requireNonNull(newOwnerPhone);
        //@@author 0218hy
        Client updatedOwner = new Client(owner.getName(), newOwnerPhone, owner.getEmail(),
                owner.getAddress(), owner.getTags());
        return new Appointment(updatedOwner, pet.withOwner(updatedOwner), date, startTime, endTime, service);
        //@@author
    }

    //@@author khangsian0412
    /**
     * Returns a copy with the pet's current name, preserving the appointment's remaining snapshot details.
     */
    Appointment withPetName(PetName newPetName) {
        requireNonNull(newPetName);
        return new Appointment(owner, pet.withName(newPetName), date, startTime, endTime, service);
    }

    /** Returns a copy with refreshed pet details while retaining this appointment's owner snapshot. */
    Appointment withPet(Pet updatedPet) {
        requireNonNull(updatedPet);
        return new Appointment(owner, updatedPet.withOwner(owner), date, startTime, endTime, service);
    }
    //@@author

    /**
     * Returns whether this appointment belongs to the given pet.
     * Pet names are compared using the same normalized form used when scheduling appointments.
     */
    public boolean isForPet(Pet pet) {
        requireNonNull(pet);
        return pet.isSamePet(this.pet);
    }

    //@@author 0218hy
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

    /** Returns whether this appointment starts at the requested date and time. */
    public boolean matches(AppointmentDate queryDate, LocalTime queryTime) {
        requireAllNonNull(queryDate, queryTime);
        return date.equals(queryDate)
                && startTime.value.equals(queryTime);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Appointment otherAppointment)) {
            return false;
        }
        return owner.equals(otherAppointment.owner)
                && pet.equals(otherAppointment.pet)
                && date.equals(otherAppointment.date)
                && startTime.equals(otherAppointment.startTime)
                && endTime.equals(otherAppointment.endTime)
                && service == otherAppointment.service;
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, pet, date, startTime, endTime, service);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("owner", owner)
                .add("pet", pet)
                .add("date", date)
                .add("startTime", startTime)
                .add("endTime", endTime)
                .add("service", service)
                .toString();
    }
    //@@author
}

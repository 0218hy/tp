package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentList;
import seedu.address.model.client.Client;
import seedu.address.model.client.UniqueClientList;
import seedu.address.model.client.exceptions.ClientNotFoundException;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.UniquePetList;

/**
 * Owns the clients, pets and appointment schedule for a grooming business.
 * Pet owners are resolved to the stored client records. Removing a client or pet also removes
 * its related appointments; scheduling commands validate new participants.
 */
public class BubuBook implements ReadOnlyBubuBook {
    private final UniqueClientList clients = new UniqueClientList();
    private final UniquePetList pets = new UniquePetList();
    private final AppointmentList appointments = new AppointmentList();

    public BubuBook() {}

    /** Creates an independent copy of all records. */
    public BubuBook(ReadOnlyBubuBook source) {
        resetData(source);
    }

    //@@author 0218hy
    /** Validates the entire replacement before changing any existing list. */
    public void resetData(ReadOnlyBubuBook source) {
        requireNonNull(source);
        BubuBook validated = new BubuBook();
        validated.setClients(source.getClientList());
        validated.setPets(source.getPetList());
        validated.setAppointments(source.getAppointmentList());
        clients.setClients(validated.getClientList());
        pets.setPets(validated.getPetList());
        appointments.setAppointments(validated.getAppointmentList());
    }
    //@@author

    //@@author 0218hy
    /** Replaces clients and refreshes pet owners, rejecting missing owners before mutation. */
    public void setClients(List<Client> replacement) {
        UniqueClientList validated = new UniqueClientList();
        validated.setClients(replacement);
        List<Pet> updatedPets = getPetList().stream()
                .map(pet -> pet.withOwner(findOwner(pet, validated.asUnmodifiableObservableList())))
                .collect(Collectors.toList());
        clients.setClients(validated.asUnmodifiableObservableList());
        pets.setPets(updatedPets);
    }
    //@@author

    public boolean hasClient(Client client) {
        return clients.contains(client);
    }

    public void addClient(Client client) {
        clients.add(client);
    }

    /** Updates a client and every pet referencing that client. */
    public void setClient(Client target, Client editedClient) {
        requireAllNonNull(target, editedClient);
        clients.setClient(target, editedClient);
        pets.updateOwner(target, editedClient);
        appointments.updateOwner(target, editedClient);
    }

    /** Removes a client and all pets and appointments belonging to that client. */
    public void removeClient(Client client) {
        requireNonNull(client);
        clients.remove(client);
        appointments.removeAppointmentsForOwner(client.getPhone());
        pets.removeAllOwnedBy(client);
    }

    //@@author 0218hy
    /** Replaces pets after validating uniqueness and resolving every owner. */
    public void setPets(List<Pet> replacement) {
        requireNonNull(replacement);
        List<Pet> resolved = replacement.stream()
                .map(pet -> pet.withOwner(findOwner(pet, getClientList())))
                .collect(Collectors.toList());
        pets.setPets(resolved);
    }
    //@@author

    public boolean hasPet(Pet pet) {
        return pets.contains(pet);
    }

    public void addPet(Pet pet) {
        pets.add(pet.withOwner(findOwner(pet, getClientList())));
    }

    public void setPet(Pet target, Pet editedPet) {
        requireAllNonNull(target, editedPet);
        Pet resolvedPet = editedPet.withOwner(findOwner(editedPet, getClientList()));
        pets.setPet(target, resolvedPet);
        if (!target.getName().equals(resolvedPet.getName())) {
            appointments.updatePetName(target, resolvedPet.getName());
        }
    }

    /** Removes a pet and all appointments belonging to that pet. */
    public void removePet(Pet pet) {
        requireNonNull(pet);
        pets.remove(pet);
        appointments.removeAppointmentsForPet(pet);
    }

    //@@author 0218hy
    /** Clears all clients, pets, and appointments, preserving the observable lists. */
    public void clear() {
        appointments.setAppointments(List.of());
        pets.setPets(List.of());
        clients.setClients(List.of());
    }
    //@@author

    //@@author 0218hy
    private static Client findOwner(Pet pet, List<Client> clients) {
        requireNonNull(pet);
        return clients.stream().filter(client -> client.isSameClient(pet.getOwner()))
                .findFirst().orElseThrow(ClientNotFoundException::new);
    }
    //@@author

    //@@author 0218hy
    public void setAppointments(List<Appointment> replacement) {
        appointments.setAppointments(replacement);
    }
    //@@author

    //@@author 0218hy
    public boolean hasOverlappingAppointment(Appointment appointment) {
        return appointments.hasOverlappingAppointment(appointment);
    }
    //@@author

    //@@author 0218hy
    public void addAppointment(Appointment appointment) {
        appointments.addAppointment(appointment);
    }
    //@@author

    public void removeAppointment(Appointment appointment) {
        appointments.removeAppointment(appointment);
    }

    @Override
    public ObservableList<Client> getClientList() {
        return clients.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Pet> getPetList() {
        return pets.asUnmodifiableObservableList();
    }

    //@@author 0218hy
    @Override
    public ObservableList<Appointment> getAppointmentList() {
        return appointments.getAppointmentList();
    }
    //@@author

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof BubuBook otherBook
                && clients.equals(otherBook.clients) && pets.equals(otherBook.pets)
                && appointments.equals(otherBook.appointments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clients, pets, appointments);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("clients", clients).add("pets", pets)
                .add("appointments", getAppointmentList()).toString();
    }
}

package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.client.Client;
import seedu.address.model.client.UniqueClientList;
import seedu.address.model.client.exceptions.ClientNotFoundException;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.UniquePetList;

/**
 * Wraps all data at the address-book level.
 * Stores unique clients and pets. Clients are unique by {@code Client#isSameClient}, while pets are unique by
 * {@code Pet#isSamePet}.
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniqueClientList clients = new UniqueClientList();
    private final UniquePetList pets = new UniquePetList();

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Clients in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the client list with {@code clients}.
     * {@code clients} must not contain duplicate clients.
     */
    public void setClients(List<Client> clients) {
        this.clients.setClients(clients);
    }

    /**
     * Replaces the contents of the pet list with {@code pets}.
     */
    public void setPets(List<Pet> pets) {
        for (Pet pet : pets) {
            if (!hasClient(pet.getOwner())) {
                throw new ClientNotFoundException();
            }
        }
        this.pets.setPets(pets);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setClients(newData.getClientList());
        setPets(newData.getPetList());
    }

    //// client-level operations

    /**
     * Returns true if a client with the same identity as {@code client} exists in the address book.
     */
    public boolean hasClient(Client client) {
        requireNonNull(client);
        return clients.contains(client);
    }

    /**
     * Adds a client to the address book.
     * The client must not already exist in the address book.
     */
    public void addClient(Client p) {
        clients.add(p);
    }

    /**
     * Replaces the given client {@code target} in the list with {@code editedClient}.
     * {@code target} must exist in the address book.
     * The client identity of {@code editedClient} must not be the same as another existing client in the address book.
     */
    public void setClient(Client target, Client editedClient) {
        requireNonNull(editedClient);

        clients.setClient(target, editedClient);
        pets.updateOwner(target, editedClient);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removeClient(Client key) {
        clients.remove(key);
    }

    //// pet-level operations

    /**
     * Returns true if a pet with the same identity as {@code pet} exists in the address book.
     */
    public boolean hasPet(Pet pet) {
        requireNonNull(pet);
        return pets.contains(pet);
    }

    /**
     * Adds a pet to the address book.
     * The pet must not already exist in the address book.
     */
    public void addPet(Pet pet) {
        if (!hasClient(pet.getOwner())) {
            throw new ClientNotFoundException();
        }
        pets.add(pet);
    }

    /**
     * Replaces the given pet with {@code editedPet}.
     */
    public void setPet(Pet target, Pet editedPet) {
        requireNonNull(editedPet);
        if (!hasClient(editedPet.getOwner())) {
            throw new ClientNotFoundException();
        }
        pets.setPet(target, editedPet);
    }

    /**
     * Removes the given pet from the address book.
     * The pet must exist in the address book.
     */
    public void removePet(Pet pet) {
        pets.remove(pet);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", clients)
                .add("pets", pets)
                .toString();
    }

    @Override
    public ObservableList<Client> getClientList() {
        return clients.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Pet> getPetList() {
        return pets.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return clients.equals(otherAddressBook.clients)
                && pets.equals(otherAddressBook.pets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clients, pets);
    }
}

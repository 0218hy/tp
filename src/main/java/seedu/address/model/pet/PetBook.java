package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.client.Client;

/** Stores unique pets. Relationships with other books are managed by ModelManager. */
public class PetBook implements ReadOnlyPetBook {
    private final UniquePetList pets = new UniquePetList();

    public PetBook() {}

    public PetBook(ReadOnlyPetBook source) {
        resetData(source);
    }

    /** Replaces this book's contents with a validated copy of the supplied data. */
    public void resetData(ReadOnlyPetBook source) {
        requireNonNull(source);
        setPets(source.getPetList());
    }

    public void setPets(List<Pet> pets) {
        this.pets.setPets(pets);
    }

    /** Returns whether a pet with the same identity exists. */
    public boolean hasPet(Pet pet) {
        return pets.contains(pet);
    }

    /** Adds a pet that does not duplicate an existing record. */
    public void addPet(Pet pet) {
        pets.add(pet);
    }

    public void setPet(Pet target, Pet editedPet) {
        pets.setPet(target, editedPet);
    }

    /** Removes an existing pet. */
    public void removePet(Pet pet) {
        pets.remove(pet);
    }

    /** Updates the owner of all pets belonging to the edited client. */
    public void updateOwner(Client targetOwner, Client editedOwner) {
        pets.updateOwner(targetOwner, editedOwner);
    }

    @Override
    public ObservableList<Pet> getPetList() {
        return pets.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PetBook otherBook && pets.equals(otherBook.pets);
    }

    @Override
    public int hashCode() {
        return pets.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("pets", pets).toString();
    }
}

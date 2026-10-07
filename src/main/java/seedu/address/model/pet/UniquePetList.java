package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.model.pet.exceptions.PetNotFoundException;

/**
 * A list of pets that enforces uniqueness and does not allow nulls.
 * Pets are considered unique when their owners and names are the same.
 */
public class UniquePetList implements Iterable<Pet> {

    private final ObservableList<Pet> internalList = FXCollections.observableArrayList();
    private final ObservableList<Pet> internalUnmodifiableList = FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains an equivalent pet as the given argument.
     */
    public boolean contains(Pet toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::isSamePet);
    }

    /**
     * Adds a pet to the list.
     */
    public void add(Pet toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicatePetException();
        }
        internalList.add(toAdd);
    }

    /**
     * Replaces the given pet with the edited pet.
     */
    public void setPet(Pet target, Pet editedPet) {
        requireAllNonNull(target, editedPet);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new PetNotFoundException();
        }
        if (!target.isSamePet(editedPet) && contains(editedPet)) {
            throw new DuplicatePetException();
        }
        internalList.set(index, editedPet);
    }

    /**
     * Replaces the owner of every pet owned by {@code targetOwner}.
     */
    public void updateOwner(Person targetOwner, Person editedOwner) {
        requireAllNonNull(targetOwner, editedOwner);
        for (int index = 0; index < internalList.size(); index++) {
            Pet pet = internalList.get(index);
            if (pet.getOwner().isSamePerson(targetOwner)) {
                internalList.set(index, pet.withOwner(editedOwner));
            }
        }
    }

    /**
     * Removes the given pet from the list.
     */
    public void remove(Pet toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new PetNotFoundException();
        }
    }

    /**
     * Replaces the contents of this list with {@code pets}.
     */
    public void setPets(List<Pet> pets) {
        requireAllNonNull(pets);
        if (!petsAreUnique(pets)) {
            throw new DuplicatePetException();
        }
        internalList.setAll(pets);
    }

    /**
     * Returns the backing list as an unmodifiable observable list.
     */
    public ObservableList<Pet> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Pet> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof UniquePetList otherUniquePetList)) {
            return false;
        }

        return internalList.equals(otherUniquePetList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    private boolean petsAreUnique(List<Pet> pets) {
        for (int firstIndex = 0; firstIndex < pets.size() - 1; firstIndex++) {
            for (int secondIndex = firstIndex + 1; secondIndex < pets.size(); secondIndex++) {
                if (pets.get(firstIndex).isSamePet(pets.get(secondIndex))) {
                    return false;
                }
            }
        }
        return true;
    }
}

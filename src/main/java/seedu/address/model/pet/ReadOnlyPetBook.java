package seedu.address.model.pet;

import javafx.collections.ObservableList;

/** Unmodifiable view of the pet book. */
public interface ReadOnlyPetBook {
    /** Returns the unique pets in insertion order. */
    ObservableList<Pet> getPetList();
}

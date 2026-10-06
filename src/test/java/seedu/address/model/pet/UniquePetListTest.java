package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.model.pet.exceptions.PetNotFoundException;
import seedu.address.testutil.PetBuilder;

public class UniquePetListTest {

    private final UniquePetList uniquePetList = new UniquePetList();

    @Test
    public void contains_nullPet_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePetList.contains(null));
    }

    @Test
    public void contains_petNotInList_returnsFalse() {
        assertFalse(uniquePetList.contains(new PetBuilder(ALICE).build()));
    }

    @Test
    public void contains_petInList_returnsTrue() {
        Pet pet = new PetBuilder(ALICE).build();
        uniquePetList.add(pet);
        assertTrue(uniquePetList.contains(pet));
    }

    @Test
    public void contains_petWithSameIdentityInList_returnsTrue() {
        Pet pet = new PetBuilder(ALICE).build();
        uniquePetList.add(pet);

        assertTrue(uniquePetList.contains(new PetBuilder(pet).withRequirement("Daily medicine").build()));
    }

    @Test
    public void add_nullPet_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePetList.add(null));
    }

    @Test
    public void add_duplicatePet_throwsDuplicatePetException() {
        Pet pet = new PetBuilder(ALICE).build();
        uniquePetList.add(pet);
        assertThrows(DuplicatePetException.class, () -> uniquePetList.add(pet));
    }

    @Test
    public void setPet_targetPetNotInList_throwsPetNotFoundException() {
        Pet pet = new PetBuilder(ALICE).build();
        assertThrows(PetNotFoundException.class, () -> uniquePetList.setPet(pet, pet));
    }

    @Test
    public void setPet_nullTarget_throwsNullPointerException() {
        Pet pet = new PetBuilder(ALICE).build();
        assertThrows(NullPointerException.class, () -> uniquePetList.setPet(null, pet));
    }

    @Test
    public void setPet_nullEditedPet_throwsNullPointerException() {
        Pet pet = new PetBuilder(ALICE).build();
        assertThrows(NullPointerException.class, () -> uniquePetList.setPet(pet, null));
    }

    @Test
    public void setPet_existingPet_replacesPet() {
        Pet pet = new PetBuilder(ALICE).build();
        Pet editedPet = new PetBuilder(pet).withRequirement("Daily medicine").build();
        uniquePetList.add(pet);

        uniquePetList.setPet(pet, editedPet);

        assertEquals(List.of(editedPet), uniquePetList.asUnmodifiableObservableList());
    }

    @Test
    public void setPet_duplicateIdentity_throwsDuplicatePetException() {
        Pet milo = new PetBuilder(ALICE).build();
        Pet luna = new PetBuilder(ALICE).withName("Luna").build();
        Pet duplicatePet = new PetBuilder(milo).withName("Luna").build();
        uniquePetList.add(milo);
        uniquePetList.add(luna);

        assertThrows(DuplicatePetException.class, () -> uniquePetList.setPet(milo, duplicatePet));
    }

    @Test
    public void updateOwner_replacesOwnerForAllMatchingPets() {
        Pet ownedByAlice = new PetBuilder(ALICE).build();
        Pet alsoOwnedByAlice = new PetBuilder(ALICE).withName("Luna").build();
        Pet ownedByBob = new PetBuilder(BOB).withName("Coco").build();
        uniquePetList.add(ownedByAlice);
        uniquePetList.add(alsoOwnedByAlice);
        uniquePetList.add(ownedByBob);
        Pet expectedUpdatedPet = new PetBuilder(ownedByAlice).withOwner(BOB).build();
        Pet expectedSecondUpdatedPet = new PetBuilder(alsoOwnedByAlice).withOwner(BOB).build();

        uniquePetList.updateOwner(ALICE, BOB);

        assertEquals(expectedUpdatedPet, uniquePetList.asUnmodifiableObservableList().get(0));
        assertEquals(expectedSecondUpdatedPet, uniquePetList.asUnmodifiableObservableList().get(1));
        assertEquals(ownedByBob, uniquePetList.asUnmodifiableObservableList().get(2));
        assertSame(BOB, uniquePetList.asUnmodifiableObservableList().get(0).getOwner());
        assertSame(BOB, uniquePetList.asUnmodifiableObservableList().get(1).getOwner());
    }

    @Test
    public void updateOwner_nullOwner_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePetList.updateOwner(null, BOB));
        assertThrows(NullPointerException.class, () -> uniquePetList.updateOwner(ALICE, null));
    }

    @Test
    public void remove_nullPet_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePetList.remove(null));
    }

    @Test
    public void remove_petNotInList_throwsPetNotFoundException() {
        assertThrows(PetNotFoundException.class, () -> uniquePetList.remove(new PetBuilder(ALICE).build()));
    }

    @Test
    public void remove_existingPet_removesPet() {
        Pet pet = new PetBuilder(ALICE).build();
        uniquePetList.add(pet);

        uniquePetList.remove(pet);

        assertEquals(List.of(), uniquePetList.asUnmodifiableObservableList());
    }

    @Test
    public void setPets_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePetList.setPets(null));
    }

    @Test
    public void setPets_uniqueList_replacesOwnList() {
        Pet milo = new PetBuilder(ALICE).build();
        Pet luna = new PetBuilder(ALICE).withName("Luna").build();
        uniquePetList.add(milo);

        uniquePetList.setPets(List.of(luna));

        assertEquals(List.of(luna), uniquePetList.asUnmodifiableObservableList());
    }

    @Test
    public void setPets_listWithDuplicatePets_throwsDuplicatePetException() {
        Pet pet = new PetBuilder(ALICE).build();

        assertThrows(DuplicatePetException.class, () -> uniquePetList.setPets(List.of(pet, pet)));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        uniquePetList.add(new PetBuilder(ALICE).build());
        ObservableList<Pet> pets = uniquePetList.asUnmodifiableObservableList();

        assertThrows(UnsupportedOperationException.class, () -> pets.remove(0));
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniquePetList.asUnmodifiableObservableList().toString(), uniquePetList.toString());
    }
}

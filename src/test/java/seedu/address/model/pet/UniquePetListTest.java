package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.model.pet.exceptions.PetNotFoundException;
import seedu.address.testutil.PetBuilder;

public class UniquePetListTest {

    private final UniquePetList uniquePetList = new UniquePetList();

    @Test
    public void contains_petInList_returnsTrue() {
        Pet pet = new PetBuilder(ALICE).build();
        uniquePetList.add(pet);
        assertTrue(uniquePetList.contains(pet));
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
    public void updateOwner_replacesOwnerForMatchingPets() {
        Pet ownedByAlice = new PetBuilder(ALICE).build();
        Pet ownedByBob = new PetBuilder(BOB).withName("Luna").build();
        uniquePetList.add(ownedByAlice);
        uniquePetList.add(ownedByBob);
        Pet expectedUpdatedPet = new PetBuilder(ownedByAlice).withOwner(BOB).build();

        uniquePetList.updateOwner(ALICE, BOB);

        assertEquals(expectedUpdatedPet, uniquePetList.asUnmodifiableObservableList().get(0));
        assertEquals(ownedByBob, uniquePetList.asUnmodifiableObservableList().get(1));
        assertFalse(uniquePetList.asUnmodifiableObservableList().get(0).getOwner() == ALICE);
    }
}

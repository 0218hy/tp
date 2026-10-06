package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PetBuilder;

public class PetTest {

    @Test
    public void isSamePet() {
        Pet pet = new PetBuilder(ALICE).build();

        assertTrue(pet.isSamePet(pet));
        assertTrue(pet.isSamePet(new PetBuilder(pet).withRequirement("Daily medicine").build()));
        assertFalse(pet.isSamePet(new PetBuilder(pet).withName("Luna").build()));
        assertFalse(pet.isSamePet(new PetBuilder(pet).withOwner(BOB).build()));
        assertFalse(pet.isSamePet(null));
    }

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        Pet pet = new PetBuilder(ALICE).build();

        assertThrows(NullPointerException.class, () -> new Pet(null, pet.getOwner(), pet.getSpecies(),
                pet.getRequirement()));
        assertThrows(NullPointerException.class, () -> new Pet(pet.getName(), null, pet.getSpecies(),
                pet.getRequirement()));
        assertThrows(NullPointerException.class, () -> new Pet(pet.getName(), pet.getOwner(), null,
                pet.getRequirement()));
        assertThrows(NullPointerException.class, () -> new Pet(pet.getName(), pet.getOwner(), pet.getSpecies(), null));
    }

    @Test
    public void equals() {
        Pet pet = new PetBuilder(ALICE).build();

        assertTrue(pet.equals(pet));
        assertTrue(pet.equals(new PetBuilder(pet).build()));
        assertFalse(pet.equals(null));
        assertFalse(pet.equals(1));
        assertFalse(pet.equals(new PetBuilder(pet).withName("Luna").build()));
        assertFalse(pet.equals(new PetBuilder(pet).withOwner(BOB).build()));
        assertFalse(pet.equals(new PetBuilder(pet).withSpecies(Species.CAT).build()));
        assertFalse(pet.equals(new PetBuilder(pet).withRequirement("Daily medicine").build()));
    }

    @Test
    public void withOwner_replacesOnlyOwner() {
        Pet pet = new PetBuilder(ALICE).build();

        Pet updatedPet = pet.withOwner(BOB);

        assertSame(BOB, updatedPet.getOwner());
        assertEquals(pet.getName(), updatedPet.getName());
        assertEquals(pet.getSpecies(), updatedPet.getSpecies());
        assertEquals(pet.getRequirement(), updatedPet.getRequirement());
    }
}

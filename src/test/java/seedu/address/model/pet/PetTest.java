package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

public class PetTest {

    @Test
    public void isOwnedBy_matchesPhoneRatherThanClientName() {
        Pet pet = new PetBuilder(ALICE).build();
        assertTrue(pet.isOwnedBy(ALICE.getPhone()));
        assertTrue(pet.isOwnedBy(new ClientBuilder(ALICE).withName("Another Name").build().getPhone()));
        assertFalse(pet.isOwnedBy(BOB.getPhone()));
        assertFalse(pet.isOwnedBy(null));
    }

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
    public void isSamePet_ownerAndName_matchesNormalizedIdentity() {
        Pet pet = new PetBuilder(ALICE).withName("Bu Bu").build();

        assertTrue(pet.isSamePet(ALICE, new PetName("  bU   bU  ")));
        assertFalse(pet.isSamePet(BOB, new PetName("Bu Bu")));
        assertFalse(pet.isSamePet(ALICE, new PetName("Luna")));
        assertFalse(pet.isSamePet(null, pet.getName()));
        assertFalse(pet.isSamePet(ALICE, null));
    }

    @Test
    public void isIdentifiedBy_matchesOwnerAndNormalizedName() {
        Pet pet = new PetBuilder(ALICE).build();

        assertTrue(pet.isIdentifiedBy(ALICE.getPhone(), new PetName("milo")));
        assertFalse(pet.isIdentifiedBy(BOB.getPhone(), new PetName("Milo")));
        assertFalse(pet.isIdentifiedBy(ALICE.getPhone(), new PetName("Luna")));
        assertThrows(NullPointerException.class, () -> pet.isIdentifiedBy(null, pet.getName()));
        assertThrows(NullPointerException.class, () -> pet.isIdentifiedBy(ALICE.getPhone(), null));
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

    @Test
    public void withName_replacesOnlyName() {
        Pet pet = new PetBuilder(ALICE).withBreed("Golden retriever").build();

        Pet renamedPet = pet.withName(new PetName("Milo Bean"));

        assertEquals(new PetName("Milo Bean"), renamedPet.getName());
        assertSame(ALICE, renamedPet.getOwner());
        assertEquals(pet.getSpecies(), renamedPet.getSpecies());
        assertEquals(pet.getBreed(), renamedPet.getBreed());
        assertEquals(pet.getRequirement(), renamedPet.getRequirement());
        assertEquals(new PetName("Milo"), pet.getName());
        assertThrows(NullPointerException.class, () -> pet.withName(null));
    }
}

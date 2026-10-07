package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.storage.JsonAdaptedPet.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Person;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;
import seedu.address.testutil.PetBuilder;

public class JsonAdaptedPetTest {

    private static final String VALID_NAME = "Milo";
    private static final String VALID_OWNER_NAME = ALICE.getName().fullName;
    private static final String VALID_SPECIES = Species.DOG.name();
    private static final String VALID_REQUIREMENT = "Daily brushing";
    private static final List<Person> VALID_PERSONS = List.of(ALICE, BOB);

    @Test
    public void toModelType_validPetDetails_returnsPetWithReferencedOwner() throws Exception {
        Pet expectedPet = new PetBuilder(ALICE).withName(VALID_NAME).withRequirement(VALID_REQUIREMENT).build();
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, VALID_SPECIES, VALID_REQUIREMENT);

        Pet actualPet = pet.toModelType(VALID_PERSONS);

        assertEquals(expectedPet, actualPet);
        assertSame(ALICE, actualPet.getOwner());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet("Milo!", VALID_OWNER_NAME, VALID_SPECIES, VALID_REQUIREMENT);
        assertThrows(IllegalValueException.class, PetName.MESSAGE_CONSTRAINTS, () -> pet.toModelType(VALID_PERSONS));
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(null, VALID_OWNER_NAME, VALID_SPECIES, VALID_REQUIREMENT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, PetName.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_PERSONS));
    }

    @Test
    public void toModelType_nullOwnerName_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, null, VALID_SPECIES, VALID_REQUIREMENT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "OwnerName");
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_PERSONS));
    }

    @Test
    public void toModelType_unknownOwner_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, "Unknown Owner", VALID_SPECIES, VALID_REQUIREMENT);
        String expectedMessage = JsonAdaptedPet.MESSAGE_OWNER_NOT_FOUND;
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_PERSONS));
    }

    @Test
    public void toModelType_invalidSpecies_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, "HORSE", VALID_REQUIREMENT);
        String expectedMessage = "Pet species is invalid.";
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_PERSONS));
    }

    @Test
    public void toModelType_nullSpecies_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, null, VALID_REQUIREMENT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Species.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_PERSONS));
    }

    @Test
    public void toModelType_invalidRequirement_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, VALID_SPECIES, " ");
        String expectedMessage = Requirement.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_PERSONS));
    }

    @Test
    public void toModelType_nullRequirement_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, VALID_SPECIES, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Requirement.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_PERSONS));
    }
}

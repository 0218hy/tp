package seedu.address.storage.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.storage.pet.JsonAdaptedPet.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

public class JsonAdaptedPetTest {

    private static final String VALID_NAME = "Milo";
    private static final String VALID_OWNER_NAME = ALICE.getName().fullName;
    private static final String COMPOSED_OWNER_NAME = "Jos\u00e9 Tan";
    private static final String DECOMPOSED_OWNER_NAME = "Jose\u0301 Tan";
    private static final String VALID_SPECIES = Species.DOG.name();
    private static final String VALID_REQUIREMENT = "Daily brushing";
    private static final List<Client> VALID_CLIENTS = List.of(ALICE, BOB);

    //@@author khangsian0412
    @Test
    public void toModelType_validPetDetails_returnsPetWithReferencedOwner() throws Exception {
        Pet expectedPet = new PetBuilder(ALICE).withName(VALID_NAME).withRequirement(VALID_REQUIREMENT).build();
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, VALID_SPECIES, VALID_REQUIREMENT);

        Pet actualPet = pet.toModelType(VALID_CLIENTS);

        assertEquals(expectedPet, actualPet);
        assertSame(ALICE, actualPet.getOwner());
    }
    //@@author

    // @@author xinyu-zxy
    @Test
    public void toModelType_breedAndOwnerPhone_preserved() throws Exception {
        // EP: current-format record with an owner phone and optional breed
        String breed = "Golden retriever";
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, ALICE.getPhone().value,
                VALID_SPECIES, breed, VALID_REQUIREMENT);

        Pet actualPet = pet.toModelType(VALID_CLIENTS);

        assertEquals(breed, actualPet.getBreed().value);
        assertSame(ALICE, actualPet.getOwner());
    }

    @Test
    public void toModelType_legacyOwnerNameMatchesMultipleClients_throwsIllegalValueException() {
        // EP: legacy owner name matches multiple clients
        Client sameNameClient = new ClientBuilder(BOB).withName(VALID_OWNER_NAME).build();
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, VALID_SPECIES, VALID_REQUIREMENT);

        String expectedMessage = JsonAdaptedPet.MESSAGE_AMBIGUOUS_OWNER;
        List<Client> clientsWithSameName = List.of(ALICE, sameNameClient);
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(clientsWithSameName));
    }

    @Test
    public void toModelType_ownerPhoneDistinguishesClientsWithSameName_returnsCorrectOwner() throws Exception {
        // EP: owner phone distinguishes clients with the same name
        Client sameNameClient = new ClientBuilder(BOB).withName(VALID_OWNER_NAME).build();
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, sameNameClient.getPhone().value,
                VALID_SPECIES, null, VALID_REQUIREMENT);

        assertSame(sameNameClient, pet.toModelType(List.of(ALICE, sameNameClient)).getOwner());
    }

    @Test
    public void toModelType_ownerNameConflictsWithPhone_throwsIllegalValueException() {
        // EP: stored owner phone and owner name identify different clients
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, BOB.getPhone().value,
                VALID_SPECIES, null, VALID_REQUIREMENT);

        String expectedMessage = JsonAdaptedPet.MESSAGE_OWNER_DETAILS_MISMATCH;
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_CLIENTS));
    }

    @Test
    public void toModelType_currentPetWithCanonicallyEquivalentOwnerName_returnsOwner() throws Exception {
        Client owner = new ClientBuilder(ALICE).withName(COMPOSED_OWNER_NAME).build();
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, DECOMPOSED_OWNER_NAME, owner.getPhone().value,
                VALID_SPECIES, null, VALID_REQUIREMENT);

        assertSame(owner, pet.toModelType(List.of(owner)).getOwner());
    }

    @Test
    public void toModelType_legacyPetWithCanonicallyEquivalentOwnerName_returnsOwner() throws Exception {
        Client owner = new ClientBuilder(ALICE).withName(DECOMPOSED_OWNER_NAME).build();
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, COMPOSED_OWNER_NAME, VALID_SPECIES, VALID_REQUIREMENT);

        assertSame(owner, pet.toModelType(List.of(owner)).getOwner());
    }

    @Test
    public void toModelType_legacyPetWithCanonicallyEquivalentClientNames_throwsAmbiguousOwnerException() {
        Client firstCanonicallyEquivalentOwner = new ClientBuilder(ALICE).withName(COMPOSED_OWNER_NAME).build();
        Client secondCanonicallyEquivalentOwner = new ClientBuilder(BOB).withName(DECOMPOSED_OWNER_NAME).build();
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, COMPOSED_OWNER_NAME, VALID_SPECIES, VALID_REQUIREMENT);

        assertThrows(IllegalValueException.class, JsonAdaptedPet.MESSAGE_AMBIGUOUS_OWNER, () ->
                pet.toModelType(List.of(firstCanonicallyEquivalentOwner, secondCanonicallyEquivalentOwner)));
    }

    @Test
    public void toModelType_ownerPhoneAndCanonicallyDifferentName_throwsIllegalValueException() {
        Client owner = new ClientBuilder(ALICE).withName("Jose Tan").build();
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, COMPOSED_OWNER_NAME, owner.getPhone().value,
                VALID_SPECIES, null, VALID_REQUIREMENT);

        assertThrows(IllegalValueException.class, JsonAdaptedPet.MESSAGE_OWNER_DETAILS_MISMATCH, () ->
                pet.toModelType(List.of(owner)));
    }
    // @@author

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet("Milo!", VALID_OWNER_NAME, VALID_SPECIES, VALID_REQUIREMENT);
        assertThrows(IllegalValueException.class, PetName.MESSAGE_CONSTRAINTS, () -> pet.toModelType(VALID_CLIENTS));
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(null, VALID_OWNER_NAME, VALID_SPECIES, VALID_REQUIREMENT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, PetName.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_CLIENTS));
    }

    //@@author khangsian0412
    @Test
    public void toModelType_nullOwnerName_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, null, VALID_SPECIES, VALID_REQUIREMENT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "OwnerName");
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_CLIENTS));
    }

    @Test
    public void toModelType_unknownOwner_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, "Unknown Owner", VALID_SPECIES, VALID_REQUIREMENT);
        String expectedMessage = JsonAdaptedPet.MESSAGE_OWNER_NOT_FOUND;
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_CLIENTS));
    }
    //@@author

    @Test
    public void toModelType_invalidSpecies_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, "HORSE", VALID_REQUIREMENT);
        String expectedMessage = "Pet species is invalid.";
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_CLIENTS));
    }

    @Test
    public void toModelType_nullSpecies_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, null, VALID_REQUIREMENT);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Species.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_CLIENTS));
    }

    @Test
    public void toModelType_invalidRequirement_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, VALID_SPECIES, " ");
        String expectedMessage = Requirement.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_CLIENTS));
    }

    @Test
    public void toModelType_nullRequirement_throwsIllegalValueException() {
        JsonAdaptedPet pet = new JsonAdaptedPet(VALID_NAME, VALID_OWNER_NAME, VALID_SPECIES, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Requirement.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, () -> pet.toModelType(VALID_CLIENTS));
    }
}

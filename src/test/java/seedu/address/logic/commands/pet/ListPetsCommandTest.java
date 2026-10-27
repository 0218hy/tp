package seedu.address.logic.commands.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetOwnerPhoneMatchesPredicate;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;
import seedu.address.testutil.TypicalClients;

// @@author xinyu-zxy
/**
 * Tests listing pet profiles without changing the saved model.
 */
public class ListPetsCommandTest {
    private static final PetOwnerPhoneMatchesPredicate ALICE_PREDICATE =
            new PetOwnerPhoneMatchesPredicate(ALICE.getPhone());
    private static final PetOwnerPhoneMatchesPredicate UNKNOWN_OWNER_PREDICATE =
            new PetOwnerPhoneMatchesPredicate(new Phone("91234567"));

    @Test
    public void execute_allPets_groupsAndSortsByOwnerThenPet() throws Exception {
        // EP: no owner filter
        Model model = createModel();
        Pet zoe = addPet(model, BENSON, "Zoe", Species.CAT, null);
        Pet mochi = addPet(model, ALICE, "mochi", Species.DOG, "Golden retriever");
        Pet bubu = addPet(model, ALICE, "BuBu", Species.CAT, null);
        List<Pet> savedOrder = List.copyOf(model.getBubuBook().getPetList());
        model.updateFilteredPetList(pet -> false);

        String expected = "Listed all pets.";
        CommandResult result = new ListPetsCommand(null).execute(model);
        assertEquals(expected, result.getFeedbackToUser());
        assertEquals(CommandResult.DisplayType.PETS, result.getDisplayType());
        assertEquals(savedOrder, model.getBubuBook().getPetList());
        assertEquals(List.of(bubu, mochi, zoe), model.getFilteredPetList());
    }

    @Test
    public void execute_ownersWithSameName_keepsPetsInSeparateGroups() throws Exception {
        // EP: different owner phones with identical owner names
        Model model = createModel();
        Client sameNameOwner = new ClientBuilder(ALICE).withPhone("81234567")
                .withEmail("other-alice@example.com").build();
        model.addClient(sameNameOwner);
        Pet mochi = addPet(model, ALICE, "Mochi", Species.DOG, null);
        Pet otherMochi = addPet(model, sameNameOwner, "Mochi", Species.CAT, null);
        Pet bubu = addPet(model, ALICE, "BuBu", Species.CAT, null);

        String expected = "Listed all pets.";
        assertEquals(expected, new ListPetsCommand(null).execute(model).getFeedbackToUser());
        assertEquals(List.of(otherMochi, bubu, mochi), model.getFilteredPetList());
    }

    @Test
    public void execute_ownerFilter_sortsOnlySelectedOwnersPets() throws Exception {
        // EP: owner filter matching multiple pets
        Model model = createModel();
        addPet(model, BENSON, "Zoe", Species.DOG, null);
        addPet(model, ALICE, "Mochi", Species.DOG, "Golden retriever");
        addPet(model, ALICE, "BuBu", Species.CAT, null);

        String expected = "Found 2 pet(s) under Alice Pauline.";
        CommandResult result = new ListPetsCommand(ALICE_PREDICATE).execute(model);
        assertEquals(expected, result.getFeedbackToUser());
        assertEquals(CommandResult.DisplayType.PETS, result.getDisplayType());
        assertEquals(2, model.getFilteredPetList().size());
        assertTrue(model.getFilteredPetList().stream().allMatch(ALICE_PREDICATE));
    }

    @Test
    public void execute_ownerFilterWithSameNameOwners_listsOnlyMatchingPhone() throws Exception {
        // EP: owner filter matches one of two same-name owners by phone
        Model model = createModel();
        Client sameNameOwner = new ClientBuilder(ALICE).withPhone("81234567")
                .withEmail("other-alice@example.com").build();
        model.addClient(sameNameOwner);
        Pet alicePet = addPet(model, ALICE, "Mochi", Species.DOG, null);
        addPet(model, sameNameOwner, "Mochi", Species.CAT, null);
        List<Pet> savedPets = List.copyOf(model.getBubuBook().getPetList());

        CommandResult result = new ListPetsCommand(ALICE_PREDICATE).execute(model);

        assertEquals("Found 1 pet(s) under Alice Pauline.", result.getFeedbackToUser());
        assertEquals(CommandResult.DisplayType.PETS, result.getDisplayType());
        assertEquals(List.of(alicePet), model.getFilteredPetList());
        assertEquals(savedPets, model.getBubuBook().getPetList());
    }

    @Test
    public void execute_emptyResults_returnsSpecificMessages() throws Exception {
        Model model = createModel();

        // EP: no owner filter and no saved pets
        assertEquals("Listed all pets.",
                new ListPetsCommand(null).execute(model).getFeedbackToUser());

        // EP: owner filter matching no pets in an empty pet list
        assertEquals("Alice Pauline has no saved pet profiles.",
                new ListPetsCommand(ALICE_PREDICATE).execute(model).getFeedbackToUser());

        addPet(model, BENSON, "Zoe", Species.CAT, null);

        // EP: owner filter matching no pets in a non-empty pet list
        assertEquals("Alice Pauline has no saved pet profiles.",
                new ListPetsCommand(ALICE_PREDICATE).execute(model).getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredPetList());
    }

    @Test
    public void execute_singlePet_returnsCountAndPreservesProfile() throws Exception {
        // EP: owner filter matching exactly one pet
        // BVA: one matching pet (smallest non-empty result)
        Model model = createModel();
        Pet pip = addPet(model, ALICE, "Pip", Species.GUINEA_PIG, null);

        assertEquals("Found 1 pet(s) under Alice Pauline.",
                new ListPetsCommand(ALICE_PREDICATE).execute(model).getFeedbackToUser());
        assertEquals(List.of(pip), model.getFilteredPetList());
    }

    @Test
    public void execute_unknownOwner_throwsCommandException() {
        // EP: owner filter does not identify a registered client
        Model model = createModel();
        addPet(model, ALICE, "Mochi", Species.DOG, null);
        model.updateFilteredPetList(pet -> false);
        String message = ListPetsCommand.MESSAGE_OWNER_NOT_FOUND;
        assertThrows(CommandException.class, message, () ->
                new ListPetsCommand(UNKNOWN_OWNER_PREDICATE).execute(model));
        assertEquals(1, model.getBubuBook().getPetList().size());
        assertEquals(List.of(), model.getFilteredPetList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        // EP: null execution model
        assertThrows(NullPointerException.class, () -> new ListPetsCommand(null).execute(null));
    }

    @Test
    public void equals_samePhone_equalCommands() {
        // EP: filtered commands containing the same owner phone
        assertEquals(new ListPetsCommand(ALICE_PREDICATE),
                new ListPetsCommand(new PetOwnerPhoneMatchesPredicate(ALICE.getPhone())));

        // EP: unfiltered commands
        assertEquals(new ListPetsCommand(null), new ListPetsCommand(null));
        assertEquals(new ListPetsCommand(ALICE_PREDICATE).hashCode(),
                new ListPetsCommand(new PetOwnerPhoneMatchesPredicate(ALICE.getPhone())).hashCode());
    }

    /**
     * Returns a model containing the typical registered clients and no pets.
     */
    private Model createModel() {
        return new ModelManager(TypicalClients.getTypicalBubuBook(), new UserPrefs());
    }

    /**
     * Adds a test pet to the model using its existing owner.
     *
     * @param model The model receiving the test pet.
     * @param owner The pet's registered owner.
     * @param name The pet's name.
     * @param species The pet's species.
     * @param breed The pet's breed, or {@code null} when it is not recorded.
     */
    private Pet addPet(Model model, Client owner, String name,
            Species species, String breed) {
        PetBuilder builder = new PetBuilder(owner).withName(name).withSpecies(species);
        if (breed != null) {
            builder.withBreed(breed);
        }
        Pet pet = builder.build();
        model.addPet(pet);
        return pet;
    }
}

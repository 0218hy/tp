package seedu.address.logic.commands.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetOwnerPhoneMatchesPredicate;
import seedu.address.model.pet.Species;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PetBuilder;
import seedu.address.testutil.TypicalPersons;

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
        Model model = createModel();
        addPet(model, BENSON, "Zoe", Species.CAT, null);
        addPet(model, ALICE, "mochi", Species.DOG, "Golden retriever");
        addPet(model, ALICE, "BuBu", Species.CAT, null);
        List<Pet> savedOrder = List.copyOf(model.getAddressBook().getPetList());
        model.updateFilteredPetList(pet -> false);

        String expected = "All pet profiles\n\nAlice Pauline\n"
                + "BuBu Cat Daily walk\nmochi Dog Golden retriever Daily walk\n\n"
                + "Benson Meier\nZoe Cat Daily walk";
        assertEquals(expected, new ListPetsCommand(null).execute(model).getFeedbackToUser());
        assertEquals(savedOrder, model.getAddressBook().getPetList());
        assertEquals(savedOrder, model.getFilteredPetList());
    }

    @Test
    public void execute_ownersWithSameName_keepsPetsInSeparateGroups() throws Exception {
        Model model = createModel();
        Person sameNameOwner = new PersonBuilder(ALICE).withPhone("81234567")
                .withEmail("other-alice@example.com").build();
        model.addPerson(sameNameOwner);
        addPet(model, ALICE, "Mochi", Species.DOG, null);
        addPet(model, sameNameOwner, "Mochi", Species.CAT, null);
        addPet(model, ALICE, "BuBu", Species.CAT, null);

        String expected = "All pet profiles\n\nAlice Pauline\nMochi Cat Daily walk\n\n"
                + "Alice Pauline\nBuBu Cat Daily walk\nMochi Dog Daily walk";
        assertEquals(expected, new ListPetsCommand(null).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_ownerFilter_sortsOnlySelectedOwnersPets() throws Exception {
        Model model = createModel();
        addPet(model, BENSON, "Zoe", Species.DOG, null);
        addPet(model, ALICE, "Mochi", Species.DOG, "Golden retriever");
        addPet(model, ALICE, "BuBu", Species.CAT, null);

        String expected = "Pets under Alice Pauline\nBuBu Cat Daily walk\n"
                + "Mochi Dog Golden retriever Daily walk";
        assertEquals(expected, new ListPetsCommand(ALICE_PREDICATE).execute(model).getFeedbackToUser());
        assertEquals(2, model.getFilteredPetList().size());
        assertTrue(model.getFilteredPetList().stream().allMatch(ALICE_PREDICATE));
    }

    @Test
    public void execute_emptyResults_returnsSpecificMessages() throws Exception {
        Model model = createModel();

        assertEquals(ListPetsCommand.MESSAGE_NO_PETS,
                new ListPetsCommand(null).execute(model).getFeedbackToUser());
        assertEquals("Alice Pauline has no saved pet profiles.",
                new ListPetsCommand(ALICE_PREDICATE).execute(model).getFeedbackToUser());

        addPet(model, BENSON, "Zoe", Species.CAT, null);
        assertEquals("Alice Pauline has no saved pet profiles.",
                new ListPetsCommand(ALICE_PREDICATE).execute(model).getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredPetList());
    }

    @Test
    public void execute_guineaPig_displaysReadableSpecies() throws Exception {
        Model model = createModel();
        addPet(model, ALICE, "Pip", Species.GUINEA_PIG, null);

        assertEquals("Pets under Alice Pauline\nPip Guinea pig Daily walk",
                new ListPetsCommand(ALICE_PREDICATE).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_unknownOwner_throwsCommandException() {
        Model model = createModel();
        addPet(model, ALICE, "Mochi", Species.DOG, null);
        model.updateFilteredPetList(pet -> false);
        String message = ListPetsCommand.MESSAGE_OWNER_NOT_FOUND;
        assertThrows(CommandException.class, message, () ->
                new ListPetsCommand(UNKNOWN_OWNER_PREDICATE).execute(model));
        assertEquals(1, model.getAddressBook().getPetList().size());
        assertEquals(List.of(), model.getFilteredPetList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListPetsCommand(null).execute(null));
    }

    @Test
    public void equals_samePhone_equalCommands() {
        assertEquals(new ListPetsCommand(ALICE_PREDICATE),
                new ListPetsCommand(new PetOwnerPhoneMatchesPredicate(ALICE.getPhone())));
        assertEquals(new ListPetsCommand(null), new ListPetsCommand(null));
        assertEquals(new ListPetsCommand(ALICE_PREDICATE).hashCode(),
                new ListPetsCommand(new PetOwnerPhoneMatchesPredicate(ALICE.getPhone())).hashCode());
    }

    /**
     * Returns a model containing the typical registered clients and no pets.
     */
    private Model createModel() {
        return new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
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
    private void addPet(Model model, Person owner, String name,
            Species species, String breed) {
        PetBuilder builder = new PetBuilder(owner).withName(name).withSpecies(species);
        if (breed != null) {
            builder.withBreed(breed);
        }
        model.addPet(builder.build());
    }
}

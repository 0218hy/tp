package seedu.address.logic.commands.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;
import seedu.address.testutil.PetBuilder;
import seedu.address.testutil.TypicalClients;

// @@author xinyu-zxy
/** Tests the add-pet command against the model. */
public class AddPetCommandTest {
    private static final String OWNER_PHONE = TypicalClients.ALICE.getPhone().value;
    private static final String REQUIREMENTS = "Calm with baths";

    @Test
    public void execute_withAndWithoutBreed_addsPets() throws Exception {
        Model model = new ModelManager(TypicalClients.getTypicalBubuBook(), new UserPrefs());

        // EP: pet with an optional breed
        AddPetCommand first = command("Mochi", OWNER_PHONE, new Breed("Golden retriever"));
        // EP: pet without an optional breed
        AddPetCommand second = command("BuBu", OWNER_PHONE, null);

        assertEquals("Pet added: Mochi (Dog, Golden retriever) under Alice Pauline.",
                first.execute(model).getFeedbackToUser());
        assertEquals("Pet added: BuBu (Dog) under Alice Pauline.", second.execute(model).getFeedbackToUser());
        assertEquals(2, model.getBubuBook().getPetList().size());
    }

    @Test
    public void execute_normalizedDuplicate_rejectedOnlyForSameOwner() throws Exception {
        Model model = new ModelManager(TypicalClients.getTypicalBubuBook(), new UserPrefs());
        command("Mochi", OWNER_PHONE, null).execute(model);

        // EP: same owner and duplicate name after case normalization
        assertThrows(CommandException.class, "This client already has a pet named Mochi.", () ->
                command("mochi", OWNER_PHONE, null).execute(model));

        // EP: same owner and duplicate name after whitespace normalization
        assertThrows(CommandException.class, "This client already has a pet named Mo chi.", () -> {
            command("Mo chi", OWNER_PHONE, null).execute(model);
            command("Mo  chi", OWNER_PHONE, null).execute(model);
        });

        // EP: different owner and same normalized pet name
        command("Mochi", TypicalClients.BENSON.getPhone().value, null).execute(model);
        assertEquals(3, model.getBubuBook().getPetList().size());
    }

    @Test
    public void execute_modelDoesNotStorePet_assertionFails() {
        // EP: model violates the postcondition by dropping the added pet
        Model model = new ModelManager(TypicalClients.getTypicalBubuBook(), new UserPrefs()) {
            @Override
            public void addPet(Pet pet) {
                // Simulates a model regression that silently drops an added pet.
            }
        };
        assertThrows(AssertionError.class, () -> command("Mochi", OWNER_PHONE, null).execute(model));
    }

    @Test
    public void execute_unknownOwner_rejected() {
        Model model = new ModelManager(TypicalClients.getTypicalBubuBook(), new UserPrefs());

        // EP: owner phone does not identify a registered client
        assertThrows(CommandException.class, AddPetCommand.MESSAGE_OWNER_NOT_FOUND, () ->
                command("Mochi", "91234567", null).execute(model));
        assertEquals(List.of(), model.getBubuBook().getPetList());
    }

    @Test
    public void execute_sameNameWithDifferentBreed_rejectsWithoutMutation() {
        Model model = new ModelManager(TypicalClients.getTypicalBubuBook(), new UserPrefs());
        Pet existingPet = new PetBuilder(TypicalClients.ALICE).withName("Mochi").withBreed("Bengal").build();
        model.addPet(existingPet);
        List<Pet> petsBefore = List.copyOf(model.getBubuBook().getPetList());

        // EP: same owner and duplicate normalized name with different non-identifying details
        assertThrows(CommandException.class, "This client already has a pet named Mochi.", () ->
                command("mochi", OWNER_PHONE, new Breed("British Shorthair")).execute(model));
        assertEquals(petsBefore, model.getBubuBook().getPetList());
    }

    @Test
    public void constructorAndExecute_nullRequiredArguments_rejected() {
        PetName name = new PetName("Mochi");
        Requirement requirement = new Requirement(REQUIREMENTS);

        // EP: each required constructor argument is null in turn
        assertThrows(NullPointerException.class, () ->
                new AddPetCommand(null, OWNER_PHONE, Species.DOG, null, requirement));
        assertThrows(NullPointerException.class, () ->
                new AddPetCommand(name, null, Species.DOG, null, requirement));
        assertThrows(NullPointerException.class, () ->
                new AddPetCommand(name, OWNER_PHONE, null, null, requirement));
        assertThrows(NullPointerException.class, () ->
                new AddPetCommand(name, OWNER_PHONE, Species.DOG, null, null));

        // EP: execution model is null
        assertThrows(NullPointerException.class, () -> command("Mochi", OWNER_PHONE, null).execute(null));
    }

    /** Creates a valid add-pet command for the given owner and breed. */
    private AddPetCommand command(String name, String ownerPhone, Breed breed) {
        return new AddPetCommand(new PetName(name), ownerPhone, Species.DOG, breed,
                new Requirement(REQUIREMENTS));
    }
}

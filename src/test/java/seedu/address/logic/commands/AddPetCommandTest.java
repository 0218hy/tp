package seedu.address.logic.commands;

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
import seedu.address.testutil.TypicalPersons;

/** Tests the add-pet command against the model. */
public class AddPetCommandTest {
    private static final String OWNER_PHONE = TypicalPersons.ALICE.getPhone().value;
    private static final String REQUIREMENTS = "Calm with baths";

    @Test
    public void execute_withAndWithoutBreed_addsPets() throws Exception {
        Model model = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        AddPetCommand first = command("Mochi", OWNER_PHONE, new Breed("Golden retriever"));
        AddPetCommand second = command("BuBu", OWNER_PHONE, null);

        assertEquals("Pet added: Mochi (Dog, Golden retriever) under Alice Pauline.",
                first.execute(model).getFeedbackToUser());
        assertEquals("Pet added: BuBu (Dog) under Alice Pauline.", second.execute(model).getFeedbackToUser());
        assertEquals(2, model.getAddressBook().getPetList().size());
    }

    @Test
    public void execute_normalizedDuplicate_rejectedOnlyForSameOwner() throws Exception {
        Model model = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        command("Mochi", OWNER_PHONE, null).execute(model);
        assertThrows(CommandException.class, "This client already has a pet named Mochi.", () ->
                command("mochi", OWNER_PHONE, null).execute(model));
        assertThrows(CommandException.class, "This client already has a pet named Mo chi.", () -> {
            command("Mo chi", OWNER_PHONE, null).execute(model);
            command("Mo  chi", OWNER_PHONE, null).execute(model);
        });
        command("Mochi", TypicalPersons.BENSON.getPhone().value, null).execute(model);
        assertEquals(3, model.getAddressBook().getPetList().size());
    }

    @Test
    public void execute_unknownOwner_rejected() {
        Model model = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        assertThrows(CommandException.class, AddPetCommand.MESSAGE_OWNER_NOT_FOUND, () ->
                command("Mochi", "91234567", null).execute(model));
        assertEquals(List.of(), model.getAddressBook().getPetList());
    }

    @Test
    public void execute_sameNameWithDifferentBreed_rejectsWithoutMutation() {
        Model model = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        Pet existingPet = new PetBuilder(TypicalPersons.ALICE).withName("Mochi").withBreed("Bengal").build();
        model.addPet(existingPet);
        List<Pet> petsBefore = List.copyOf(model.getAddressBook().getPetList());

        assertThrows(CommandException.class, "This client already has a pet named Mochi.", () ->
                command("mochi", OWNER_PHONE, new Breed("British Shorthair")).execute(model));
        assertEquals(petsBefore, model.getAddressBook().getPetList());
    }

    @Test
    public void constructorAndExecute_nullRequiredArguments_rejected() {
        PetName name = new PetName("Mochi");
        Requirement requirement = new Requirement(REQUIREMENTS);
        assertThrows(NullPointerException.class, () ->
                new AddPetCommand(null, OWNER_PHONE, Species.DOG, null, requirement));
        assertThrows(NullPointerException.class, () ->
                new AddPetCommand(name, null, Species.DOG, null, requirement));
        assertThrows(NullPointerException.class, () ->
                new AddPetCommand(name, OWNER_PHONE, null, null, requirement));
        assertThrows(NullPointerException.class, () ->
                new AddPetCommand(name, OWNER_PHONE, Species.DOG, null, null));
        assertThrows(NullPointerException.class, () -> command("Mochi", OWNER_PHONE, null).execute(null));
    }

    /** Creates a valid add-pet command for the given owner and breed. */
    private AddPetCommand command(String name, String ownerPhone, Breed breed) {
        return new AddPetCommand(new PetName(name), ownerPhone, Species.DOG, breed,
                new Requirement(REQUIREMENTS));
    }
}

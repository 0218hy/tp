package seedu.address.ui.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import seedu.address.model.pet.Pet;
import seedu.address.testutil.PetBuilder;
import seedu.address.ui.JavaFxTestUtils;

public class PetCardTest {

    //@@author chunghean
    @Test
    public void constructor_withoutBreed_displaysPetDetails() {
        JavaFxTestUtils.runAndWait(() -> {
            Pet pet = new PetBuilder(ALICE).build();
            PetCard card = new PetCard(pet, 2);

            assertEquals(pet, card.pet);
            assertEquals("2. ", getLabel(card, "id").getText());
            assertEquals("Milo", getLabel(card, "name").getText());
            assertEquals("Owner: Alice Pauline (94351253)", getLabel(card, "owner").getText());
            assertEquals("Species: Dog", getLabel(card, "species").getText());
            assertEquals("Breed: Not recorded", getLabel(card, "breed").getText());
            assertEquals("Requirement: Daily walk", getLabel(card, "requirement").getText());
            assertNotNull(card.getRoot());
        });
    }

    @Test
    public void constructor_withBreed_displaysBreed() {
        JavaFxTestUtils.runAndWait(() -> {
            PetCard card = new PetCard(new PetBuilder(ALICE).withBreed("Golden retriever").build(), 1);

            assertEquals("Breed: Golden retriever", getLabel(card, "breed").getText());
        });
    }
    //@@author

    // @@author xinyu-zxy
    @Test
    public void constructor_sameOwner_hidesHeadingWithoutResettingIndex() {
        JavaFxTestUtils.runAndWait(() -> {
            // EP: pet does not start a new owner group
            PetCard card = new PetCard(new PetBuilder(ALICE).build(), 2, false);
            assertFalse(getLabel(card, "owner").isVisible());
            assertFalse(getLabel(card, "owner").isManaged());
            assertEquals("2. ", getLabel(card, "id").getText());
        });
    }
    // @@author

    private static Label getLabel(PetCard card, String fieldName) {
        try {
            Field field = PetCard.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Label) field.get(card);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}

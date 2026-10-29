package seedu.address.ui.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.TypicalClients.ALICE;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Label;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;
import seedu.address.ui.JavaFxTestUtils;

public class PetListPanelTest {

    //@@author chunghean
    @Test
    public void constructorAndCell_updateGraphicForEmptyAndPopulatedItems() {
        JavaFxTestUtils.runAndWait(() -> {
            Pet pet = new PetBuilder(ALICE).build();
            ObservableList<Pet> pets = FXCollections.observableArrayList(pet);
            PetListPanel panel = new PetListPanel(pets);
            PetListPanel.PetListViewCell cell = panel.new PetListViewCell();

            // EP: empty cell
            assertEquals(pets, panelList(panel));
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());

            // EP: populated cell
            cell.updateItem(pet, false);
            assertNotNull(cell.getGraphic());

            // EP: cell with a null item despite not being marked empty
            cell.updateItem(null, false);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());
        });
    }
    //@@author

    // @@author xinyu-zxy
    @Test
    public void updateItem_ownerGroups_showsOnlyFirstHeadingAndKeepsContinuousIndices() {
        JavaFxTestUtils.runAndWait(() -> {
            Pet firstPet = new PetBuilder(ALICE).withName("Bubu").build();
            Pet secondPet = new PetBuilder(ALICE).withName("Mochi").build();
            Client sameNameOwner = new ClientBuilder(ALICE).withPhone("81234567").build();
            Pet thirdPet = new PetBuilder(sameNameOwner).withName("Luna").build();
            ObservableList<Pet> pets = FXCollections.observableArrayList(firstPet, secondPet, thirdPet);
            PetListPanel panel = new PetListPanel(pets);
            PetListPanel.PetListViewCell cell = panel.new PetListViewCell();

            // EP: first pet in an owner group
            // BVA: index zero (first valid list index)
            cell.updateIndex(0);
            cell.updateItem(firstPet, false);
            assertCard(cell, true, "1. ", "Bubu");

            // EP: pet with the same owner as the previous pet
            cell.updateIndex(1);
            cell.updateItem(secondPet, false);
            assertCard(cell, false, "2. ", "Mochi");

            // EP: pet with a different owner from the previous pet
            cell.updateIndex(2);
            cell.updateItem(thirdPet, false);
            assertCard(cell, true, "3. ", "Luna");
            assertEquals("Owner: Alice Pauline (81234567)", getLabel(cell, "owner").getText());
        });
    }

    @Test
    public void updateItem_temporaryIndices_showsHeadingWithoutReadingPreviousPet() {
        JavaFxTestUtils.runAndWait(() -> {
            Pet pet = new PetBuilder(ALICE).build();
            ObservableList<Pet> pets = FXCollections.observableArrayList(pet);
            PetListPanel panel = new PetListPanel(pets);
            PetListPanel.PetListViewCell cell = panel.new PetListViewCell();

            // EP: index below the valid list range
            // BVA: index minus one (immediately below the first valid index)
            cell.updateIndex(-1);
            cell.updateItem(pet, false);
            assertCard(cell, true, "0. ", "Milo");

            // EP: index at or above the list size
            // BVA: index equal to the list size (immediately above the last valid index)
            cell.updateIndex(pets.size());
            cell.updateItem(pet, false);
            assertCard(cell, true, "2. ", "Milo");

            cell.updateIndex(pets.size() + 1);
            cell.updateItem(pet, false);
            assertCard(cell, true, "3. ", "Milo");
        });
    }

    @Test
    public void updateItem_afterFilteringAndDeletion_recalculatesGroupHeading() {
        JavaFxTestUtils.runAndWait(() -> {
            Pet firstPet = new PetBuilder(ALICE).withName("Bubu").build();
            Pet secondPet = new PetBuilder(ALICE).withName("Mochi").build();
            ObservableList<Pet> savedPets = FXCollections.observableArrayList(firstPet, secondPet);
            FilteredList<Pet> displayedPets = new FilteredList<>(savedPets);
            PetListPanel panel = new PetListPanel(displayedPets);
            PetListPanel.PetListViewCell cell = panel.new PetListViewCell();
            cell.updateIndex(1);
            cell.updateItem(secondPet, false);
            assertCard(cell, false, "2. ", "Mochi");

            // EP: filtering makes an existing pet the first item in its owner group
            displayedPets.setPredicate(pet -> pet.equals(secondPet));
            cell.updateIndex(0);
            cell.updateItem(secondPet, false);
            assertCard(cell, true, "1. ", "Mochi");

            // EP: removing the filter restores a preceding pet with the same owner
            displayedPets.setPredicate(pet -> true);
            cell.updateIndex(1);
            cell.updateItem(secondPet, false);
            assertCard(cell, false, "2. ", "Mochi");

            // EP: deletion makes an existing pet the first item in its owner group
            savedPets.remove(firstPet);
            cell.updateIndex(0);
            cell.updateItem(secondPet, false);
            assertCard(cell, true, "1. ", "Mochi");

            // EP: deleting all pets produces an empty cell
            savedPets.clear();
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
        });
    }

    private static void assertCard(PetListPanel.PetListViewCell cell, boolean headingVisible,
            String expectedIndex, String expectedName) {
        Label heading = getLabel(cell, "owner");
        assertEquals(headingVisible, heading.isVisible());
        assertEquals(headingVisible, heading.isManaged());
        assertEquals(expectedIndex, getLabel(cell, "id").getText());
        assertEquals(expectedName, getLabel(cell, "name").getText());
    }

    private static Label getLabel(PetListPanel.PetListViewCell cell, String id) {
        return (Label) cell.getGraphic().lookup("#" + id);
    }
    // @@author

    @SuppressWarnings("unchecked")
    private static ObservableList<Pet> panelList(PetListPanel panel) {
        try {
            var field = PetListPanel.class.getDeclaredField("petListView");
            field.setAccessible(true);
            return ((javafx.scene.control.ListView<Pet>) field.get(panel)).getItems();
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}

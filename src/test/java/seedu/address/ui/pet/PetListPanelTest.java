package seedu.address.ui.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalClients.ALICE;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.model.client.Client;
import seedu.address.model.pet.Pet;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;
import seedu.address.ui.JavaFxTestUtils;

public class PetListPanelTest {

    private static final double DEFAULT_WINDOW_WIDTH = 740;
    private static final double MINIMUM_WINDOW_WIDTH = 450;
    private static final double WIDTH_TOLERANCE = 1;
    private static final String LONG_REQUIREMENT = "long note ".repeat(24);

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
    public void listView_afterFilteringAndDeletion_recalculatesGroupHeading() {
        JavaFxTestUtils.runAndWait(() -> {
            Pet firstPet = new PetBuilder(ALICE).withName("Bubu").build();
            Pet secondPet = new PetBuilder(ALICE).withName("Mochi").build();
            ObservableList<Pet> savedPets = FXCollections.observableArrayList(firstPet, secondPet);
            FilteredList<Pet> displayedPets = new FilteredList<>(savedPets);
            PetListPanel panel = new PetListPanel(displayedPets);
            ListView<Pet> listView = panelListView(panel);
            Scene scene = new Scene(panel.getRoot(), 800, 600);
            JavaFxTestUtils.layoutScene(scene);
            assertCardAt(listView, 1, false, "2. ", "Mochi");

            // EP: changing the preceding pet should refresh the unchanged cell's group heading
            Client differentOwner = new ClientBuilder(ALICE).withPhone("81234567").build();
            Pet replacementPet = new PetBuilder(differentOwner).withName("Luna").build();
            savedPets.set(0, replacementPet);
            JavaFxTestUtils.layoutScene(scene);
            assertCardAt(listView, 1, true, "2. ", "Mochi");

            savedPets.set(0, firstPet);
            JavaFxTestUtils.layoutScene(scene);
            assertCardAt(listView, 1, false, "2. ", "Mochi");

            // EP: filtering makes an existing pet the first item in its owner group
            displayedPets.setPredicate(pet -> pet.equals(secondPet));
            JavaFxTestUtils.layoutScene(scene);
            assertCardAt(listView, 0, true, "1. ", "Mochi");

            // EP: removing the filter restores a preceding pet with the same owner
            displayedPets.setPredicate(pet -> true);
            JavaFxTestUtils.layoutScene(scene);
            assertCardAt(listView, 1, false, "2. ", "Mochi");

            // EP: deletion makes an existing pet the first item in its owner group
            savedPets.remove(firstPet);
            JavaFxTestUtils.layoutScene(scene);
            assertCardAt(listView, 0, true, "1. ", "Mochi");

            // EP: deleting all pets produces an empty cell
            savedPets.clear();
            JavaFxTestUtils.layoutScene(scene);
            assertEquals(0, listView.getItems().size());
        });
    }

    @Test
    public void longRequirement_inActualListView_wrapsWithinDefaultAndMinimumWidths() {
        JavaFxTestUtils.runAndWait(() -> {
            assertLongRequirementFitsListView(DEFAULT_WINDOW_WIDTH);
            assertLongRequirementFitsListView(MINIMUM_WINDOW_WIDTH);
        });
    }

    private static void assertLongRequirementFitsListView(double windowWidth) {
        Pet pet = new PetBuilder(ALICE).withRequirement(LONG_REQUIREMENT).build();
        PetListPanel panel = new PetListPanel(FXCollections.observableArrayList(pet));
        Region panelRoot = panel.getRoot();
        StackPane sceneRoot = new StackPane(panelRoot);
        new Scene(sceneRoot, windowWidth, 600);
        sceneRoot.applyCss();
        sceneRoot.layout();

        ListView<Pet> listView = panelListView(panel);
        ListCell<Pet> cell = listViewCell(listView);
        Region card = (Region) cell.getGraphic();
        Label requirement = (Label) card.lookup("#requirement");

        assertTrue(listView.getWidth() > 0);
        assertTrue(card.getWidth() > 0);
        assertTrue(card.getBoundsInParent().getWidth() <= listView.getWidth() + WIDTH_TOLERANCE);
        assertTrue(requirement.getHeight() > requirement.prefHeight(Double.MAX_VALUE));
    }

    private static void assertCardAt(ListView<Pet> listView, int index, boolean headingVisible,
            String expectedIndex, String expectedName) {
        PetListPanel.PetListViewCell cell = JavaFxTestUtils.getCellAt(listView, index,
                PetListPanel.PetListViewCell.class);
        assertCard(cell, headingVisible, expectedIndex, expectedName);
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
    private static ListView<Pet> panelListView(PetListPanel panel) {
        try {
            var field = PetListPanel.class.getDeclaredField("petListView");
            field.setAccessible(true);
            return (ListView<Pet>) field.get(panel);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static ListCell<Pet> listViewCell(ListView<Pet> listView) {
        return listView.getSkin().getNode().lookupAll(".list-cell").stream()
                .map(node -> (ListCell<Pet>) node)
                .filter(cell -> !cell.isEmpty())
                .findFirst()
                .orElseThrow(() -> new AssertionError("The pet ListView did not create a populated cell."));
    }

    private static ObservableList<Pet> panelList(PetListPanel panel) {
        return panelListView(panel).getItems();
    }
}

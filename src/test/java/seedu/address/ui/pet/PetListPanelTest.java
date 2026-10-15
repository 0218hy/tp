package seedu.address.ui.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.TypicalClients.ALICE;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.pet.Pet;
import seedu.address.testutil.PetBuilder;
import seedu.address.ui.JavaFxTestUtils;

@EnabledOnOs(OS.LINUX)
public class PetListPanelTest {

    @Test
    public void constructorAndCell_updateGraphicForEmptyAndPopulatedItems() {
        JavaFxTestUtils.runAndWait(() -> {
            Pet pet = new PetBuilder(ALICE).build();
            ObservableList<Pet> pets = FXCollections.observableArrayList(pet);
            PetListPanel panel = new PetListPanel(pets);
            PetListPanel.PetListViewCell cell = panel.new PetListViewCell();

            assertEquals(pets, panelList(panel));
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());

            cell.updateItem(pet, false);
            assertNotNull(cell.getGraphic());

            cell.updateItem(null, false);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());
        });
    }

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

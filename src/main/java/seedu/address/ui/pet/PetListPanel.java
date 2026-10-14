package seedu.address.ui.pet;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.pet.Pet;
import seedu.address.ui.UiPart;

/** Panel containing the list of pets. */
public class PetListPanel extends UiPart<Region> {
    private static final String FXML = "PetListPanel.fxml";

    @FXML
    private ListView<Pet> petListView;

    /** Creates a {@code PetListPanel} with the given observable pet list. */
    public PetListPanel(ObservableList<Pet> petList) {
        super(FXML);
        petListView.setItems(petList);
        petListView.setCellFactory(listView -> new PetListViewCell());
    }

    /** Custom cell that displays one pet using a {@link PetCard}. */
    class PetListViewCell extends ListCell<Pet> {
        @Override
        protected void updateItem(Pet pet, boolean empty) {
            super.updateItem(pet, empty);

            if (empty || pet == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PetCard(pet, getIndex() + 1).getRoot());
            }
        }
    }
}

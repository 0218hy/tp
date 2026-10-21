package seedu.address.ui.pet;

import javafx.collections.ListChangeListener;
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
        // A deletion or filter change can make an existing pet the first in its owner group.
        petList.addListener((ListChangeListener<Pet>) change -> petListView.refresh());
    }

    /** Custom cell that displays one pet using a {@link PetCard}. */
    class PetListViewCell extends ListCell<Pet> {
        PetListViewCell() {
            indexProperty().addListener((observable, oldIndex, newIndex) -> updateItem(getItem(), isEmpty()));
        }

        @Override
        protected void updateItem(Pet pet, boolean empty) {
            super.updateItem(pet, empty);

            if (empty || pet == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            boolean showOwnerHeading = startsOwnerGroup(pet, getIndex());
            PetCard card = new PetCard(pet, getIndex() + 1, showOwnerHeading);
            setGraphic(card.getRoot());
        }

        /** Uses phone numbers to distinguish owners, including those with the same name. */
        private boolean startsOwnerGroup(Pet pet, int index) {
            ObservableList<Pet> pets = petListView.getItems();
            // Virtualised cells can temporarily have an index outside the current list.
            if (index <= 0 || index >= pets.size()) {
                return true;
            }
            Pet previousPet = pets.get(index - 1);
            var currentOwner = pet.getOwner();
            var previousOwner = previousPet.getOwner();
            return !currentOwner.getPhone().equals(previousOwner.getPhone());
        }
    }
}

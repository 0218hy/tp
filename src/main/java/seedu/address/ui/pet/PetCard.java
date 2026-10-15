package seedu.address.ui.pet;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.pet.Pet;
import seedu.address.ui.UiPart;

/** A UI component that displays information of a {@code Pet}. */
public class PetCard extends UiPart<Region> {
    private static final String FXML = "PetCard.fxml";

    public final Pet pet;

    @FXML
    private HBox cardPane;
    @FXML
    private Label id;
    @FXML
    private Label name;
    @FXML
    private Label owner;
    @FXML
    private Label species;
    @FXML
    private Label breed;
    @FXML
    private Label requirement;

    /** Creates a {@code PetCard} with the given pet and displayed index. */
    public PetCard(Pet pet, int displayedIndex) {
        super(FXML);
        this.pet = pet;
        id.setText(displayedIndex + ". ");
        name.setText(pet.getName().toString());
        owner.setText("Owner: " + pet.getOwner().getName().fullName
                + " (" + pet.getOwner().getPhone().value + ")");
        species.setText("Species: " + pet.getSpecies().getDisplayName());
        breed.setText("Breed: " + (pet.getBreed() == null ? "Not recorded" : pet.getBreed()));
        requirement.setText("Requirement: " + pet.getRequirement());
    }
}

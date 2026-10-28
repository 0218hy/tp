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
    private static final String INDEX_FORMAT = "%d. ";
    private static final String OWNER_FORMAT = "Owner: %s (%s)";
    private static final String SPECIES_FORMAT = "Species: %s";
    private static final String BREED_FORMAT = "Breed: %s";
    private static final String REQUIREMENT_FORMAT = "Requirement: %s";
    private static final String UNRECORDED_BREED = "Not recorded";

    final Pet pet;

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

    // @@author xinyu-zxy
    /** Creates a {@code PetCard} with the given pet and displayed index. */
    public PetCard(Pet pet, int displayedIndex) {
        this(pet, displayedIndex, true);
    }

    /**
     * Creates a pet card with an optional owner heading.
     *
     * @param pet Pet to display.
     * @param displayedIndex Index to display; callers are expected to supply a one-based value.
     * @param showOwnerHeading Whether to show the heading; the panel determines owner-group boundaries.
     */
    public PetCard(Pet pet, int displayedIndex, boolean showOwnerHeading) {
        super(FXML);
        this.pet = pet;
        id.setText(String.format(INDEX_FORMAT, displayedIndex));
        name.setText(pet.getName().toString());
        owner.setText(String.format(OWNER_FORMAT, pet.getOwner().getName(), pet.getOwner().getPhone()));
        owner.setVisible(showOwnerHeading);
        owner.setManaged(showOwnerHeading);
        species.setText(String.format(SPECIES_FORMAT, pet.getSpecies().getDisplayName()));
        breed.setText(String.format(BREED_FORMAT, pet.getBreed() == null ? UNRECORDED_BREED : pet.getBreed()));
        requirement.setText(String.format(REQUIREMENT_FORMAT, pet.getRequirement()));
    }
    // @@author
}

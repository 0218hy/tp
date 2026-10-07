package seedu.address.storage;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Person;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;

/**
 * Jackson-friendly version of {@link Pet}.
 */
class JsonAdaptedPet {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Pet's %s field is missing!";
    public static final String MESSAGE_OWNER_NOT_FOUND = "Pet owner does not exist in the address book.";

    private final String name;
    private final String ownerName;
    private final String species;
    private final String requirement;

    /**
     * Constructs a {@code JsonAdaptedPet} with the given pet details.
     */
    @JsonCreator
    public JsonAdaptedPet(@JsonProperty("name") String name, @JsonProperty("ownerName") String ownerName,
            @JsonProperty("species") String species, @JsonProperty("requirement") String requirement) {
        this.name = name;
        this.ownerName = ownerName;
        this.species = species;
        this.requirement = requirement;
    }

    /**
     * Converts a given {@code Pet} into this class for Jackson use.
     */
    public JsonAdaptedPet(Pet source) {
        name = source.getName().value;
        ownerName = source.getOwner().getName().fullName;
        species = source.getSpecies().name();
        requirement = source.getRequirement().value;
    }

    /**
     * Converts this Jackson-friendly adapted pet object into the model's {@code Pet} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted pet.
     */
    public Pet toModelType(List<Person> persons) throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, PetName.class.getSimpleName()));
        }
        if (!PetName.isValidName(name)) {
            throw new IllegalValueException(PetName.MESSAGE_CONSTRAINTS);
        }
        PetName modelName = new PetName(name);

        if (ownerName == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "OwnerName"));
        }
        Person modelOwner = persons.stream()
                .filter(person -> person.getName().fullName.equals(ownerName))
                .findFirst()
                .orElseThrow(() -> new IllegalValueException(MESSAGE_OWNER_NOT_FOUND));

        if (species == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Species.class.getSimpleName()));
        }
        Species modelSpecies;
        try {
            modelSpecies = Species.valueOf(species);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException("Pet species is invalid.");
        }

        if (requirement == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, Requirement.class.getSimpleName()));
        }
        if (!Requirement.isValidRequirement(requirement)) {
            throw new IllegalValueException(Requirement.MESSAGE_CONSTRAINTS);
        }
        Requirement modelRequirement = new Requirement(requirement);

        return new Pet(modelName, modelOwner, modelSpecies, modelRequirement);
    }
}

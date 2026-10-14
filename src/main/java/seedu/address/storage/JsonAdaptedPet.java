package seedu.address.storage;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Person;
import seedu.address.model.pet.Breed;
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
    public static final String MESSAGE_AMBIGUOUS_OWNER = "Pet owner name matches multiple clients.";
    public static final String MESSAGE_OWNER_DETAILS_MISMATCH = "Pet owner name and phone do not match.";
    private static final String OWNER_NAME_FIELD = "OwnerName";
    private static final String MESSAGE_INVALID_SPECIES = "Pet species is invalid.";

    private final String name;
    private final String ownerName;
    private final String ownerPhone;
    private final String species;
    private final String breed;
    private final String requirement;

    /**
     * Constructs a stored pet record from its JSON fields.
     * Owner phone and breed may be absent in older records.
     */
    @JsonCreator
    public JsonAdaptedPet(@JsonProperty("name") String name, @JsonProperty("ownerName") String ownerName,
            @JsonProperty("ownerPhone") String ownerPhone, @JsonProperty("species") String species,
            @JsonProperty("breed") String breed, @JsonProperty("requirement") String requirement) {
        this.name = name;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.species = species;
        this.breed = breed;
        this.requirement = requirement;
    }

    /**
     * Creates a legacy pet record identified by the owner's name.
     * The breed and owner phone were not recorded in this format.
     */
    public JsonAdaptedPet(String name, String ownerName, String species, String requirement) {
        this(name, ownerName, null, species, null, requirement);
    }

    /**
     * Converts a given {@code Pet} into this class for Jackson use.
     */
    public JsonAdaptedPet(Pet source) {
        name = source.getName().value;
        ownerName = source.getOwner().getName().fullName;
        ownerPhone = source.getOwner().getPhone().value;
        species = source.getSpecies().name();
        breed = source.getBreed() == null ? null : source.getBreed().value;
        requirement = source.getRequirement().value;
    }

    /**
     * Converts the stored fields into a {@code Pet} linked to a registered owner.
     * Uses the owner's phone when present, or the legacy owner name otherwise.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted pet.
     */
    public Pet toModelType(List<Person> persons) throws IllegalValueException {
        PetName modelName = toPetName();
        Person modelOwner = findOwner(persons);
        Species modelSpecies = toSpecies();
        Breed modelBreed = toBreed();
        Requirement modelRequirement = toRequirement();
        return new Pet(modelName, modelOwner, modelSpecies, modelBreed, modelRequirement);
    }

    /**
     * Converts the stored name after checking its required field and format.
     */
    private PetName toPetName() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, PetName.class.getSimpleName()));
        }
        if (!PetName.isValidName(name)) {
            throw new IllegalValueException(PetName.MESSAGE_CONSTRAINTS);
        }
        return new PetName(name);
    }

    /**
     * Finds the owner by phone, falling back to the name in legacy records.
     * Rejects ambiguous legacy names and names that conflict with a stored phone.
     */
    private Person findOwner(List<Person> persons) throws IllegalValueException {
        if (ownerName == null && ownerPhone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, OWNER_NAME_FIELD));
        }
        Person owner = ownerPhone == null ? findLegacyOwnerByName(persons) : findOwnerByPhone(persons);
        if (ownerName != null && !owner.getName().fullName.equals(ownerName)) {
            throw new IllegalValueException(MESSAGE_OWNER_DETAILS_MISMATCH);
        }
        return owner;
    }

    /**
     * Finds the owner identified by the stored phone number.
     */
    private Person findOwnerByPhone(List<Person> persons) throws IllegalValueException {
        return persons.stream()
                .filter(person -> person.getPhone().value.equals(ownerPhone))
                .findFirst()
                .orElseThrow(() -> new IllegalValueException(MESSAGE_OWNER_NOT_FOUND));
    }

    /**
     * Finds the sole owner with the stored name in a legacy pet record.
     */
    private Person findLegacyOwnerByName(List<Person> persons) throws IllegalValueException {
        Iterator<Person> matchingOwners = persons.stream()
                .filter(person -> person.getName().fullName.equals(ownerName))
                .iterator();
        if (!matchingOwners.hasNext()) {
            throw new IllegalValueException(MESSAGE_OWNER_NOT_FOUND);
        }
        Person owner = matchingOwners.next();
        if (matchingOwners.hasNext()) {
            throw new IllegalValueException(MESSAGE_AMBIGUOUS_OWNER);
        }
        return owner;
    }

    /**
     * Converts the stored species to a supported model value.
     */
    private Species toSpecies() throws IllegalValueException {
        if (species == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Species.class.getSimpleName()));
        }
        try {
            return Species.valueOf(species);
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(MESSAGE_INVALID_SPECIES);
        }
    }

    /**
     * Converts a recorded breed, or returns {@code null} when no breed was stored.
     */
    private Breed toBreed() throws IllegalValueException {
        if (breed != null && !Breed.isValidBreed(breed)) {
            throw new IllegalValueException(Breed.MESSAGE_CONSTRAINTS);
        }
        return breed == null ? null : new Breed(breed);
    }

    /**
     * Converts the stored care requirements after validating them.
     */
    private Requirement toRequirement() throws IllegalValueException {
        if (requirement == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, Requirement.class.getSimpleName()));
        }
        if (!Requirement.isValidRequirement(requirement)) {
            throw new IllegalValueException(Requirement.MESSAGE_CONSTRAINTS);
        }
        return new Requirement(requirement);
    }
}

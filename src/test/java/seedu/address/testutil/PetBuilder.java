package seedu.address.testutil;

import seedu.address.model.person.Person;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;

/**
 * A utility class to help with building Pet objects.
 */
public class PetBuilder {

    public static final String DEFAULT_NAME = "Milo";
    public static final String DEFAULT_REQUIREMENT = "Daily walk";

    private PetName name;
    private Person owner;
    private Species species;
    private Requirement requirement;

    /**
     * Creates a {@code PetBuilder} with default details.
     */
    public PetBuilder(Person owner) {
        name = new PetName(DEFAULT_NAME);
        this.owner = owner;
        species = Species.DOG;
        requirement = new Requirement(DEFAULT_REQUIREMENT);
    }

    /**
     * Initializes the PetBuilder with the data of {@code petToCopy}.
     */
    public PetBuilder(Pet petToCopy) {
        name = petToCopy.getName();
        owner = petToCopy.getOwner();
        species = petToCopy.getSpecies();
        requirement = petToCopy.getRequirement();
    }

    /**
     * Sets the name of the pet being built.
     */
    public PetBuilder withName(String name) {
        this.name = new PetName(name);
        return this;
    }

    /**
     * Sets the owner of the pet being built.
     */
    public PetBuilder withOwner(Person owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Sets the species of the pet being built.
     */
    public PetBuilder withSpecies(Species species) {
        this.species = species;
        return this;
    }

    /**
     * Sets the care requirement of the pet being built.
     */
    public PetBuilder withRequirement(String requirement) {
        this.requirement = new Requirement(requirement);
        return this;
    }

    public Pet build() {
        return new Pet(name, owner, species, requirement);
    }
}

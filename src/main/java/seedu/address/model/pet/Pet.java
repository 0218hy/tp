package seedu.address.model.pet;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;

/**
 * Represents a pet in the address book.
 * Guarantees: details are present and not null; immutable.
 */
public class Pet {

    private final PetName name;
    private final Person owner;
    private final Species species;
    private final Requirement requirement;

    /**
     * Every field must be present and not null.
     */
    public Pet(PetName name, Person owner, Species species, Requirement requirement) {
        requireAllNonNull(name, owner, species, requirement);
        this.name = name;
        this.owner = owner;
        this.species = species;
        this.requirement = requirement;
    }

    public PetName getName() {
        return name;
    }

    public Person getOwner() {
        return owner;
    }

    public Species getSpecies() {
        return species;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    /**
     * Returns a copy of this pet with the given owner.
     */
    public Pet withOwner(Person newOwner) {
        return new Pet(name, newOwner, species, requirement);
    }

    /**
     * Returns true if both pets have the same owner and name.
     */
    public boolean isSamePet(Pet otherPet) {
        if (otherPet == this) {
            return true;
        }

        return otherPet != null
                && owner.isSamePerson(otherPet.owner)
                && name.equals(otherPet.name);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Pet otherPet)) {
            return false;
        }

        return name.equals(otherPet.name)
                && owner.equals(otherPet.owner)
                && species.equals(otherPet.species)
                && requirement.equals(otherPet.requirement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner, species, requirement);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("owner", owner)
                .add("species", species)
                .add("requirement", requirement)
                .toString();
    }
}

package seedu.address.model.pet;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;

/**
 * Represents a pet in the address book.
 * Its required details are non-null, while breed may be absent.
 * The profile is immutable.
 */
public class Pet {

    private final PetName name;
    private final Person owner;
    private final Species species;
    private final Breed breed;
    private final Requirement requirement;

    /**
     * Creates a pet without a recorded breed.
     * All supplied fields must be non-null.
     */
    public Pet(PetName name, Person owner, Species species, Requirement requirement) {
        this(name, owner, species, null, requirement);
    }

    /**
     * Creates a pet with an optional breed.
     * All other fields must be non-null.
     */
    public Pet(PetName name, Person owner, Species species, Breed breed, Requirement requirement) {
        requireAllNonNull(name, owner, species, requirement);
        this.name = name;
        this.owner = owner;
        this.species = species;
        this.breed = breed;
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

    /**
     * Returns the breed, or {@code null} when it was not supplied.
     */
    public Breed getBreed() {
        return breed;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    /**
     * Returns a copy of this pet with the given owner.
     * The pet's name, species, breed, and care requirements are preserved.
     */
    public Pet withOwner(Person newOwner) {
        return new Pet(name, newOwner, species, breed, requirement);
    }

    /**
     * Returns whether both pets belong to the same owner and have the same normalized name.
     * Case and repeated whitespace in the names do not affect identity.
     */
    public boolean isSamePet(Pet otherPet) {
        if (otherPet == this) {
            return true;
        }

        return otherPet != null
                && owner.getPhone().equals(otherPet.owner.getPhone())
                && name.getNormalizedName().equals(otherPet.name.getNormalizedName());
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
                && Objects.equals(breed, otherPet.breed)
                && requirement.equals(otherPet.requirement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner, species, breed, requirement);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("owner", owner)
                .add("species", species)
                .add("breed", breed)
                .add("requirement", requirement)
                .toString();
    }
}

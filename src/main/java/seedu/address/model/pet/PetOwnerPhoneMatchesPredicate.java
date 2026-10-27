package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.model.client.Phone;

// @@author xinyu-zxy
/**
 * Tests whether a pet belongs to the client with the specified phone number.
 */
public class PetOwnerPhoneMatchesPredicate implements Predicate<Pet> {
    private final Phone ownerPhone;

    /**
     * Creates a predicate for pets belonging to {@code ownerPhone}.
     *
     * @param ownerPhone A valid owner phone number.
     */
    public PetOwnerPhoneMatchesPredicate(Phone ownerPhone) {
        this.ownerPhone = requireNonNull(ownerPhone);
    }

    public Phone getOwnerPhone() {
        return ownerPhone;
    }

    /**
     * Returns whether {@code pet} belongs to the configured owner.
     *
     * @param pet The pet to test.
     */
    @Override
    public boolean test(Pet pet) {
        requireNonNull(pet);
        return pet.isOwnedBy(ownerPhone);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PetOwnerPhoneMatchesPredicate otherPredicate
                && ownerPhone.equals(otherPredicate.ownerPhone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerPhone);
    }
}

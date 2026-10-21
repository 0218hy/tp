package seedu.address.model.pet;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.model.client.Phone;

/**
 * Tests whether a pet belongs to the client with the specified phone number.
 */
public class PetOwnerPhoneMatchesPredicate implements Predicate<Pet> {
    private final Phone ownerPhone;

    public PetOwnerPhoneMatchesPredicate(Phone ownerPhone) {
        this.ownerPhone = requireNonNull(ownerPhone);
    }

    public Phone getOwnerPhone() {
        return ownerPhone;
    }

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

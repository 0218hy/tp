package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PetBuilder;

// @@author xinyu-zxy
/**
 * Tests filtering pets by their owner's phone number.
 */
public class PetOwnerPhoneMatchesPredicateTest {

    @Test
    public void test_matchingOwnerPhone_returnsTrue() {
        PetOwnerPhoneMatchesPredicate predicate = new PetOwnerPhoneMatchesPredicate(ALICE.getPhone());

        // EP: pet owner phone matches the target phone
        assertTrue(predicate.test(new PetBuilder(ALICE).build()));

        // EP: pet owner phone differs from the target phone
        assertFalse(predicate.test(new PetBuilder(BENSON).build()));
    }

    @Test
    public void equals_sameOwnerPhone_equalPredicates() {
        // EP: predicates containing the same owner phone
        PetOwnerPhoneMatchesPredicate first = new PetOwnerPhoneMatchesPredicate(ALICE.getPhone());
        PetOwnerPhoneMatchesPredicate second = new PetOwnerPhoneMatchesPredicate(ALICE.getPhone());

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void constructor_nullPhone_throwsNullPointerException() {
        // EP: null owner phone
        assertThrows(NullPointerException.class, () -> new PetOwnerPhoneMatchesPredicate(null));
    }
}

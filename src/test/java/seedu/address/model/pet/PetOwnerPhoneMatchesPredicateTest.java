package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PetBuilder;

/**
 * Tests filtering pets by their owner's phone number.
 */
public class PetOwnerPhoneMatchesPredicateTest {

    @Test
    public void test_matchingOwnerPhone_returnsTrue() {
        PetOwnerPhoneMatchesPredicate predicate = new PetOwnerPhoneMatchesPredicate(ALICE.getPhone());

        assertTrue(predicate.test(new PetBuilder(ALICE).build()));
        assertFalse(predicate.test(new PetBuilder(BENSON).build()));
    }

    @Test
    public void equals_sameOwnerPhone_equalPredicates() {
        PetOwnerPhoneMatchesPredicate first = new PetOwnerPhoneMatchesPredicate(ALICE.getPhone());
        PetOwnerPhoneMatchesPredicate second = new PetOwnerPhoneMatchesPredicate(ALICE.getPhone());

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void constructor_nullPhone_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PetOwnerPhoneMatchesPredicate(null));
    }
}

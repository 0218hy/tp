package seedu.address.model.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AddressTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Address(null));
    }

    @Test
    public void constructor_invalidAddress_throwsIllegalArgumentException() {
        String invalidAddress = "";
        assertThrows(IllegalArgumentException.class, () -> new Address(invalidAddress));
    }

    //@@author anderschow
    @Test
    public void isValidAddress() {
        // null address
        assertThrows(NullPointerException.class, () -> Address.isValidAddress(null));

        // invalid addresses
        assertFalse(Address.isValidAddress("")); // empty string
        assertFalse(Address.isValidAddress(" ")); // spaces only

        assertFalse(Address.isValidAddress("A".repeat(4)));
        assertTrue(Address.isValidAddress("A".repeat(5)));
        assertTrue(Address.isValidAddress("A".repeat(120)));
        assertFalse(Address.isValidAddress("A".repeat(121)));
        assertFalse(Address.isValidAddress("     "));
        assertFalse(Address.isValidAddress("12 Road / 3"));
        assertFalse(Address.isValidAddress("12 Road @ 3"));
        assertFalse(Address.isValidAddress("12 Road\n3"));
        assertFalse(Address.isValidAddress("12 St. John\u2019s Road")); // curly apostrophe is unsupported

        // valid addresses
        assertTrue(Address.isValidAddress("Blk 456, Den Road, #01-355"));
        assertTrue(Address.isValidAddress("12 St. John's Road"));
        assertFalse(Address.isValidAddress("-")); // one character
        assertFalse(Address.isValidAddress("Leng Inc; 1234 Market St; San Francisco CA 2349879; USA")); // long address
    }
    //@@author

    @Test
    public void equals() {
        Address address = new Address("Valid Address");

        // same values -> returns true
        assertTrue(address.equals(new Address("Valid Address")));

        // same object -> returns true
        assertTrue(address.equals(address));

        // null -> returns false
        assertFalse(address.equals(null));

        // different types -> returns false
        assertFalse(address.equals(5.0f));

        // different values -> returns false
        assertFalse(address.equals(new Address("Other Valid Address")));
    }
}

package seedu.address.model.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ClientNameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ClientName(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new ClientName(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> ClientName.isValidName(null));

        // invalid name
        assertFalse(ClientName.isValidName("")); // empty string
        assertFalse(ClientName.isValidName(" ")); // spaces only
        assertFalse(ClientName.isValidName("^")); // only non-alphanumeric characters
        assertFalse(ClientName.isValidName("peter*")); // contains non-alphanumeric characters

        assertFalse(ClientName.isValidName("A"));
        assertFalse(ClientName.isValidName("A".repeat(61)));
        assertFalse(ClientName.isValidName("  "));
        assertTrue(ClientName.isValidName("Al"));
        assertTrue(ClientName.isValidName("A".repeat(60)));
        assertTrue(ClientName.isValidName("Mary-Jane O'Connor Jr."));

        // valid name
        assertTrue(ClientName.isValidName("peter jack")); // alphabets only
        assertFalse(ClientName.isValidName("12345")); // numbers only
        assertFalse(ClientName.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(ClientName.isValidName("Capital Tan")); // with capital letters
        assertTrue(ClientName.isValidName("David Roger Jackson Ray Jr")); // long names
    }

    @Test
    public void equals() {
        ClientName name = new ClientName("Valid ClientName");

        // same values -> returns true
        assertTrue(name.equals(new ClientName("Valid ClientName")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new ClientName("Other Valid ClientName")));
    }
}

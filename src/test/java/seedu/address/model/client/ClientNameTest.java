package seedu.address.model.client;

//@@author anderschow
import static org.junit.jupiter.api.Assertions.assertEquals;
//@@author
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ClientNameTest {

    //@@author anderschow
    @Test
    public void constructor_unicodeNames_normalizesToNfc() {
        ClientName composed = new ClientName("Jos\u00e9 Tan");
        ClientName decomposed = new ClientName("Jose\u0301 Tan");
        assertEquals("Jos\u00e9 Tan", decomposed.fullName);
        assertEquals(composed, decomposed);
        assertEquals(composed.hashCode(), decomposed.hashCode());
        assertTrue(ClientName.isValidName("李小明"));
        assertTrue(ClientName.isValidName("अनिता"));
        assertTrue(ClientName.isValidName("Лев Иванов"));
    }

    @Test
    public void isValidName_unicodeLength_countsNormalizedCodePoints() {
        String supplementaryLetter = "\uD840\uDC00";
        for (String unit : new String[]{"A", "e\u0301", supplementaryLetter}) {
            assertFalse(ClientName.isValidName(unit));
            assertTrue(ClientName.isValidName(unit.repeat(2)));
            assertTrue(ClientName.isValidName(unit.repeat(60)));
            assertFalse(ClientName.isValidName(unit.repeat(61)));
        }
        // This combining mark does not compose with q and counts separately.
        assertTrue(ClientName.isValidName("q\u0301".repeat(30)));
        assertFalse(ClientName.isValidName("q\u0301".repeat(30) + "a"));
    }

    @Test
    public void isValidName_disallowedUnicodeCharacters_returnsFalse() {
        for (String name : new String[]{"李1", "José@", "李😀", "李\t明", "李\n明", "李\u200B明", "\uD840"}) {
            assertFalse(ClientName.isValidName(name));
        }
    }
    //@@author

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
        ClientName name = new ClientName("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new ClientName("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new ClientName("Other Valid Name")));
    }
}

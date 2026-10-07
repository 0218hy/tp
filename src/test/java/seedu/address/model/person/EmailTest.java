package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class EmailTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    public void constructor_invalidEmail_throwsIllegalArgumentException() {
        String invalidEmail = "";
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    public void isValidEmail() {
        // null email
        assertThrows(NullPointerException.class, () -> Email.isValidEmail(null));

        // blank email
        assertFalse(Email.isValidEmail("")); // empty string
        assertFalse(Email.isValidEmail(" ")); // spaces only

        // missing parts
        assertFalse(Email.isValidEmail("@example.com")); // missing local part
        assertFalse(Email.isValidEmail("peterjackexample.com")); // missing '@' symbol
        assertFalse(Email.isValidEmail("peterjack@")); // missing domain name

        // Whitespace, repeated @ signs and missing domain sections are invalid.
        assertFalse(Email.isValidEmail("peter jack@example.com"));
        assertFalse(Email.isValidEmail("peterjack@exam ple.com"));
        assertFalse(Email.isValidEmail(" peterjack@example.com"));
        assertFalse(Email.isValidEmail("peterjack@example.com "));
        assertFalse(Email.isValidEmail("peter\tjack@example.com"));
        assertFalse(Email.isValidEmail("peterjack@@example.com"));
        assertFalse(Email.isValidEmail("peter@jack@example.com"));
        assertFalse(Email.isValidEmail("peterjack@.example.com"));
        assertFalse(Email.isValidEmail("peterjack@example.com."));
        assertFalse(Email.isValidEmail("peterjack@example..com"));
        assertFalse(Email.isValidEmail("a@bc"));
        assertFalse(Email.isValidEmail("test@localhost"));
        assertFalse(Email.isValidEmail("123@145"));

        // The client specification does not impose extra local-part or domain-label restrictions.
        assertTrue(Email.isValidEmail("amelia@example.com"));
        assertTrue(Email.isValidEmail("PeterJack_1190@example.com"));
        assertTrue(Email.isValidEmail("PeterJack+1190@example.com"));
        assertTrue(Email.isValidEmail("-peterjack@example.com"));
        assertTrue(Email.isValidEmail("peter..jack@example.com"));
        assertTrue(Email.isValidEmail("peterjack@exam_ple.com"));
        assertTrue(Email.isValidEmail("peterjack@example.c"));
        assertTrue(Email.isValidEmail("e1234567@u.nus.edu"));

    }

    @Test
    public void equals() {
        Email email = new Email("valid@email.com");

        // same values -> returns true
        assertTrue(email.equals(new Email("valid@email.com")));

        // same object -> returns true
        assertTrue(email.equals(email));

        // null -> returns false
        assertFalse(email.equals(null));

        // different types -> returns false
        assertFalse(email.equals(5.0f));

        // different values -> returns false
        assertFalse(email.equals(new Email("other.valid@email.com")));
    }
}

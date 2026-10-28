package seedu.address.model.client;

//@@author anderschow
import static org.junit.jupiter.api.Assertions.assertEquals;
//@@author
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class EmailTest {

    //@@author anderschow
    @Test
    public void equals_domainCaseIgnored_localPartCasePreserved() {
        Email lower = new Email("alice@example.com");
        Email mixed = new Email("alice@Example.COM");
        assertEquals(lower, mixed);
        assertEquals(mixed, lower);
        assertEquals(lower.hashCode(), mixed.hashCode());
        assertEquals("alice@Example.COM", mixed.value);
        assertEquals("alice@Example.COM", mixed.toString());
        assertFalse(lower.equals(new Email("Alice@EXAMPLE.COM")));
    }
    //@@author

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    public void constructor_invalidEmail_throwsIllegalArgumentException() {
        String invalidEmail = "";
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    //@@author anderschow
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

    //@@author anderschow
    @Test
    public void isValidEmail_lengthBoundaryAndOversizedDomain() {
        String maximumEmail = "a".repeat(Email.MAX_LENGTH - "@example.com".length()) + "@example.com";
        assertTrue(Email.isValidEmail(maximumEmail.substring(1)));
        assertTrue(Email.isValidEmail(maximumEmail));
        String manySections = "a@" + "b.".repeat(124) + "com";
        assertTrue(Email.isValidEmail(manySections));
        assertTrue(Email.isValidEmail("a" + manySections));
        for (String email : new String[]{"a" + maximumEmail, "aa" + manySections,
            "a@" + "b.".repeat(6000) + "com"}) {
            assertFalse(Email.isValidEmail(email));
            assertThrows(IllegalArgumentException.class, () -> new Email(email));
        }
    }

    @Test
    public void isValidEmail_unicodeWhitespace_rejectedInEveryPart() {
        for (String space : new String[]{"\u00a0", "\u2003", "\u202f", "\u3000", "\u0085", "\u2028"}) {
            for (String email : new String[]{"alice" + space + "tan@example.com",
                "alice@exam" + space + "ple.com", "alice@example.c" + space + "om",
                space + "alice@example.com", "alice@example.com" + space}) {
                assertFalse(Email.isValidEmail(email));
                assertThrows(IllegalArgumentException.class, () -> new Email(email));
            }
        }
    }
    //@@author

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
    //@@author
}

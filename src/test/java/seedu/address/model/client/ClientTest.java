package seedu.address.model.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.ClientBuilder;

public class ClientTest {

    // @@author xinyu-zxy
    @Test
    public void hasPhone_matchesExactStoredNumber() {
        String phone = ALICE.getPhone().value;

        // EP: exact phone-number match
        assertTrue(ALICE.hasPhone(phone));

        // EP: non-matching phone-number text
        assertFalse(ALICE.hasPhone(" " + phone));
        assertFalse(ALICE.hasPhone(""));

        // EP: null phone-number input
        assertFalse(ALICE.hasPhone(null));
    }
    // @@author

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Client client = new ClientBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> client.getTags().remove(0));
    }

    //@@author anderschow
    @Test
    public void isSameClient() {
        // same object -> returns true
        assertTrue(ALICE.isSameClient(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSameClient(null));

        // Shared names and addresses alone do not make clients duplicates.
        Client differentContact = new ClientBuilder(ALICE).withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.isSameClient(differentContact));

        Client samePhone = new ClientBuilder(ALICE).withName(VALID_NAME_BOB)
                .withEmail(VALID_EMAIL_BOB).build();
        assertTrue(ALICE.isSameClient(samePhone));

        Client sameEmail = new ClientBuilder(ALICE).withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).build();
        assertTrue(ALICE.isSameClient(sameEmail));

    }
    //@@author

    @Test
    public void equals() {
        // same values -> returns true
        Client aliceCopy = new ClientBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different client -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Client editedAlice = new ClientBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new ClientBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new ClientBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new ClientBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void equals_emailDomainCase_distinguishesDataButNotIdentity() {
        Client lower = new ClientBuilder(ALICE).withEmail("alice@example.com").build();
        Client upper = new ClientBuilder(lower).withEmail("alice@EXAMPLE.COM").build();
        assertFalse(lower.equals(upper));
        assertTrue(lower.isSameClient(new ClientBuilder(upper).withPhone(VALID_PHONE_BOB).build()));
        Client copy = new ClientBuilder(upper).build();
        assertEquals(upper, copy);
        assertEquals(upper.hashCode(), copy.hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = Client.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress() + ", tags=" + ALICE.getTags() + "}";
        assertEquals(expected, ALICE.toString());
    }
}

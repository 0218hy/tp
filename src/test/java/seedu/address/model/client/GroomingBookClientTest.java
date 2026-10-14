package seedu.address.model.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.getTypicalGroomingBook;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.GroomingBook;
import seedu.address.model.client.exceptions.DuplicateClientException;
import seedu.address.testutil.ClientBuilder;

public class GroomingBookClientTest {

    private final GroomingBook clientBook = new GroomingBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), clientBook.getClientList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> clientBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyGroomingBook_replacesData() {
        GroomingBook newData = getTypicalGroomingBook();
        clientBook.resetData(newData);
        assertEquals(newData, clientBook);
    }

    @Test
    public void resetData_withDuplicateClients_throwsDuplicateClientException() {
        // Two clients with the same identity fields
        Client editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Client> newClients = List.of(ALICE, editedAlice);
        ClientBookStub newData = new ClientBookStub(newClients);

        assertThrows(DuplicateClientException.class, () -> clientBook.resetData(newData));
    }

    @Test
    public void hasClient_nullClient_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> clientBook.hasClient(null));
    }

    @Test
    public void hasClient_clientNotInGroomingBook_returnsFalse() {
        assertFalse(clientBook.hasClient(ALICE));
    }

    @Test
    public void hasClient_clientInGroomingBook_returnsTrue() {
        clientBook.addClient(ALICE);
        assertTrue(clientBook.hasClient(ALICE));
    }

    @Test
    public void hasClient_clientWithSameIdentityFieldsInGroomingBook_returnsTrue() {
        clientBook.addClient(ALICE);
        Client editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(clientBook.hasClient(editedAlice));
    }




    @Test
    public void getClientList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> clientBook.getClientList().remove(0));
    }


    @Test
    public void toStringMethod() {
        String expected = GroomingBook.class.getCanonicalName() + "{clients=" + clientBook.getClientList()
                + ", pets=[], appointments=[]}";
        assertEquals(expected, clientBook.toString());
    }

    /**
     * A stub GroomingBook whose clients list can violate interface constraints.
     */
    private static class ClientBookStub extends GroomingBook {
        private final ObservableList<Client> clients = FXCollections.observableArrayList();

        ClientBookStub(Collection<Client> clients) {
            this.clients.setAll(clients);
        }

        @Override
        public ObservableList<Client> getClientList() {
            return clients;
        }

    }

}

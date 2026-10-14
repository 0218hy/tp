package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import seedu.address.model.client.Client;
import seedu.address.model.client.exceptions.ClientNotFoundException;
import seedu.address.model.client.exceptions.DuplicateClientException;
import seedu.address.model.pet.Pet;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

public class GroomingBookTest {

    private final GroomingBook groomingBook = new GroomingBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), groomingBook.getClientList());
        assertEquals(List.of(), groomingBook.getPetList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> groomingBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyGroomingBook_replacesData() {
        GroomingBook newData = getTypicalGroomingBook();
        groomingBook.resetData(newData);
        assertEquals(newData, groomingBook);
    }

    @Test
    public void resetData_withDuplicateClients_throwsDuplicateClientException() {
        // Two clients with the same identity fields
        Client editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Client> newClients = List.of(ALICE, editedAlice);
        GroomingBookStub newData = new GroomingBookStub(newClients);

        assertThrows(DuplicateClientException.class, () -> groomingBook.resetData(newData));
    }

    @Test
    public void hasClient_nullClient_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> groomingBook.hasClient(null));
    }

    @Test
    public void hasClient_clientNotInGroomingBook_returnsFalse() {
        assertFalse(groomingBook.hasClient(ALICE));
    }

    @Test
    public void hasClient_clientInGroomingBook_returnsTrue() {
        groomingBook.addClient(ALICE);
        assertTrue(groomingBook.hasClient(ALICE));
    }

    @Test
    public void hasClient_clientWithSameIdentityFieldsInGroomingBook_returnsTrue() {
        groomingBook.addClient(ALICE);
        Client editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(groomingBook.hasClient(editedAlice));
    }

    @Test
    public void addPet_ownerNotInGroomingBook_throwsClientNotFoundException() {
        assertThrows(ClientNotFoundException.class, () -> groomingBook.addPet(new PetBuilder(ALICE).build()));
    }

    @Test
    public void addPet_ownerInGroomingBook_addsPet() {
        Pet pet = new PetBuilder(ALICE).build();
        groomingBook.addClient(ALICE);

        groomingBook.addPet(pet);

        assertTrue(groomingBook.hasPet(pet));
    }

    @Test
    public void getPetList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> groomingBook.getPetList().remove(0));
    }

    @Test
    public void getClientList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> groomingBook.getClientList().remove(0));
    }

    @Test
    public void setClient_ownerOfPet_replacesPetOwner() {
        groomingBook.addClient(ALICE);
        Pet pet = new PetBuilder(ALICE).build();
        groomingBook.addPet(pet);
        Client editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();

        groomingBook.setClient(ALICE, editedAlice);

        assertSame(editedAlice, groomingBook.getPetList().get(0).getOwner());
    }

    @Test
    public void toStringMethod() {
        String expected = GroomingBook.class.getCanonicalName() + "{clients=" + groomingBook.getClientList()
                + ", pets=" + groomingBook.getPetList() + ", appointments=[]}";
        assertEquals(expected, groomingBook.toString());
    }

    /**
     * A stub ReadOnlyGroomingBook whose clients list can violate interface constraints.
     */
    private static class GroomingBookStub extends GroomingBook {
        private final ObservableList<Client> clients = FXCollections.observableArrayList();

        GroomingBookStub(Collection<Client> clients) {
            this.clients.setAll(clients);
        }

        @Override
        public ObservableList<Client> getClientList() {
            return clients;
        }

        @Override
        public ObservableList<Pet> getPetList() {
            return FXCollections.emptyObservableList();
        }
    }

}

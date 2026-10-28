package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.getTypicalBubuBook;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Client;
import seedu.address.model.client.exceptions.ClientNotFoundException;
import seedu.address.model.client.exceptions.DuplicateClientException;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

public class BubuBookTest {

    private final BubuBook bubuBook = new BubuBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), bubuBook.getClientList());
        assertEquals(List.of(), bubuBook.getPetList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> bubuBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyBubuBook_replacesData() {
        BubuBook newData = getTypicalBubuBook();
        bubuBook.resetData(newData);
        assertEquals(newData, bubuBook);
    }

    @Test
    public void resetData_withDuplicateClients_throwsDuplicateClientException() {
        // Two clients with the same identity fields
        Client editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Client> newClients = List.of(ALICE, editedAlice);
        BubuBookStub newData = new BubuBookStub(newClients);

        assertThrows(DuplicateClientException.class, () -> bubuBook.resetData(newData));
    }

    @Test
    public void hasClient_nullClient_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> bubuBook.hasClient(null));
    }

    @Test
    public void hasClient_clientNotInBubuBook_returnsFalse() {
        assertFalse(bubuBook.hasClient(ALICE));
    }

    @Test
    public void hasClient_clientInBubuBook_returnsTrue() {
        bubuBook.addClient(ALICE);
        assertTrue(bubuBook.hasClient(ALICE));
    }

    @Test
    public void hasClient_clientWithSameIdentityFieldsInBubuBook_returnsTrue() {
        bubuBook.addClient(ALICE);
        Client editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(bubuBook.hasClient(editedAlice));
    }

    @Test
    public void addPet_ownerNotInBubuBook_throwsClientNotFoundException() {
        assertThrows(ClientNotFoundException.class, () -> bubuBook.addPet(new PetBuilder(ALICE).build()));
    }

    //@@author khangsian0412
    @Test
    public void addPet_ownerInBubuBook_addsPet() {
        Pet pet = new PetBuilder(ALICE).build();
        bubuBook.addClient(ALICE);

        bubuBook.addPet(pet);

        assertTrue(bubuBook.hasPet(pet));
    }
    //@@author

    @Test
    public void getPetList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> bubuBook.getPetList().remove(0));
    }

    @Test
    public void getClientList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> bubuBook.getClientList().remove(0));
    }

    //@@author khangsian0412
    @Test
    public void setClient_ownerOfPet_replacesPetOwner() {
        bubuBook.addClient(ALICE);
        Pet pet = new PetBuilder(ALICE).build();
        bubuBook.addPet(pet);
        Client editedAlice = new ClientBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();

        bubuBook.setClient(ALICE, editedAlice);

        assertSame(editedAlice, bubuBook.getPetList().get(0).getOwner());
    }

    @Test
    public void setPet_nameChange_updatesRelatedAppointmentsAndRetainsUnrelatedAppointments() {
        bubuBook.addClient(ALICE);
        Client otherOwner = new ClientBuilder().withPhone("81234567").withEmail("other@example.com").build();
        bubuBook.addClient(otherOwner);
        bubuBook.addPet(new PetBuilder(ALICE).withName("Milo").withRequirement("Original requirement").build());
        bubuBook.addPet(new PetBuilder(otherOwner).withName("Mochi").build());
        Pet milo = bubuBook.getPetList().get(0);
        Pet mochi = bubuBook.getPetList().get(1);
        Appointment miloAppointment = new Appointment(ALICE, milo, new AppointmentDate("01-01-2020"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM);
        Appointment mochiAppointment = new Appointment(otherOwner, mochi, new AppointmentDate("01-01-2020"),
                new StartTime("11:00"), new EndTime("12:00"), Service.NAIL_TRIM);
        bubuBook.addAppointment(miloAppointment);
        bubuBook.addAppointment(mochiAppointment);

        // Non-name changes do not alter the appointment's historical pet snapshot.
        Pet requirementOnlyEdit = new PetBuilder(milo).withRequirement("Updated requirement").build();
        bubuBook.setPet(milo, requirementOnlyEdit);
        assertSame(miloAppointment, bubuBook.getAppointmentList().get(0));

        // A name change updates appointment identity while retaining its other snapshot details.
        Pet renamedMilo = new PetBuilder(requirementOnlyEdit).withName("Rex").build();
        bubuBook.setPet(requirementOnlyEdit, renamedMilo);
        Pet savedRex = bubuBook.getPetList().get(0);

        assertEquals(new PetName("Rex"), bubuBook.getAppointmentList().get(0).getPetName());
        assertTrue(bubuBook.getAppointmentList().get(0).isForPet(savedRex));
        assertEquals("Original requirement", bubuBook.getAppointmentList().get(0).getPet().getRequirement().toString());
        assertSame(mochiAppointment, bubuBook.getAppointmentList().get(1));

        // The renamed pet must still be associated with, and remove, its appointment.
        bubuBook.removePet(savedRex);
        assertEquals(List.of(mochiAppointment), bubuBook.getAppointmentList());
    }
    //@@author

    @Test
    public void toStringMethod() {
        String expected = BubuBook.class.getCanonicalName() + "{clients=" + bubuBook.getClientList()
                + ", pets=" + bubuBook.getPetList() + ", appointments=[]}";
        assertEquals(expected, bubuBook.toString());
    }

    /**
     * A stub ReadOnlyBubuBook whose clients list can violate interface constraints.
     */
    private static class BubuBookStub extends BubuBook {
        private final ObservableList<Client> clients = FXCollections.observableArrayList();

        BubuBookStub(Collection<Client> clients) {
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

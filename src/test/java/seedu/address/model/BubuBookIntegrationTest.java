package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.appointment.exceptions.OverlappingAppointmentException;
import seedu.address.model.client.Client;
import seedu.address.model.client.exceptions.ClientNotFoundException;
import seedu.address.model.pet.Pet;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

public class BubuBookIntegrationTest {
    private final Appointment appointment = new Appointment(ALICE.getPhone(), "Milo",
            new AppointmentDate("01-01-2000"), new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM);

    @Test
    public void resetData_invalidAppointments_preservesClientsPetsAndSchedule() {
        BubuBook book = populatedBook();
        BubuBook before = new BubuBook(book);
        BubuBook invalid = new BubuBook() {
            @Override
            public ObservableList<Appointment> getAppointmentList() {
                return FXCollections.observableArrayList(appointment, appointment);
            }
        };
        invalid.addClient(BENSON);

        assertThrows(OverlappingAppointmentException.class, () -> book.resetData(invalid));
        assertEquals(before, book);
    }

    @Test
    public void resetData_validReplacement_preservesAllObservableViews() {
        BubuBook book = new BubuBook();
        ObservableList<Client> clients = book.getClientList();
        ObservableList<Pet> pets = book.getPetList();
        ObservableList<Appointment> appointments = book.getAppointmentList();
        BubuBook replacement = populatedBook();

        book.resetData(replacement);
        replacement.clear();

        assertEquals(List.of(ALICE), clients);
        assertEquals(List.of(new PetBuilder(ALICE).withBreed("Golden retriever").build()), pets);
        assertEquals(List.of(appointment), appointments);
        assertSame(book.getClientList().get(0), pets.get(0).getOwner());
        assertThrows(UnsupportedOperationException.class, clients::clear);
        assertThrows(UnsupportedOperationException.class, pets::clear);
        assertThrows(UnsupportedOperationException.class, appointments::clear);
    }

    @Test
    public void setClients_missingOwner_rejectsWithoutChangingAnyRecords() {
        BubuBook book = populatedBook();
        BubuBook before = new BubuBook(book);

        assertThrows(ClientNotFoundException.class, () -> book.setClients(List.of(BENSON)));
        assertEquals(before, book);
    }

    @Test
    public void setClients_updatedOwner_refreshesPetsAndPreservesSchedule() {
        BubuBook book = populatedBook();
        Client editedAlice = new ClientBuilder(ALICE).withAddress("123 New Street").build();

        book.setClients(List.of(editedAlice));

        assertSame(editedAlice, book.getPetList().get(0).getOwner());
        assertEquals(List.of(appointment), book.getAppointmentList());
    }

    @Test
    public void resetData_missingOwner_preservesAllCollections() {
        BubuBook book = populatedBook();
        BubuBook before = new BubuBook(book);
        BubuBook invalid = new BubuBook() {
            @Override
            public ObservableList<Pet> getPetList() {
                return FXCollections.observableArrayList(new PetBuilder(BENSON).build());
            }
        };
        invalid.addClient(ALICE);

        assertThrows(ClientNotFoundException.class, () -> book.resetData(invalid));
        assertEquals(before, book);
    }

    private BubuBook populatedBook() {
        BubuBook book = new BubuBook();
        book.addClient(ALICE);
        book.addPet(new PetBuilder(ALICE).withBreed("Golden retriever").build());
        book.addAppointment(appointment);
        return book;
    }
}

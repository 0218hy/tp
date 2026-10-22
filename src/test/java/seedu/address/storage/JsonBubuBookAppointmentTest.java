package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.BubuBook;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.storage.appointment.JsonAdaptedAppointment;
import seedu.address.storage.client.JsonAdaptedClient;
import seedu.address.storage.pet.JsonAdaptedPet;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

public class JsonBubuBookAppointmentTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void saveAndRead_historicalAppointments_preservesAllFieldsAndOrder() throws Exception {
        JsonBubuBookStorage storage = storage("nested/bububook.json");
        BubuBook book = new BubuBook();
        book.addAppointment(create("10:00", "11:00"));
        book.addAppointment(create("11:00", "11:30"));
        storage.saveBubuBook(book);
        assertEquals(book, storage.readBubuBook().orElseThrow());
        String json = Files.readString(storage.getBubuBookFilePath());
        assertTrue(json.contains("\"ownerPhone\" : \"91234567\""));
        assertTrue(json.contains("\"date\" : \"01-01-2000\""));
        assertTrue(json.contains("\"service\" : \"Full groom\""));
    }

    @Test
    public void saveAndRead_participantSnapshots_reusesMatchingProfiles() throws Exception {
        JsonBubuBookStorage storage = storage("snapshots.json");
        BubuBook book = bookWithParticipantSnapshots();

        storage.saveBubuBook(book);

        var restored = storage.readBubuBook().orElseThrow();
        Appointment restoredAppointment = restored.getAppointmentList().get(0);
        assertEquals(book, restored);
        assertSame(restored.getClientList().get(0), restoredAppointment.getOwner());
        assertSame(restored.getPetList().get(0), restoredAppointment.getPet());
    }

    @Test
    public void saveAndRead_profileEdits_preservesParticipantSnapshots() throws Exception {
        JsonBubuBookStorage storage = storage("edited-profiles.json");
        BubuBook book = bookWithParticipantSnapshots();
        Appointment appointment = book.getAppointmentList().get(0);
        Client editedOwner = new ClientBuilder(appointment.getOwner()).withAddress("New address").build();
        book.setClient(appointment.getOwner(), editedOwner);
        book.setPet(book.getPetList().get(0), new PetBuilder(appointment.getPet()).withOwner(editedOwner)
                .withName("New name").withRequirement("New care instructions").build());

        storage.saveBubuBook(book);

        var restored = storage.readBubuBook().orElseThrow();
        assertEquals(book, restored);
        // Pet renames cascade to appointment identity, while the appointment retains its profile snapshot.
        assertEquals(new Appointment(appointment.getOwner(), appointment.getPet().withName(new PetName("New name")),
                appointment.getDate(), appointment.getStartTime(), appointment.getEndTime(), appointment.getService()),
                restored.getAppointmentList().get(0));
        assertSame(restored.getAppointmentList().get(0).getOwner(),
                restored.getAppointmentList().get(0).getPet().getOwner());
    }

    @Test
    public void saveAndRead_changedPhone_updatesBothParticipantLinksAndPreservesProfiles() throws Exception {
        JsonBubuBookStorage storage = storage("changed-phone.json");
        BubuBook book = bookWithParticipantSnapshots();
        Appointment original = book.getAppointmentList().get(0);
        Client editedOwner = new ClientBuilder(original.getOwner())
                .withPhone("81234567").withAddress("New address").build();
        book.setClient(original.getOwner(), editedOwner);

        storage.saveBubuBook(book);

        var restored = storage.readBubuBook().orElseThrow();
        Appointment updated = restored.getAppointmentList().get(0);
        Client expectedOwner = new ClientBuilder(original.getOwner()).withPhone("81234567").build();
        assertEquals(book, restored);
        assertEquals(new Appointment(expectedOwner, original.getPet().withOwner(expectedOwner),
                original.getDate(), original.getStartTime(), original.getEndTime(), original.getService()), updated);
        assertSame(updated.getOwner(), updated.getPet().getOwner());
        assertEquals(new Phone("91234567"), original.getOwnerPhone());
    }

    @Test
    public void saveAndRead_removedPet_removesRelatedAppointments() throws Exception {
        JsonBubuBookStorage storage = storage("removed-pet.json");
        BubuBook book = bookWithParticipantSnapshots();
        book.removePet(book.getPetList().get(0));

        storage.saveBubuBook(book);

        var restored = storage.readBubuBook().orElseThrow();
        assertEquals(book, restored);
        assertTrue(restored.getAppointmentList().isEmpty());
        assertTrue(restored.getPetList().isEmpty());
        assertEquals(1, restored.getClientList().size());
    }

    @Test
    public void saveAndRead_removedClientAfterPhoneChange_removesRelatedAppointments() throws Exception {
        JsonBubuBookStorage storage = storage("removed-client.json");
        BubuBook book = bookWithParticipantSnapshots();
        Client owner = book.getClientList().get(0);
        Client editedOwner = new ClientBuilder(owner).withPhone("81234567").build();
        book.setClient(owner, editedOwner);
        book.removeClient(editedOwner);

        storage.saveBubuBook(book);

        assertEquals(new BubuBook(), storage.readBubuBook().orElseThrow());
    }

    @Test
    public void readBubuBook_legacyAppointment_resolvesParticipantsAndSavesSnapshots() throws Exception {
        JsonBubuBookStorage storage = storage("legacy.json");
        Appointment expected = create("10:00", "11:00");
        JsonAdaptedAppointment legacy = new JsonAdaptedAppointment("91234567", "  bUbU  ",
                "01-01-2000", "10:00", "11:00", "Full groom");
        JsonUtil.saveJsonFile(new JsonSerializableBubuBook(List.of(new JsonAdaptedClient(expected.getOwner())),
                List.of(new JsonAdaptedPet(expected.getPet())), List.of(legacy)), storage.getBubuBookFilePath());
        var restored = storage.readBubuBook().orElseThrow();
        assertEquals(expected, restored.getAppointmentList().get(0));
        assertSame(restored.getClientList().get(0), restored.getAppointmentList().get(0).getOwner());
        assertSame(restored.getPetList().get(0), restored.getAppointmentList().get(0).getPet());
        storage.saveBubuBook(restored);
        assertTrue(Files.readString(storage.getBubuBookFilePath()).contains("\"owner\" : {"));
        assertEquals(restored, storage.readBubuBook().orElseThrow());
    }

    @Test
    public void toModelType_snapshotWithoutRegisteredParticipants_restoresCompleteProfiles() throws Exception {
        Appointment original = bookWithParticipantSnapshots().getAppointmentList().get(0);

        Appointment restored = new JsonAdaptedAppointment(original).toModelType();

        assertEquals(original, restored);
        assertSame(restored.getOwner(), restored.getPet().getOwner());
    }

    @Test
    public void toModelType_legacyDifferentPetName_rejected() {
        Appointment participants = create("10:00", "11:00");
        JsonAdaptedAppointment legacy = new JsonAdaptedAppointment("91234567", "Mochi",
                "01-01-2000", "10:00", "11:00", "Full groom");

        IllegalValueException exception = assertThrows(IllegalValueException.class, () -> legacy.toModelType(
                List.of(participants.getOwner()), List.of(participants.getPet())));

        assertEquals("Legacy appointment pet is not registered under its owner.", exception.getMessage());
    }

    @Test
    public void toModelType_legacyMissingParticipants_rejected() {
        Appointment participants = create("10:00", "11:00");
        JsonAdaptedAppointment legacy = new JsonAdaptedAppointment("91234567", "BuBu",
                "01-01-2000", "10:00", "11:00", "Full groom");
        assertThrows(IllegalValueException.class, legacy::toModelType);
        assertThrows(IllegalValueException.class, () -> legacy.toModelType(
                List.of(participants.getOwner()), List.of()));
        Client otherOwner = new ClientBuilder().withPhone("81234567").build();
        Pet otherPet = new PetBuilder(otherOwner).withName("BuBu").build();
        assertThrows(IllegalValueException.class, () -> legacy.toModelType(
                List.of(participants.getOwner(), otherOwner), List.of(otherPet)));
    }

    @Test
    public void toModelType_incompleteOrMismatchedSnapshots_rejected() {
        Appointment appointment = create("10:00", "11:00");
        JsonAdaptedClient owner = new JsonAdaptedClient(appointment.getOwner());
        JsonAdaptedPet pet = new JsonAdaptedPet(appointment.getPet());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedAppointment(owner, null, null, null,
                "01-01-2000", "10:00", "11:00", "Full groom").toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedAppointment(null, pet, null, null,
                "01-01-2000", "10:00", "11:00", "Full groom").toModelType());
        JsonAdaptedClient otherOwner = new JsonAdaptedClient(new ClientBuilder().withPhone("81234567").build());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedAppointment(otherOwner, pet, null, null,
                "01-01-2000", "10:00", "11:00", "Full groom").toModelType());
    }

    @Test
    public void saveBubuBook_replacesPreviousScheduleAndCleansTemporaryFiles() throws Exception {
        JsonBubuBookStorage storage = storage("bububook.json");
        BubuBook book = new BubuBook();
        book.addAppointment(create("10:00", "11:00"));
        storage.saveBubuBook(book);
        storage.saveBubuBook(new BubuBook());
        assertEquals(new BubuBook(), storage.readBubuBook().orElseThrow());
        try (var paths = Files.list(temporaryFolder)) {
            assertEquals(List.of(storage.getBubuBookFilePath()), paths.toList());
        }
    }

    @Test
    public void readBubuBook_missingFile_returnsEmpty() throws Exception {
        assertTrue(storage("missing.json").readBubuBook().isEmpty());
    }

    @Test
    public void readBubuBook_malformedOrMissingData_rejectedWithoutChangingFile() throws Exception {
        JsonBubuBookStorage storage = storage("invalid.json");
        for (String json : List.of("broken", "null", "[]", "{}", "{\"appointments\":null}",
                "{\"clients\":[],\"pets\":[],\"appointments\":[null]}",
                "{\"clients\":[],\"pets\":[],\"appointments\":[{}]}")) {
            Files.writeString(storage.getBubuBookFilePath(), json);
            assertThrows(DataLoadingException.class, storage::readBubuBook);
            assertEquals(json, Files.readString(storage.getBubuBookFilePath()));
        }
    }

    @Test
    public void readBubuBook_overlappingRecords_rejected() throws Exception {
        JsonBubuBookStorage storage = storage("overlap.json");
        List<JsonAdaptedAppointment> records = List.of(new JsonAdaptedAppointment(create("10:00", "11:00")),
                new JsonAdaptedAppointment(create("10:30", "11:30")));
        JsonUtil.saveJsonFile(new JsonSerializableBubuBook(List.of(), List.of(), records),
                storage.getBubuBookFilePath());
        assertThrows(DataLoadingException.class, storage::readBubuBook);
    }

    @Test
    public void toModelType_missingAndInvalidFields_rejected() {
        Appointment participants = create("10:00", "11:00");
        List<String> valid = List.of("91234567", "BuBu", "01-01-2000", "10:00", "11:00", "Full groom");
        List<String> invalid = List.of("abc", "!!!", "31-02-2000", "10:15", "09:30", "Unknown");
        for (int i = 0; i < valid.size(); i++) {
            List<String> fields = new ArrayList<>(valid);
            fields.set(i, null);
            assertThrows(IllegalValueException.class, () -> adapt(fields).toModelType(
                    List.of(participants.getOwner()), List.of(participants.getPet())));
            fields.set(i, invalid.get(i));
            assertThrows(IllegalValueException.class, () -> adapt(fields).toModelType(
                    List.of(participants.getOwner()), List.of(participants.getPet())));
        }
        assertThrows(IllegalValueException.class, () ->
                new JsonSerializableBubuBook(List.of(), List.of(),
                        Arrays.asList((JsonAdaptedAppointment) null)).toModelType());
    }

    @Test
    public void saveBubuBook_targetIsDirectory_reportsFailureAndCleansTemporaryFile() throws Exception {
        JsonBubuBookStorage storage = storage("bububook.json");
        Files.createDirectory(storage.getBubuBookFilePath());
        Path marker = storage.getBubuBookFilePath().resolve("keep.txt");
        Files.writeString(marker, "keep");
        assertThrows(IOException.class, () -> storage.saveBubuBook(new BubuBook()));
        assertEquals("keep", Files.readString(marker));
        try (var paths = Files.list(temporaryFolder)) {
            assertEquals(List.of(storage.getBubuBookFilePath()), paths.toList());
        }
    }

    @Test
    public void constructorAndSave_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new JsonBubuBookStorage(null));
        assertThrows(NullPointerException.class, () -> storage("bububook.json").saveBubuBook(null));
    }

    private BubuBook bookWithParticipantSnapshots() {
        Client owner = new ClientBuilder().withPhone("91234567").withTags("friend").build();
        Pet pet = new PetBuilder(owner).withName("BuBu").withBreed("Poodle")
                .withRequirement("Sensitive skin").build();
        Appointment appointment = new Appointment(owner, pet, new AppointmentDate("01-01-2000"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM);
        BubuBook book = new BubuBook();
        book.addClient(owner);
        book.addPet(pet);
        book.addAppointment(appointment);
        return book;
    }

    private JsonBubuBookStorage storage(String filename) {
        return new JsonBubuBookStorage(temporaryFolder.resolve(filename));
    }

    private JsonAdaptedAppointment adapt(List<String> fields) {
        return new JsonAdaptedAppointment(fields.get(0), fields.get(1), fields.get(2),
                fields.get(3), fields.get(4), fields.get(5));
    }

    private Appointment create(String start, String end) {
        return createAppointment(new Phone("91234567"), "BuBu", new AppointmentDate("01-01-2000"),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }
}

package seedu.address.storage.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import seedu.address.model.GroomingBook;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Phone;

public class JsonAppointmentStorageTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void saveAndRead_historicalAppointments_preservesAllFieldsAndOrder() throws Exception {
        JsonAppointmentStorage storage = storage("nested/appointments.json");
        GroomingBook book = new GroomingBook();
        book.addAppointment(create("10:00", "11:00"));
        book.addAppointment(create("11:00", "11:30"));
        storage.saveAppointmentData(book);
        assertEquals(book, storage.readAppointmentData().orElseThrow());
        String json = Files.readString(storage.getAppointmentFilePath());
        assertTrue(json.contains("\"ownerPhone\" : \"91234567\""));
        assertTrue(json.contains("\"date\" : \"01-01-2000\""));
        assertTrue(json.contains("\"service\" : \"Full groom\""));
    }

    @Test
    public void saveAppointmentData_replacesPreviousScheduleAndCleansTemporaryFiles() throws Exception {
        JsonAppointmentStorage storage = storage("appointments.json");
        GroomingBook book = new GroomingBook();
        book.addAppointment(create("10:00", "11:00"));
        storage.saveAppointmentData(book);
        storage.saveAppointmentData(new GroomingBook());
        assertEquals(new GroomingBook(), storage.readAppointmentData().orElseThrow());
        try (var paths = Files.list(temporaryFolder)) {
            assertEquals(List.of(storage.getAppointmentFilePath()), paths.toList());
        }
    }

    @Test
    public void readAppointmentData_missingFile_returnsEmpty() throws Exception {
        assertTrue(storage("missing.json").readAppointmentData().isEmpty());
    }

    @Test
    public void readAppointmentData_malformedOrMissingData_rejectedWithoutChangingFile() throws Exception {
        JsonAppointmentStorage storage = storage("invalid.json");
        for (String json : List.of("broken", "null", "[]", "{}", "{\"appointments\":null}",
                "{\"appointments\":[null]}", "{\"appointments\":[{}]}")) {
            Files.writeString(storage.getAppointmentFilePath(), json);
            assertThrows(DataLoadingException.class, storage::readAppointmentData);
            assertEquals(json, Files.readString(storage.getAppointmentFilePath()));
        }
    }

    @Test
    public void readAppointmentData_overlappingRecords_rejected() throws Exception {
        JsonAppointmentStorage storage = storage("overlap.json");
        List<JsonAdaptedAppointment> records = List.of(new JsonAdaptedAppointment(create("10:00", "11:00")),
                new JsonAdaptedAppointment(create("10:30", "11:30")));
        JsonUtil.saveJsonFile(new JsonSerializableAppointments(records), storage.getAppointmentFilePath());
        assertThrows(DataLoadingException.class, storage::readAppointmentData);
    }

    @Test
    public void toModelType_missingAndInvalidFields_rejected() {
        List<String> valid = List.of("91234567", "BuBu", "01-01-2000", "10:00", "11:00", "Full groom");
        List<String> invalid = List.of("abc", "!!!", "31-02-2000", "10:15", "09:30", "Unknown");
        for (int i = 0; i < valid.size(); i++) {
            List<String> fields = new ArrayList<>(valid);
            fields.set(i, null);
            assertThrows(IllegalValueException.class, () -> adapt(fields).toModelType());
            fields.set(i, invalid.get(i));
            assertThrows(IllegalValueException.class, () -> adapt(fields).toModelType());
        }
        assertThrows(IllegalValueException.class, () ->
                new JsonSerializableAppointments(Arrays.asList((JsonAdaptedAppointment) null)).toModelType());
    }

    @Test
    public void saveAppointmentData_targetIsDirectory_reportsFailureAndCleansTemporaryFile() throws Exception {
        JsonAppointmentStorage storage = storage("appointments.json");
        Files.createDirectory(storage.getAppointmentFilePath());
        Path marker = storage.getAppointmentFilePath().resolve("keep.txt");
        Files.writeString(marker, "keep");
        assertThrows(IOException.class, () -> storage.saveAppointmentData(new GroomingBook()));
        assertEquals("keep", Files.readString(marker));
        try (var paths = Files.list(temporaryFolder)) {
            assertEquals(List.of(storage.getAppointmentFilePath()), paths.toList());
        }
    }

    @Test
    public void constructorAndSave_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new JsonAppointmentStorage(null));
        assertThrows(NullPointerException.class, () -> storage("appointments.json").saveAppointmentData(null));
    }

    private JsonAppointmentStorage storage(String filename) {
        return new JsonAppointmentStorage(temporaryFolder.resolve(filename));
    }

    private JsonAdaptedAppointment adapt(List<String> fields) {
        return new JsonAdaptedAppointment(fields.get(0), fields.get(1), fields.get(2),
                fields.get(3), fields.get(4), fields.get(5));
    }

    private Appointment create(String start, String end) {
        return new Appointment(new Phone("91234567"), "BuBu", new AppointmentDate("01-01-2000"),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }
}

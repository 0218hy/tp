package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.BubuBook;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonBubuBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PetBuilder;
import seedu.address.testutil.RecordFileLockHolder;

/** Verifies that independent BuBu instances cannot silently replace each other's saved records. */
public class ConcurrentRecordSaveTest {
    private static final String ADD_CLIENT =
            "add-client n/Bob Tan i/89997777 e/bob@example.com a/123 Test Road";
    private static final String CHANGED_MESSAGE = "Records changed outside this BuBu window. "
            + "Close and reopen BuBu, then retry your command.";

    @TempDir
    public Path temporaryFolder;

    //@@author 0218hy
    @Test
    public void execute_staleWindow_preservesCommittedClientsPetsAndAppointments() throws Exception {
        List<String> commands = List.of(ADD_CLIENT,
                "add-pet p/Milo i/" + ALICE.getPhone() + " s/Dog r/Needs gentle brushing",
                "add-appointment i/" + ALICE.getPhone()
                        + " p/BuBu d/01-01-9999 st/10:00 et/11:00 s/Full groom");
        for (int index = 0; index < commands.size(); index++) {
            Path path = temporaryFolder.resolve("book-" + index + ".json");
            BubuBook initial = new BubuBook();
            initial.addClient(ALICE);
            initial.addPet(new PetBuilder(ALICE).withName("BuBu").build());
            new JsonBubuBookStorage(path).saveBubuBook(initial);
            StorageManager firstStorage = storage(path);
            StorageManager secondStorage = storage(path);
            Model first = new MainApp().initModelManager(firstStorage, new UserPrefs());
            Model second = new MainApp().initModelManager(secondStorage, new UserPrefs());
            LogicManager staleLogic = new LogicManager(second, secondStorage);

            new LogicManager(first, firstStorage).execute(commands.get(index));
            byte[] committed = Files.readAllBytes(path);
            for (String staleCommand : List.of(ADD_CLIENT, commands.get(1),
                    "edit-client i/" + ALICE.getPhone() + " n/Alice Lim", "clear")) {
                CommandException exception = assertThrows(CommandException.class, () ->
                        staleLogic.execute(staleCommand));
                assertEquals(CHANGED_MESSAGE, exception.getMessage());
                assertEquals(initial, second.getBubuBook());
                assertArrayEquals(committed, Files.readAllBytes(path));
            }
            assertEquals(first.getBubuBook(), new JsonBubuBookStorage(path).readBubuBook().orElseThrow());

            StorageManager restartedStorage = storage(path);
            Model restarted = new MainApp().initModelManager(restartedStorage, new UserPrefs());
            new LogicManager(restarted, restartedStorage).execute("edit-client i/" + ALICE.getPhone() + " n/Alice Lim");
            assertEquals(restarted.getBubuBook(), restartedStorage.readBubuBook().orElseThrow());
        }
    }

    @Test
    public void save_missingAtStartup_secondWriterCannotOverwriteNewFile() throws Exception {
        Path path = temporaryFolder.resolve("new-folder/book.json");
        JsonBubuBookStorage first = new JsonBubuBookStorage(path);
        JsonBubuBookStorage second = new JsonBubuBookStorage(path);
        assertTrue(first.readBubuBook().isEmpty());
        assertTrue(second.readBubuBook().isEmpty());
        assertFalse(Files.exists(path.getParent()));
        first.saveBubuBook(bookWithAlice());
        byte[] committed = Files.readAllBytes(path);
        assertThrows(IOException.class, () -> second.saveBubuBook(new BubuBook()));
        assertArrayEquals(committed, Files.readAllBytes(path));
    }

    @Test
    public void save_externalEditWithSameTimestamp_rejectsStaleContents() throws Exception {
        Path path = temporaryFolder.resolve("book.json");
        JsonBubuBookStorage storage = new JsonBubuBookStorage(path);
        storage.saveBubuBook(bookWithAlice());
        FileTime timestamp = Files.getLastModifiedTime(path);
        String changed = Files.readString(path).replace("Alice Pauline", "Other Pauline");
        Files.writeString(path, changed);
        Files.setLastModifiedTime(path, timestamp);
        assertThrows(IOException.class, () -> storage.saveBubuBook(new BubuBook()));
        assertEquals(changed, Files.readString(path));
    }

    @Test
    public void save_deletedFile_doesNotRecreateStaleRecords() throws Exception {
        Path path = temporaryFolder.resolve("book.json");
        JsonBubuBookStorage storage = new JsonBubuBookStorage(path);
        storage.saveBubuBook(bookWithAlice());
        Files.delete(path);
        assertThrows(IOException.class, () -> storage.saveBubuBook(new BubuBook()));
        assertFalse(Files.exists(path));
    }

    @Test
    public void save_existingFileWithoutReading_refusesOverwrite() throws Exception {
        Path path = temporaryFolder.resolve("book.json");
        new JsonBubuBookStorage(path).saveBubuBook(bookWithAlice());
        byte[] committed = Files.readAllBytes(path);
        assertThrows(IOException.class, () -> new JsonBubuBookStorage(path).saveBubuBook(new BubuBook()));
        assertArrayEquals(committed, Files.readAllBytes(path));
    }

    @Test
    public void save_lockedFile_preservesFileAndAllowsRetryAfterRelease() throws Exception {
        Path path = temporaryFolder.resolve("book.json");
        JsonBubuBookStorage storage = new JsonBubuBookStorage(path);
        storage.saveBubuBook(bookWithAlice());
        byte[] committed = Files.readAllBytes(path);
        Path lockPath = path.resolveSibling(path.getFileName() + ".lock");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.lock()) {
            assertTrue(lock.isValid());
            assertThrows(IOException.class, () -> storage.saveBubuBook(new BubuBook()));
            assertArrayEquals(committed, Files.readAllBytes(path));
        }
        storage.saveBubuBook(new BubuBook());
        assertEquals(new BubuBook(), storage.readBubuBook().orElseThrow());
    }

    @Test
    public void save_lockHeldByAnotherProcess_rejectsAndReleasesAfterProcessExit() throws Exception {
        Path path = temporaryFolder.resolve("process.json");
        JsonBubuBookStorage records = new JsonBubuBookStorage(path);
        records.saveBubuBook(bookWithAlice());
        byte[] committed = Files.readAllBytes(path);
        Path java = Path.of(System.getProperty("java.home"), "bin", "java");
        String classPath = Path.of(RecordFileLockHolder.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).toString();
        Process holder = new ProcessBuilder(java.toString(), "-cp", classPath,
                RecordFileLockHolder.class.getName(), path.resolveSibling("process.json.lock").toString())
                .redirectErrorStream(true).start();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var ready = executor.submit(() -> holder.inputReader().readLine());
            try {
                assertEquals("locked", ready.get(10, TimeUnit.SECONDS));
                IOException exception = assertThrows(IOException.class, () -> records.saveBubuBook(new BubuBook()));
                assertEquals("Another BuBu window is saving records. Retry your command.", exception.getMessage());
                assertArrayEquals(committed, Files.readAllBytes(path));
            } finally {
                holder.destroyForcibly();
                assertTrue(holder.waitFor(10, TimeUnit.SECONDS));
            }
        }
        records.saveBubuBook(new BubuBook());
        assertEquals(new BubuBook(), records.readBubuBook().orElseThrow());
    }

    @Test
    public void save_simultaneousWriters_exactlyOneSaveSucceeds() throws Exception {
        for (int index = 0; index < 5; index++) {
            Path path = temporaryFolder.resolve("concurrent-" + index + ".json");
            new JsonBubuBookStorage(path).saveBubuBook(new BubuBook());
            JsonBubuBookStorage first = new JsonBubuBookStorage(path);
            JsonBubuBookStorage second = new JsonBubuBookStorage(path);
            first.readBubuBook();
            second.readBubuBook();
            BubuBook other = new BubuBook();
            other.addClient(BENSON);
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            try (var executor = Executors.newFixedThreadPool(2)) {
                var firstResult = executor.submit(() -> saveAfterSignal(first, bookWithAlice(), ready, start));
                var secondResult = executor.submit(() -> saveAfterSignal(second, other, ready, start));
                try {
                    assertTrue(ready.await(5, TimeUnit.SECONDS));
                } finally {
                    start.countDown();
                }
                boolean firstSaved = firstResult.get(5, TimeUnit.SECONDS);
                boolean secondSaved = secondResult.get(5, TimeUnit.SECONDS);
                assertTrue(firstSaved ^ secondSaved);
                assertEquals(firstSaved ? bookWithAlice() : other,
                        new JsonBubuBookStorage(path).readBubuBook().orElseThrow());
            }
        }
    }

    @Test
    public void save_alternatePaths_trackVersionsIndependently() throws Exception {
        Path first = temporaryFolder.resolve("first.json");
        Path second = temporaryFolder.resolve("second.json");
        JsonBubuBookStorage storage = new JsonBubuBookStorage(first);
        storage.saveBubuBook(bookWithAlice());
        storage.saveBubuBook(new BubuBook(), second);
        storage.saveBubuBook(new BubuBook(), temporaryFolder.resolve("./first.json"));
        storage.saveBubuBook(bookWithAlice(), second);
        assertEquals(new BubuBook(), storage.readBubuBook().orElseThrow());
        assertEquals(bookWithAlice(), storage.readBubuBook(second).orElseThrow());
    }

    private boolean saveAfterSignal(JsonBubuBookStorage storage, BubuBook book,
            CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        assertTrue(start.await(5, TimeUnit.SECONDS));
        try {
            storage.saveBubuBook(book);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private BubuBook bookWithAlice() {
        BubuBook book = new BubuBook();
        book.addClient(ALICE);
        return book;
    }

    private StorageManager storage(Path path) {
        return new StorageManager(new JsonBubuBookStorage(path),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }
    //@@author
}

//@@author anderschow
package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.BuBuParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.client.Address;
import seedu.address.storage.JsonBubuBookStorage;

public class ClientAddressTest {
    @TempDir
    public Path testFolder;

    @Test
    public void addAndEdit_apostropheAddress_survivesStorageRoundTrip() throws Exception {
        BuBuParser parser = new BuBuParser();
        Model model = new ModelManager();
        JsonBubuBookStorage storage = new JsonBubuBookStorage(testFolder.resolve("addresses.json"));
        String address = "12 St. John's Road";
        parser.parseCommand("add-client n/Carol Tan i/83334444 e/carol@example.com a/" + address).execute(model);
        assertEquals(address, model.getBubuBook().getClientList().getFirst().getAddress().value);
        storage.saveBubuBook(model.getBubuBook());
        assertEquals(model.getBubuBook(), storage.readBubuBook().orElseThrow());

        parser.parseCommand("edit-client i/83334444 a/123 Old Road").execute(model);
        parser.parseCommand("edit-client i/83334444 a/" + address).execute(model);
        assertEquals(address, model.getBubuBook().getClientList().getFirst().getAddress().value);
        storage.saveBubuBook(model.getBubuBook());
        assertEquals(model.getBubuBook(), storage.readBubuBook().orElseThrow());
    }

    @Test
    public void addAndEdit_invalidAddresses_reportSharedConstraint() {
        BuBuParser parser = new BuBuParser();
        for (String address : new String[]{"1234", "A".repeat(121), "12 Road @ 3", "12 Road / 3"}) {
            assertThrows(ParseException.class, Address.MESSAGE_CONSTRAINTS, () -> parser.parseCommand(
                    "add-client n/Carol Tan i/83334444 e/carol@example.com a/" + address));
            assertThrows(ParseException.class, Address.MESSAGE_CONSTRAINTS, () -> parser.parseCommand(
                    "edit-client i/83334444 a/" + address));
        }
    }
}
//@@author

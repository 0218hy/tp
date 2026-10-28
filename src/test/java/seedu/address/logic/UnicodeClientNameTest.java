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
import seedu.address.model.client.ClientName;
import seedu.address.storage.JsonBubuBookStorage;

public class UnicodeClientNameTest {
    @TempDir
    public Path testFolder;

    @Test
    public void addAndEdit_unicodeNames_surviveStorageRoundTrip() throws Exception {
        BuBuParser parser = new BuBuParser();
        JsonBubuBookStorage storage = new JsonBubuBookStorage(testFolder.resolve("unicode.json"));
        String[] inputs = {"José Tan", "李小明", "Jose\u0301 Tan", "अनिता", "\uD840\uDC00".repeat(60)};
        String[] expected = {"José Tan", "李小明", "José Tan", "अनिता", "\uD840\uDC00".repeat(60)};
        for (int i = 0; i < inputs.length; i++) {
            Model model = new ModelManager();
            parser.parseCommand("add-client n/" + inputs[i]
                    + " i/84445555 e/li@example.com a/123 Test Road").execute(model);
            assertEquals(expected[i], model.getBubuBook().getClientList().getFirst().getName().fullName);
            storage.saveBubuBook(model.getBubuBook());
            assertEquals(model.getBubuBook(), storage.readBubuBook().orElseThrow());

            parser.parseCommand("edit-client i/84445555 n/Alice Tan").execute(model);
            parser.parseCommand("edit-client i/84445555 n/" + inputs[i]).execute(model);
            assertEquals(expected[i], model.getBubuBook().getClientList().getFirst().getName().fullName);
            storage.saveBubuBook(model.getBubuBook());
            assertEquals(model.getBubuBook(), storage.readBubuBook().orElseThrow());
        }
    }

    @Test
    public void addAndEdit_invalidNames_reportSharedConstraint() {
        BuBuParser parser = new BuBuParser();
        for (String name : new String[]{"José1", "李😀", "e\u0301", "李".repeat(61)}) {
            assertThrows(ParseException.class, ClientName.MESSAGE_CONSTRAINTS, () -> parser.parseCommand(
                    "add-client n/" + name + " i/84445555 e/li@example.com a/123 Test Road"));
            assertThrows(ParseException.class, ClientName.MESSAGE_CONSTRAINTS, () -> parser.parseCommand(
                    "edit-client i/84445555 n/" + name));
        }
    }
}
//@@author

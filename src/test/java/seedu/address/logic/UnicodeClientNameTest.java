//@@author anderschow
package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;

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
    public void addAndSearch_canonicallyEquivalentNames_matchIgnoringCase() throws Exception {
        BuBuParser parser = new BuBuParser();
        for (String name : List.of("Jos\u00e9 Tan", "Jose\u0301 Tan")) {
            Model model = new ModelManager();
            parser.parseCommand("add-client n/" + name
                    + " i/84445555 e/li@example.com a/123 Test Road").execute(model);
            for (String query : List.of("Jos\u00e9 Tan", "Jose\u0301 Tan", "JOS\u00c9 TAN", "JOSE\u0301 TAN",
                    "se\u0301")) {
                parser.parseCommand("list-clients q/" + query).execute(model);
                assertEquals(model.getBubuBook().getClientList(), model.getFilteredClientList());
            }
            for (String query : List.of("Jose Tan", "Jos\u00e8 Tan")) {
                parser.parseCommand("list-clients q/" + query).execute(model);
                assertEquals(List.of(), model.getFilteredClientList());
            }
        }
    }

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

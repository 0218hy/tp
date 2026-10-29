package seedu.address.logic.commands.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.BuBuParser;
import seedu.address.model.ModelManager;
import seedu.address.model.client.Client;
import seedu.address.testutil.ClientBuilder;

public class ListClientsCommandTest {
    //@@author anderschow
    private final ModelManager model = new ModelManager();
    private final Client zoe = new ClientBuilder().withName("Zoe").withPhone("91234567").build();
    private final Client amelia = new ClientBuilder().withName("amelia Tan").withPhone("87654321")
            .withEmail("amelia@example.com").withAddress("10 Sunset Road #01-02").build();
    //@@author

    //@@author anderschow
    @Test
    public void execute_emailQuery_preservesLiteralUnicodeMatching() {
        Client sam = new ClientBuilder().withName("Sam Tan").withEmail("cafe\u0301@example.com").build();
        model.addClient(sam);
        new ListClientsCommand("CAFE\u0301").execute(model);
        assertEquals(List.of(sam), model.getFilteredClientList());
        new ListClientsCommand("CAF\u00c9").execute(model);
        assertEquals(List.of(), model.getFilteredClientList());
    }

    @Test
    public void execute_queryWithContactPunctuation_matchesLiteralSubstring() throws Exception {
        Client sam = new ClientBuilder().withName("Sam Tan").withPhone("89998888")
                .withEmail("sam+grooming_test!/care@example.com").withAddress("10 Sunset Road, #01-02").build();
        model.addClient(sam);
        model.addClient(amelia);
        for (String query : List.of("SAM+GROOMING", "grooming_test!", "/care", "Road, #01-02")) {
            new BuBuParser().parseCommand("list-clients q/" + query).execute(model);
            assertEquals(List.of(sam), model.getFilteredClientList());
            assertEquals(List.of(sam, amelia), model.getBubuBook().getClientList());
        }
        new BuBuParser().parseCommand("list-clients q/.*").execute(model);
        assertEquals(List.of(), model.getFilteredClientList());
    }

    @Test
    public void execute_noQuery_resetsFilterAndSortsWithoutChangingRecords() throws Exception {
        model.addClient(zoe);
        model.addClient(amelia);
        ObservableList<Client> displayed = model.getFilteredClientList();
        model.updateFilteredClientList(person -> false);
        CommandResult result = new BuBuParser().parseCommand("list-clients").execute(model);
        assertEquals("Listed all clients.", result.getFeedbackToUser());
        assertEquals(CommandResult.DisplayType.CLIENTS, result.getDisplayType());
        assertEquals(List.of(amelia, zoe), displayed);
        assertEquals(List.of(zoe, amelia), model.getBubuBook().getClientList());
        model.deleteClient(displayed.getFirst());
        assertEquals(List.of(zoe), displayed);
    }

    @Test
    public void execute_query_matchesEachContactFieldIgnoringCase() throws Exception {
        model.addClient(zoe);
        model.addClient(amelia);
        for (String query : List.of("MEL", "7654", "@EXAMPLE.COM", "SUNSET ROAD", "#01-02")) {
            CommandResult result = new BuBuParser().parseCommand("list-clients q/" + query).execute(model);
            assertEquals("Found 1 client(s).", result.getFeedbackToUser());
            assertEquals(CommandResult.DisplayType.CLIENTS, result.getDisplayType());
            assertEquals(List.of(amelia), model.getFilteredClientList());
        }
    }

    @Test
    public void execute_noMatches_successWithEmptyList() {
        model.addClient(amelia);
        assertEquals("No clients match \"Nobody\".", new ListClientsCommand("Nobody")
                .execute(model).getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredClientList());
    }

    @Test
    public void execute_emptyBubu_success() {
        assertEquals("Listed all clients.", new ListClientsCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredClientList());
    }
    //@@author
}

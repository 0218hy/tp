package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ListClientsCommandTest {
    private final ModelManager model = new ModelManager();
    private final Person zoe = new PersonBuilder().withName("Zoe").withPhone("91234567").build();
    private final Person amelia = new PersonBuilder().withName("amelia Tan").withPhone("87654321")
            .withEmail("amelia@example.com").withAddress("10 Sunset Road #01-02").build();

    @Test
    public void execute_noQuery_resetsFilterAndSortsWithoutChangingRecords() throws Exception {
        model.addPerson(zoe);
        model.addPerson(amelia);
        ObservableList<Person> displayed = model.getFilteredPersonList();
        model.updateFilteredPersonList(person -> false);
        assertEquals("Listed all clients.", new AddressBookParser().parseCommand("list-clients")
                .execute(model).getFeedbackToUser());
        assertEquals(List.of(amelia, zoe), displayed);
        assertEquals(List.of(zoe, amelia), model.getAddressBook().getPersonList());
        model.deletePerson(displayed.getFirst());
        assertEquals(List.of(zoe), displayed);
    }

    @Test
    public void execute_query_matchesEachContactFieldIgnoringCase() throws Exception {
        model.addPerson(zoe);
        model.addPerson(amelia);
        for (String query : List.of("MEL", "7654", "@EXAMPLE.COM", "SUNSET ROAD", "#01-02")) {
            assertEquals("Found 1 client(s).", new AddressBookParser().parseCommand("list-clients q/" + query)
                    .execute(model).getFeedbackToUser());
            assertEquals(List.of(amelia), model.getFilteredPersonList());
        }
    }

    @Test
    public void execute_noMatches_successWithEmptyList() {
        model.addPerson(amelia);
        assertEquals("No clients match \"Nobody\".", new ListClientsCommand("Nobody")
                .execute(model).getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyAddressBook_success() {
        assertEquals("Listed all clients.", new ListClientsCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(), model.getFilteredPersonList());
    }
}

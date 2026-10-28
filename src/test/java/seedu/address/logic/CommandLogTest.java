//@@author anderschow
package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.client.EditClientCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.BubuBook;
import seedu.address.model.client.Address;
import seedu.address.model.client.Client;
import seedu.address.model.client.ClientName;
import seedu.address.model.client.Email;
import seedu.address.model.client.Phone;
import seedu.address.testutil.ClientBuilder;

public class CommandLogTest {
    @Test
    public void rejectionReason_invalidContactFields_reportsCategoryWithoutInput() {
        assertEquals("invalid client name format",
                CommandLog.rejectionReason(new ParseException(ClientName.MESSAGE_CONSTRAINTS)));
        assertEquals("invalid email format",
                CommandLog.rejectionReason(new ParseException(Email.MESSAGE_CONSTRAINTS)));
        assertEquals("invalid address format",
                CommandLog.rejectionReason(new ParseException(Address.MESSAGE_CONSTRAINTS)));
        assertEquals("invalid phone format",
                CommandLog.rejectionReason(new ParseException(Phone.MESSAGE_CONSTRAINTS)));
        assertEquals("phone and email already belong to saved clients",
                CommandLog.rejectionReason(new CommandException(EditClientCommand.MESSAGE_DUPLICATE_CLIENT)));
        assertEquals("invalid command syntax or field format",
                CommandLog.rejectionReason(new ParseException("Private user input")));
        assertEquals("record validation or lookup failed",
                CommandLog.rejectionReason(new CommandException("Private contact details")));
    }

    @Test
    public void changedClientFields_multipleClientsChanged_reportsEachFieldOnlyOnce() {
        Client first = new ClientBuilder().withPhone("81112222").withEmail("first@example.com").build();
        Client second = new ClientBuilder().withPhone("82223333").withEmail("second@example.com").build();
        BubuBook before = new BubuBook();
        before.addClient(first);
        before.addClient(second);
        BubuBook after = new BubuBook();
        after.addClient(new ClientBuilder(first).withName("Changed First").withTags("regular").build());
        after.addClient(new ClientBuilder(second).withName("Changed Second").withTags("regular").build());
        assertEquals("; changedFields=[name, tags]", CommandLog.changedClientFields(before, after));
        assertEquals("", CommandLog.changedClientFields(before, before));
    }
}
//@@author

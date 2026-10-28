//@@author anderschow
package seedu.address.logic.commands.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.BubuBook;
import seedu.address.model.ModelManager;
import seedu.address.model.client.Client;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.EditClientDescriptorBuilder;

/** Exercises conflict reporting for both commands, including conflicts hidden by filters. */
public class ClientConflictCommandTest {
    @Test
    public void execute_domainCaseConflict_rejectedEvenWhenHidden() {
        Client existing = new ClientBuilder().withPhone("81112222").withEmail("alice@example.com").build();
        Client target = new ClientBuilder().withPhone("82223333").withEmail("bob@example.com").build();
        for (boolean filtered : new boolean[]{false, true}) {
            ModelManager model = new ModelManager();
            model.addClient(existing);
            model.addClient(target);
            if (filtered) {
                model.updateFilteredClientList(client -> client.equals(target));
            }
            assertCommandFailure(new AddClientCommand(new ClientBuilder().withPhone("83334444")
                    .withEmail("alice@EXAMPLE.COM").build()), model, AddClientCommand.MESSAGE_DUPLICATE_EMAIL);
            assertCommandFailure(new EditClientCommand(target.getPhone(),
                    new EditClientDescriptorBuilder().withEmail("alice@Example.Com").build()),
                    model, AddClientCommand.MESSAGE_DUPLICATE_EMAIL);
        }
    }

    @Test
    public void execute_editOwnDomainCase_preservesEnteredSpelling() throws Exception {
        ModelManager model = new ModelManager();
        Client target = new ClientBuilder().withEmail("alice@example.com").build();
        model.addClient(target);
        new EditClientCommand(target.getPhone(),
                new EditClientDescriptorBuilder().withEmail("alice@Example.COM").build()).execute(model);
        assertEquals("alice@Example.COM", model.getBubuBook().getClientList().getFirst().getEmail().value);
    }

    @Test
    public void execute_conflicts_reportAllConflictingFieldsWithoutChangingRecords() {
        Client target = new ClientBuilder().withPhone("81111111").withEmail("target@example.com").build();
        Client first = new ClientBuilder().withPhone("82222222").withEmail("first@example.com").build();
        Client second = new ClientBuilder().withPhone("83333333").withEmail("second@example.com").build();
        List<Client> proposals = List.of(
                new ClientBuilder(target).withPhone(first.getPhone().value).build(),
                new ClientBuilder(target).withEmail(first.getEmail().value).build(),
                new ClientBuilder(first).build(),
                new ClientBuilder(first).withEmail(second.getEmail().value).build());
        List<String> messages = List.of(AddClientCommand.MESSAGE_DUPLICATE_PHONE,
                AddClientCommand.MESSAGE_DUPLICATE_EMAIL, AddClientCommand.MESSAGE_DUPLICATE_CLIENT,
                AddClientCommand.MESSAGE_DUPLICATE_CLIENT);
        for (boolean filtered : new boolean[]{false, true}) {
            for (int i = 0; i < proposals.size(); i++) {
                ModelManager model = new ModelManager();
                model.addClient(first);
                model.addClient(second);
                if (filtered) {
                    model.updateFilteredClientList(client -> false);
                }
                assertCommandFailure(new AddClientCommand(proposals.get(i)), model, messages.get(i));
                model.addClient(target);
                assertCommandFailure(new EditClientCommand(target.getPhone(),
                        new EditClientDescriptorBuilder(proposals.get(i)).build()), model, messages.get(i));
            }
        }
    }

    @Test
    public void execute_editOwnValuesAndCaseDistinctEmail_success() throws Exception {
        ModelManager model = new ModelManager();
        Client target = new ClientBuilder().withPhone("81111111").withEmail("first@example.com").build();
        model.addClient(target);
        BubuBook before = new BubuBook(model.getBubuBook());
        new EditClientCommand(target.getPhone(), new EditClientDescriptorBuilder(target).build()).execute(model);
        assertEquals(before, model.getBubuBook());
        Client other = new ClientBuilder().withPhone("82222222").withEmail("FIRST@example.com").build();
        new AddClientCommand(other).execute(model);
        new EditClientCommand(other.getPhone(),
                new EditClientDescriptorBuilder().withEmail("First@example.com").build()).execute(model);
        assertEquals("First@example.com", model.getBubuBook().getClientList().get(1).getEmail().value);
    }
}
//@@author

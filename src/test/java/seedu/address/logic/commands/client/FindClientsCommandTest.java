package seedu.address.logic.commands.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_CLIENTS_LISTED_OVERVIEW;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalClients.CARL;
import static seedu.address.testutil.TypicalClients.ELLE;
import static seedu.address.testutil.TypicalClients.FIONA;
import static seedu.address.testutil.TypicalClients.getTypicalClientBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.client.ClientNameContainsKeywordsPredicate;

/**
 * Contains integration tests (interaction with the Model) for {@code FindClientsCommand}.
 */
public class FindClientsCommandTest {
    private Model model = new ModelManager(getTypicalClientBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalClientBook(), new UserPrefs());

    @Test
    public void equals() {
        ClientNameContainsKeywordsPredicate firstPredicate =
                new ClientNameContainsKeywordsPredicate(List.of("first"));
        ClientNameContainsKeywordsPredicate secondPredicate =
                new ClientNameContainsKeywordsPredicate(List.of("second"));

        FindClientsCommand findFirstCommand = new FindClientsCommand(firstPredicate);
        FindClientsCommand findSecondCommand = new FindClientsCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindClientsCommand findFirstCommandCopy = new FindClientsCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different client -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_zeroKeywords_noClientFound() {
        String expectedMessage = String.format(MESSAGE_CLIENTS_LISTED_OVERVIEW, 0);
        ClientNameContainsKeywordsPredicate predicate = preparePredicate(" ");
        FindClientsCommand command = new FindClientsCommand(predicate);
        expectedModel.updateFilteredClientList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredClientList());
    }

    @Test
    public void execute_multipleKeywords_multipleClientsFound() {
        String expectedMessage = String.format(MESSAGE_CLIENTS_LISTED_OVERVIEW, 3);
        ClientNameContainsKeywordsPredicate predicate = preparePredicate("Kurz Elle Kunz");
        FindClientsCommand command = new FindClientsCommand(predicate);
        expectedModel.updateFilteredClientList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredClientList());
    }

    @Test
    public void toStringMethod() {
        ClientNameContainsKeywordsPredicate predicate = new ClientNameContainsKeywordsPredicate(List.of("keyword"));
        FindClientsCommand findCommand = new FindClientsCommand(predicate);
        String expected = FindClientsCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Parses {@code userInput} into a {@code ClientNameContainsKeywordsPredicate}.
     */
    private ClientNameContainsKeywordsPredicate preparePredicate(String userInput) {
        return new ClientNameContainsKeywordsPredicate(List.of(userInput.split("\\s+")));
    }
}

package seedu.address.logic.commands.client;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_CLIENTS;

import java.util.Locale;
import java.util.Objects;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.Model;
import seedu.address.model.client.Client;

/** Lists clients alphabetically, optionally matching a keyword across their contact details. */
public class ListClientsCommand extends Command {
    public static final String COMMAND_WORD = "list-clients";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists all clients or searches their name, phone, email and address.\n"
            + "Parameters: [q/KEYWORD]\nExample: " + COMMAND_WORD + " q/amelia";
    public static final String MESSAGE_SUCCESS = "Listed all clients.";
    public static final String MESSAGE_FOUND = "Found %d client(s).";
    public static final String MESSAGE_NO_MATCH = "No clients match \"%s\".";
    public static final String MESSAGE_INVALID_KEYWORD =
            "Search keyword must be 1-60 characters and use valid text characters.";
    public static final String MESSAGE_DUPLICATE_QUERY = "Each search field may be specified only once.";

    private final String keyword;

    /** Creates a command that lists every client. */
    public ListClientsCommand() {
        keyword = null;
    }

    /** Creates a command with a validated, trimmed keyword. */
    public ListClientsCommand(String keyword) {
        this.keyword = requireNonNull(keyword);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredClientList(keyword == null ? PREDICATE_SHOW_ALL_CLIENTS : this::matches);
        model.sortFilteredClientList();
        if (keyword == null) {
            return new CommandResult(MESSAGE_SUCCESS);
        }
        int count = model.getFilteredClientList().size();
        return new CommandResult(count == 0 ? String.format(MESSAGE_NO_MATCH, keyword)
                : String.format(MESSAGE_FOUND, count));
    }

    private boolean matches(Client person) {
        String query = keyword.toLowerCase(Locale.ROOT);
        return person.getName().fullName.toLowerCase(Locale.ROOT).contains(query)
                || person.getPhone().value.toLowerCase(Locale.ROOT).contains(query)
                || person.getEmail().value.toLowerCase(Locale.ROOT).contains(query)
                || person.getAddress().value.toLowerCase(Locale.ROOT).contains(query);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListClientsCommand otherCommand
                && Objects.equals(keyword, otherCommand.keyword);
    }
}

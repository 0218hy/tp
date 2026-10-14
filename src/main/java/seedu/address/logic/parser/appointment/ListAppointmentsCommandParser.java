package seedu.address.logic.parser.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.appointment.ListAppointmentsCommand;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the parameter-free {@code list-appointments} command. */
public class ListAppointmentsCommandParser implements Parser<ListAppointmentsCommand> {
    @Override
    public ListAppointmentsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ListAppointmentsCommand.MESSAGE_USAGE));
        }
        return new ListAppointmentsCommand();
    }
}

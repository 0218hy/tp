package seedu.address.logic.parser.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_DATE;

import seedu.address.logic.commands.appointment.ListAppointmentsCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.AppointmentDate;

/** Parses {@code list-appointments} with an optional date filter. */
public class ListAppointmentsCommandParser implements Parser<ListAppointmentsCommand> {
    //@@author 0218hy
    @Override
    public ListAppointmentsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            return new ListAppointmentsCommand();
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + args.trim(), PREFIX_DATE);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_DATE);
        if (!arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_DATE).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ListAppointmentsCommand.MESSAGE_USAGE));
        }
        try {
            return new ListAppointmentsCommand(new AppointmentDate(arguments.getValue(PREFIX_DATE).orElseThrow()));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
    //@@author
}

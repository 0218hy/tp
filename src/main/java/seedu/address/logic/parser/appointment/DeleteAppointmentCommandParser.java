package seedu.address.logic.parser.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_TIME;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.appointment.DeleteAppointmentCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.AppointmentDate;

/** Parses arguments into a {@code DeleteAppointmentCommand}. */
public class DeleteAppointmentCommandParser implements Parser<DeleteAppointmentCommand> {
    public static final String MESSAGE_INDEX_AND_DATE_TIME =
            "An appointment index cannot be combined with date/time identifiers.";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public DeleteAppointmentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + args.trim(), PREFIX_DATE, PREFIX_TIME);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_DATE, PREFIX_TIME);

        boolean hasIndex = !arguments.getPreamble().isEmpty();
        boolean hasDate = arguments.getValue(PREFIX_DATE).isPresent();
        boolean hasTime = arguments.getValue(PREFIX_TIME).isPresent();

        if (hasIndex && (hasDate || hasTime)) {
            throw new ParseException(MESSAGE_INDEX_AND_DATE_TIME);
        }

        try {
            if (hasIndex && !hasDate && !hasTime) {
                Index index = ParserUtil.parseIndex(arguments.getPreamble());
                return new DeleteAppointmentCommand(index);
            }
            if (!hasIndex && hasDate && hasTime) {
                AppointmentDate date = new AppointmentDate(arguments.getValue(PREFIX_DATE).get());
                LocalTime time = LocalTime.parse(arguments.getValue(PREFIX_TIME).get(), TIME_FORMATTER);
                return new DeleteAppointmentCommand(date, time);
            }
        } catch (DateTimeParseException | IllegalArgumentException | ParseException e) {
            throw invalidFormat(e);
        }

        throw invalidFormat(null);
    }

    private static ParseException invalidFormat(Exception cause) {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteAppointmentCommand.MESSAGE_USAGE);
        return cause == null ? new ParseException(message) : new ParseException(message, cause);
    }
}

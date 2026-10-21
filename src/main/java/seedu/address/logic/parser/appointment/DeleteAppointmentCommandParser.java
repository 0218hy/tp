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
    public static final String MESSAGE_INVALID_TIME = "Time must be a real HH:mm time (00:00 to 23:59).";

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

        if (hasIndex) {
            Index index = ParserUtil.parseIndex(arguments.getPreamble());
            return new DeleteAppointmentCommand(index);
        }
        if (!hasDate || !hasTime) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    DeleteAppointmentCommand.MESSAGE_USAGE));
        }
        try {
            AppointmentDate date = new AppointmentDate(arguments.getValue(PREFIX_DATE).get());
            LocalTime time = LocalTime.parse(arguments.getValue(PREFIX_TIME).get(), TIME_FORMATTER);
            return new DeleteAppointmentCommand(date, time);
        } catch (DateTimeParseException e) {
            throw new ParseException(MESSAGE_INVALID_TIME, e);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}

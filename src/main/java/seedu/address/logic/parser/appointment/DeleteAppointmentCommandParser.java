package seedu.address.logic.parser.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_TIME;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.appointment.DeleteAppointmentCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.Prefix;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.AppointmentDate;

/** Parses arguments into a {@code DeleteAppointmentCommand}. */
public class DeleteAppointmentCommandParser implements Parser<DeleteAppointmentCommand> {
    public static final String MESSAGE_INDEX_AND_DATE_TIME =
            "Choose either an appointment index or a date and time, not both.";
    public static final String MESSAGE_TARGET_REQUIRED =
            "Choose an appointment index, or provide both d/DATE (DD-MM-YYYY) and t/TIME (HH:mm).";
    public static final String MESSAGE_INVALID_INDEX = "Appointment index must be a positive whole number.";
    public static final String MESSAGE_INVALID_DATE =
            "Invalid appointment date. Use DD-MM-YYYY and enter a real calendar date.";
    public static final String MESSAGE_INVALID_TIME =
            "Invalid appointment time. Use HH:mm from 00:00 to 23:59; any minute is allowed.";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("(?<!\\S)([A-Za-z]+/)");
    private static final Prefix[] PREFIXES = {PREFIX_DATE, PREFIX_TIME};

    @Override
    public DeleteAppointmentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher parameterMatcher = PARAMETER_PATTERN.matcher(args);
        while (parameterMatcher.find()) {
            String parameter = parameterMatcher.group(1);
            if (!isKnownParameter(parameter)) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        DeleteAppointmentCommand.MESSAGE_USAGE));
            }
        }

        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + args.trim(), PREFIXES);
        arguments.verifyNoDuplicatePrefixesFor(PREFIXES);

        boolean hasIndex = !arguments.getPreamble().isEmpty();
        boolean hasDate = arguments.getValue(PREFIX_DATE).isPresent();
        boolean hasTime = arguments.getValue(PREFIX_TIME).isPresent();

        if (hasIndex && (hasDate || hasTime)) {
            throw new ParseException(MESSAGE_INDEX_AND_DATE_TIME);
        }

        if (hasIndex) {
            Index index = parseIndex(arguments.getPreamble());
            return new DeleteAppointmentCommand(index);
        }
        if (!hasDate || !hasTime) {
            throw new ParseException(MESSAGE_TARGET_REQUIRED);
        }
        try {
            AppointmentDate date = new AppointmentDate(arguments.getValue(PREFIX_DATE).get());
            LocalTime time = LocalTime.parse(arguments.getValue(PREFIX_TIME).get(), TIME_FORMATTER);
            return new DeleteAppointmentCommand(date, time);
        } catch (DateTimeParseException e) {
            throw new ParseException(MESSAGE_INVALID_TIME, e);
        } catch (IllegalArgumentException e) {
            throw new ParseException(MESSAGE_INVALID_DATE, e);
        }
    }

    /** Parses an appointment-list index with an appointment-specific error message. */
    private static Index parseIndex(String value) throws ParseException {
        try {
            return ParserUtil.parseIndex(value);
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_INVALID_INDEX, e);
        }
    }

    /** Returns whether the supplied parameter is supported by this command. */
    private static boolean isKnownParameter(String parameter) {
        for (Prefix prefix : PREFIXES) {
            if (prefix.toString().equals(parameter)) {
                return true;
            }
        }
        return false;
    }
}

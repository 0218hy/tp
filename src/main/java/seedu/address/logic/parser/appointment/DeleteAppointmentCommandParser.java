package seedu.address.logic.parser.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_START_TIME;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.appointment.DeleteAppointmentCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.Prefix;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.StartTime;

/** Parses arguments into a {@code DeleteAppointmentCommand}. */
public class DeleteAppointmentCommandParser implements Parser<DeleteAppointmentCommand> {
    public static final String MESSAGE_INDEX_AND_DATE_TIME =
            "Choose either an appointment index or a date and time, not both.";
    public static final String MESSAGE_TARGET_REQUIRED =
            "Choose an appointment index, or provide both d/DATE (DD-MM-YYYY) and st/START_TIME (HH:mm).";
    public static final String MESSAGE_INVALID_INDEX = "Appointment index must be a positive whole number.";
    public static final String MESSAGE_INVALID_DATE =
            "Invalid appointment date. Use DD-MM-YYYY and enter a real calendar date.";
    public static final String MESSAGE_INVALID_TIME =
            "Invalid appointment start time. Use HH:mm (for example, 10:30) from 08:00 to 19:30 "
                    + "in 30-minute intervals.";

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("(?<!\\S)([A-Za-z]+/)");
    private static final Pattern DATE_SHAPED_VALUE = Pattern.compile("\\S+-\\S*");
    private static final Prefix[] PREFIXES = {PREFIX_DATE, PREFIX_START_TIME};

    @Override
    public DeleteAppointmentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher parameterMatcher = PARAMETER_PATTERN.matcher(args);
        while (parameterMatcher.find()) {
            String parameter = parameterMatcher.group(1);
            if (!isKnownParameter(parameter)) {
                throw new ParseException(Messages.getErrorMessageForUnknownParameter(
                        parameter, DeleteAppointmentCommand.MESSAGE_USAGE));
            }
        }

        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + args.trim(), PREFIXES);
        arguments.verifyNoDuplicatePrefixesFor(PREFIXES);

        boolean hasIndex = !arguments.getPreamble().isEmpty();
        boolean hasDate = arguments.getValue(PREFIX_DATE).isPresent();
        boolean hasTime = arguments.getValue(PREFIX_START_TIME).isPresent();

        if (hasIndex && !hasDate && hasTime && looksLikeDate(arguments.getPreamble())) {
            throw new ParseException(Messages.getErrorMessageForMissingParameter(
                    "d/DATE (DD-MM-YYYY)", DeleteAppointmentCommand.MESSAGE_USAGE));
        }
        if (hasIndex && (hasDate || hasTime)) {
            throw new ParseException(MESSAGE_INDEX_AND_DATE_TIME);
        }

        if (hasIndex) {
            Index index = parseIndex(arguments.getPreamble());
            return new DeleteAppointmentCommand(index);
        }
        if (!hasDate && !hasTime) {
            throw new ParseException(MESSAGE_TARGET_REQUIRED);
        }
        if (hasDate != hasTime) {
            String missingParameter = hasDate ? "st/START_TIME (HH:mm)" : "d/DATE (DD-MM-YYYY)";
            throw new ParseException(Messages.getErrorMessageForMissingParameter(
                    missingParameter, DeleteAppointmentCommand.MESSAGE_USAGE));
        }
        AppointmentDate date = parseDate(arguments.getValue(PREFIX_DATE).orElseThrow());
        StartTime startTime = parseStartTime(arguments.getValue(PREFIX_START_TIME).orElseThrow());
        return new DeleteAppointmentCommand(date, startTime.value);
    }

    /** Parses an appointment-list index with an appointment-specific error message. */
    private static Index parseIndex(String value) throws ParseException {
        try {
            return ParserUtil.parseIndex(value);
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_INVALID_INDEX, e);
        }
    }

    /** Parses the date used to identify the appointment. */
    private static AppointmentDate parseDate(String value) throws ParseException {
        try {
            return new AppointmentDate(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(MESSAGE_INVALID_DATE, e);
        }
    }

    /** Parses the start time used to identify the appointment. */
    private static StartTime parseStartTime(String value) throws ParseException {
        try {
            return new StartTime(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(MESSAGE_INVALID_TIME, e);
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

    /** Returns whether a preamble has the shape of a date, without validating its date components. */
    private static boolean looksLikeDate(String preamble) {
        return DATE_SHAPED_VALUE.matcher(preamble).matches();
    }
}

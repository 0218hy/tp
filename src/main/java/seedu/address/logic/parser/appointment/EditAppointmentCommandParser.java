package seedu.address.logic.parser.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_END_TIME;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_NEW_DATE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_NEW_START_TIME;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_SERVICE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_START_TIME;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.appointment.EditAppointmentCommand;
import seedu.address.logic.commands.appointment.EditAppointmentCommand.EditAppointmentDescriptor;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.Prefix;
import seedu.address.logic.parser.client.ClientParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.logic.parser.pet.PetParserUtil;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;

/** Parses arguments into an {@link EditAppointmentCommand}. */
public class EditAppointmentCommandParser implements Parser<EditAppointmentCommand> {
    public static final String MESSAGE_TARGET_REQUIRED =
            "Choose an appointment index, or provide both "
                    + "d/CURRENT_DATE (DD-MM-YYYY) and st/CURRENT_START_TIME (HH:mm).";
    public static final String MESSAGE_INVALID_INDEX = "Appointment index must be a positive whole number.";
    public static final String MESSAGE_INVALID_DATE =
            "Invalid appointment date. Use DD-MM-YYYY and enter a real calendar date.";
    public static final String MESSAGE_INVALID_START_TIME =
            "Invalid start time. Use HH:mm (for example, 10:00); allowed times are 08:00 to 19:30 "
                    + "in 30-minute intervals.";
    public static final String MESSAGE_INVALID_END_TIME =
            "Invalid end time. Use HH:mm (for example, 11:00); allowed times are 08:30 to 20:00 "
                    + "in 30-minute intervals.";

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("(?<!\\S)([A-Za-z]+/)");

    private static final Prefix[] PREFIXES = {
        PREFIX_DATE, PREFIX_START_TIME, PREFIX_OWNER_IDENTIFIER, PREFIX_PET_NAME,
        PREFIX_NEW_DATE, PREFIX_NEW_START_TIME, PREFIX_END_TIME, PREFIX_SERVICE
    };

    /**
     * Parses an edit-appointment command using either a date/start-time target or a displayed index.
     *
     * @param args command arguments following {@code edit-appointment}
     * @return a command containing the target and requested replacement fields
     * @throws ParseException if the target, replacement values, or command structure is invalid
     */
    @Override
    public EditAppointmentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher parameterMatcher = PARAMETER_PATTERN.matcher(args);
        while (parameterMatcher.find()) {
            String parameter = parameterMatcher.group(1);
            if (!isKnownParameter(parameter)) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        EditAppointmentCommand.MESSAGE_USAGE));
            }
        }

        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + args.trim(), PREFIXES);
        arguments.verifyNoDuplicatePrefixesFor(PREFIXES);

        boolean hasIndex = !arguments.getPreamble().isEmpty();
        boolean hasTargetDate = arguments.getValue(PREFIX_DATE).isPresent();
        boolean hasTargetStartTime = arguments.getValue(PREFIX_START_TIME).isPresent();
        if (hasIndex && (hasTargetDate || hasTargetStartTime)) {
            throw new ParseException(EditAppointmentCommand.MESSAGE_INDEX_AND_DATE_TIME);
        }
        if (!hasIndex && (!hasTargetDate || !hasTargetStartTime)) {
            throw new ParseException(MESSAGE_TARGET_REQUIRED);
        }

        EditAppointmentDescriptor descriptor = new EditAppointmentDescriptor();
        if (arguments.getValue(PREFIX_OWNER_IDENTIFIER).isPresent()) {
            descriptor.setOwnerPhone(ClientParserUtil.parsePhone(
                    arguments.getValue(PREFIX_OWNER_IDENTIFIER).orElseThrow()));
        }
        if (arguments.getValue(PREFIX_PET_NAME).isPresent()) {
            descriptor.setPetName(PetParserUtil.parsePetName(
                    arguments.getValue(PREFIX_PET_NAME).orElseThrow()));
        }
        if (arguments.getValue(PREFIX_NEW_DATE).isPresent()) {
            descriptor.setDate(parseDate(arguments.getValue(PREFIX_NEW_DATE).orElseThrow()));
        }
        if (arguments.getValue(PREFIX_NEW_START_TIME).isPresent()) {
            descriptor.setStartTime(parseStartTime(arguments.getValue(PREFIX_NEW_START_TIME).orElseThrow()));
        }
        if (arguments.getValue(PREFIX_END_TIME).isPresent()) {
            descriptor.setEndTime(parseEndTime(arguments.getValue(PREFIX_END_TIME).orElseThrow()));
        }
        if (arguments.getValue(PREFIX_SERVICE).isPresent()) {
            descriptor.setService(parseService(arguments.getValue(PREFIX_SERVICE).orElseThrow()));
        }
        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(EditAppointmentCommand.MESSAGE_NOT_EDITED);
        }

        if (hasIndex) {
            Index index = parseIndex(arguments.getPreamble());
            return new EditAppointmentCommand(index, descriptor);
        }
        return new EditAppointmentCommand(
                parseDate(arguments.getValue(PREFIX_DATE).orElseThrow()),
                parseStartTime(arguments.getValue(PREFIX_START_TIME).orElseThrow()), descriptor);
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

    /** Parses a replacement or target date. */
    private static AppointmentDate parseDate(String value) throws ParseException {
        try {
            return new AppointmentDate(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(MESSAGE_INVALID_DATE, e);
        }
    }

    /** Parses a replacement or target start time. */
    private static StartTime parseStartTime(String value) throws ParseException {
        try {
            return new StartTime(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(MESSAGE_INVALID_START_TIME, e);
        }
    }

    /** Parses a replacement end time. */
    private static EndTime parseEndTime(String value) throws ParseException {
        try {
            return new EndTime(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(MESSAGE_INVALID_END_TIME, e);
        }
    }

    /** Parses a replacement grooming service. */
    private static Service parseService(String value) throws ParseException {
        try {
            return Service.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}

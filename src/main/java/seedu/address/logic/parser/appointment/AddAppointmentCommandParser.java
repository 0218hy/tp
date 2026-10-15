package seedu.address.logic.parser.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_END_TIME;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_SERVICE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_START_TIME;

import java.util.stream.Stream;

import seedu.address.logic.commands.appointment.AddAppointmentCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.Prefix;
import seedu.address.logic.parser.client.ClientParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Phone;

/**
 * Parses add-appointment arguments into a command. Participant, future-start and overlap checks
 * are performed when the command executes, not while parsing.
 */
public class AddAppointmentCommandParser implements Parser<AddAppointmentCommand> {
    @Override
    public AddAppointmentCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Prefix[] prefixes = {PREFIX_OWNER_IDENTIFIER, PREFIX_PET_NAME, PREFIX_DATE,
            PREFIX_START_TIME, PREFIX_END_TIME, PREFIX_SERVICE};
        // Tokenizer recognises prefixes preceded by a space, including the first argument.
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + args.trim(), prefixes);
        if (!arguments.getPreamble().isEmpty()
                || !Stream.of(prefixes).allMatch(prefix -> arguments.getValue(prefix).isPresent())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    AddAppointmentCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(prefixes);

        Phone ownerPhone = ClientParserUtil.parsePhone(arguments.getValue(PREFIX_OWNER_IDENTIFIER).get());
        try {
            Appointment appointment = new Appointment(ownerPhone, arguments.getValue(PREFIX_PET_NAME).get(),
                    new AppointmentDate(arguments.getValue(PREFIX_DATE).get()),
                    new StartTime(arguments.getValue(PREFIX_START_TIME).get()),
                    new EndTime(arguments.getValue(PREFIX_END_TIME).get()),
                    Service.fromString(arguments.getValue(PREFIX_SERVICE).get()));
            return new AddAppointmentCommand(appointment);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}

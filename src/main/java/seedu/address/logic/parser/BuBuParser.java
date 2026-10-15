package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.appointment.AddAppointmentCommand;
import seedu.address.logic.commands.appointment.DeleteAppointmentCommand;
import seedu.address.logic.commands.appointment.ListAppointmentsCommand;
import seedu.address.logic.commands.client.AddClientCommand;
import seedu.address.logic.commands.client.DeleteClientCommand;
import seedu.address.logic.commands.client.EditClientCommand;
import seedu.address.logic.commands.client.FindClientsCommand;
import seedu.address.logic.commands.client.ListClientsCommand;
import seedu.address.logic.commands.client.ListCommand;
import seedu.address.logic.commands.pet.AddPetCommand;
import seedu.address.logic.commands.pet.DeletePetCommand;
import seedu.address.logic.commands.pet.ListPetsCommand;
import seedu.address.logic.parser.appointment.AddAppointmentCommandParser;
import seedu.address.logic.parser.appointment.DeleteAppointmentCommandParser;
import seedu.address.logic.parser.appointment.ListAppointmentsCommandParser;
import seedu.address.logic.parser.client.AddClientCommandParser;
import seedu.address.logic.parser.client.DeleteClientCommandParser;
import seedu.address.logic.parser.client.EditClientCommandParser;
import seedu.address.logic.parser.client.FindClientsCommandParser;
import seedu.address.logic.parser.client.ListClientsCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.logic.parser.pet.AddPetCommandParser;
import seedu.address.logic.parser.pet.DeletePetCommandParser;
import seedu.address.logic.parser.pet.ListPetsCommandParser;

/**
 * Parses user input.
 */
public class BuBuParser {
    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Logger logger = LogsCenter.getLogger(BuBuParser.class);

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case AddAppointmentCommand.COMMAND_WORD -> new AddAppointmentCommandParser().parse(arguments);
            case ListAppointmentsCommand.COMMAND_WORD -> new ListAppointmentsCommandParser().parse(arguments);
            case DeleteAppointmentCommand.COMMAND_WORD -> new DeleteAppointmentCommandParser().parse(arguments);
            case AddClientCommand.COMMAND_WORD -> new AddClientCommandParser().parse(arguments);
            case AddPetCommand.COMMAND_WORD -> new AddPetCommandParser().parse(arguments);
            case EditClientCommand.COMMAND_WORD -> new EditClientCommandParser().parse(arguments);
            case DeleteClientCommand.COMMAND_WORD -> new DeleteClientCommandParser().parse(arguments);
            case DeletePetCommand.COMMAND_WORD -> new DeletePetCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommand();
            case FindClientsCommand.COMMAND_WORD -> new FindClientsCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommand();
            case ListClientsCommand.COMMAND_WORD -> new ListClientsCommandParser().parse(arguments);
            case ListPetsCommand.COMMAND_WORD -> new ListPetsCommandParser().parse(arguments);
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

}

package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_CLIENT;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.appointment.DeleteAppointmentCommand;
import seedu.address.logic.commands.appointment.ListAppointmentsCommand;
import seedu.address.logic.commands.client.AddClientCommand;
import seedu.address.logic.commands.client.DeleteClientCommand;
import seedu.address.logic.commands.client.ListClientsCommand;
import seedu.address.logic.commands.pet.DeletePetCommand;
import seedu.address.logic.commands.pet.ListPetsCommand;
import seedu.address.logic.parser.client.DeleteClientCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.PetOwnerPhoneMatchesPredicate;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.ClientUtil;

public class BuBuParserTest {

    private final BuBuParser parser = new BuBuParser();

    @Test
    public void parseCommand_add() throws Exception {
        Client client = new ClientBuilder().build();
        AddClientCommand command = (AddClientCommand) parser.parseCommand(ClientUtil.getAddClientCommand(client));
        assertEquals(new AddClientCommand(client), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteClientCommand command = (DeleteClientCommand) parser.parseCommand(
                DeleteClientCommand.COMMAND_WORD + " " + INDEX_FIRST_CLIENT.getOneBased());
        assertEquals(new DeleteClientCommand(INDEX_FIRST_CLIENT), command);
    }

    //@@author khangsian0412
    @Test
    public void parseCommand_deletePet() throws Exception {
        DeletePetCommand command = (DeletePetCommand) parser.parseCommand(
                DeletePetCommand.COMMAND_WORD + " p/Milo i/98765432");
        assertEquals(new DeletePetCommand(new PetName("Milo"), new Phone("98765432")), command);
    }
    //@@author

    //@@author chunghean
    @Test
    public void parseCommand_deleteAppointmentByIndex() throws Exception {
        DeleteAppointmentCommand command = (DeleteAppointmentCommand) parser.parseCommand(
                DeleteAppointmentCommand.COMMAND_WORD + " 1");
        assertEquals(new DeleteAppointmentCommand(INDEX_FIRST_CLIENT), command);
    }

    @Test
    public void parseCommand_deleteAppointmentByDateAndTime() throws Exception {
        DeleteAppointmentCommand command = (DeleteAppointmentCommand) parser.parseCommand(
                DeleteAppointmentCommand.COMMAND_WORD + " d/01-01-2020 st/10:30");
        assertEquals(new DeleteAppointmentCommand(new AppointmentDate("01-01-2020"), LocalTime.of(10, 30)),
                command);
    }

    @Test
    public void parseCommand_deleteByPhoneIdentifier() throws Exception {
        DeleteClientCommand command = (DeleteClientCommand) parser.parseCommand(
                DeleteClientCommand.COMMAND_WORD + " i/" + ALICE.getPhone().value);

        assertEquals(new DeleteClientCommand(ALICE.getPhone()), command);
    }

    @Test
    public void parseCommand_deleteWithIndexAndPhoneIdentifier_throwsParseException() {
        assertThrows(ParseException.class,
                DeleteClientCommandParser.MESSAGE_INDEX_AND_IDENTIFIER, ()
                        -> parser.parseCommand(DeleteClientCommand.COMMAND_WORD
                        + " " + INDEX_FIRST_CLIENT.getOneBased() + " i/" + ALICE.getPhone().value));
    }

    @Test
    public void parseCommand_deleteWithoutIdentifier_throwsParseException() {
        assertThrows(ParseException.class,
                DeleteClientCommandParser.MESSAGE_TARGET_REQUIRED, ()
                        -> parser.parseCommand(DeleteClientCommand.COMMAND_WORD));
    }

    @Test
    public void parseCommand_deleteWithInvalidPhoneIdentifier_throwsParseException() {
        assertThrows(ParseException.class,
                Phone.MESSAGE_CONSTRAINTS, ()
                        -> parser.parseCommand(DeleteClientCommand.COMMAND_WORD + " i/12"));
    }
    //@@author

    //@@author 0218hy
    @Test
    public void parseCommand_edit_throwsUnknownCommand() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("edit"));
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () ->
                parser.parseCommand("edit 1 n/Updated Client i/91234567"));
    }
    //@@author

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    //@@author anderschow
    @Test
    public void parseCommand_removedClientCommands_throwsUnknownCommand() {
        for (String input : List.of("find", "find Alex", "list", "list q/Alex")) {
            assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand(input));
        }
    }
    //@@author

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_listClients() throws Exception {
        assertTrue(parser.parseCommand(ListClientsCommand.COMMAND_WORD) instanceof ListClientsCommand);
        assertTrue(parser.parseCommand(" " + ListClientsCommand.COMMAND_WORD + " ") instanceof ListClientsCommand);
    }

    //@@author 0218hy
    @Test
    public void parseCommand_listAppointments() throws Exception {
        assertTrue(parser.parseCommand("list-appointments") instanceof ListAppointmentsCommand);
        assertTrue(parser.parseCommand("  list-appointments  \t") instanceof ListAppointmentsCommand);
        assertTrue(parser.parseCommand("list-appointments d/18-09-2026") instanceof ListAppointmentsCommand);
    }

    @Test
    public void parseCommand_listAppointmentsWithArguments_throwsParseException() {
        for (String arguments : List.of("unexpected", "18-09-2026", "i/91234567", "p/BuBu")) {
            String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListAppointmentsCommand.MESSAGE_USAGE);
            assertThrows(ParseException.class, message, () -> parser.parseCommand("list-appointments " + arguments));
        }
    }

    @Test
    public void parseCommand_listPets() throws Exception {
        assertEquals(new ListPetsCommand(null), parser.parseCommand(ListPetsCommand.COMMAND_WORD));
        assertEquals(new ListPetsCommand(new PetOwnerPhoneMatchesPredicate(new Phone("91234567"))),
                parser.parseCommand(ListPetsCommand.COMMAND_WORD + " i/91234567"));
    }
    //@@author

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}

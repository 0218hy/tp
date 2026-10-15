package seedu.address.logic.parser.appointment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.appointment.DeleteAppointmentCommand;
import seedu.address.model.appointment.AppointmentDate;

public class DeleteAppointmentCommandParserTest {
    private static final String VALID_DATE_TIME = "d/01-01-2020 t/10:30";
    private final DeleteAppointmentCommandParser parser = new DeleteAppointmentCommandParser();

    @Test
    public void parse_validIndex_success() {
        assertParseSuccess(parser, "1", new DeleteAppointmentCommand(Index.fromOneBased(1)));
    }

    @Test
    public void parse_validDateAndTime_success() {
        assertParseSuccess(parser, VALID_DATE_TIME,
                new DeleteAppointmentCommand(new AppointmentDate("01-01-2020"), LocalTime.of(10, 30)));
    }

    @Test
    public void parse_reorderedFieldsAndExtraSpaces_success() {
        assertParseSuccess(parser, "  t/10:30   d/01-01-2020  ",
                new DeleteAppointmentCommand(new AppointmentDate("01-01-2020"), LocalTime.of(10, 30)));
    }

    @Test
    public void parse_missingArguments_failure() {
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteAppointmentCommand.MESSAGE_USAGE);
        for (String args : new String[]{"", "d/01-01-2020", "t/10:30", "unexpected"}) {
            assertParseFailure(parser, args, usage);
        }
    }

    @Test
    public void parse_indexAndDateTime_failure() {
        assertParseFailure(parser, "1 d/01-01-2020 t/10:30",
                DeleteAppointmentCommandParser.MESSAGE_INDEX_AND_DATE_TIME);
    }

    @Test
    public void parse_indexAndTime_failure() {
        assertParseFailure(parser, "1 t/10:30",
                DeleteAppointmentCommandParser.MESSAGE_INDEX_AND_DATE_TIME);
    }

    @Test
    public void parse_invalidDateOrTime_failure() {
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteAppointmentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "d/31-02-2020 t/10:30", usage);
        assertParseFailure(parser, "d/01-01-2020 t/25:00", usage);
        assertParseFailure(parser, "d/01-01-2020 t/10:3", usage);
    }

    @Test
    public void parse_duplicatePrefix_failure() {
        assertParseFailure(parser, VALID_DATE_TIME + " d/02-01-2020",
                Messages.getErrorMessageForDuplicatePrefixes(AppointmentCliSyntax.PREFIX_DATE));
        assertParseFailure(parser, VALID_DATE_TIME + " t/11:30",
                Messages.getErrorMessageForDuplicatePrefixes(AppointmentCliSyntax.PREFIX_TIME));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}

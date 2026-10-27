package seedu.address.logic.parser.appointment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;

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
        for (String args : new String[]{"", "d/01-01-2020", "t/10:30"}) {
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

    //@@author 0218hy
    @Test
    public void parse_invalidDate_reportsDateConstraints() {
        for (String date : new String[]{"31-02-2020", "29-02-2027", "1-01-2020", ""}) {
            assertParseFailure(parser, "d/" + date + " t/10:30", AppointmentDate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidTime_reportsTimeConstraints() {
        for (String time : new String[]{"25:00", "24:00", "10:60", "10:3", "10:30:00", ""}) {
            assertParseFailure(parser, "d/01-01-2020 t/" + time,
                    DeleteAppointmentCommandParser.MESSAGE_INVALID_TIME);
        }
    }

    @Test
    public void parse_invalidIndex_reportsIndexConstraints() {
        for (String index : new String[]{"0", "-1", "1.5", "2147483648", "unexpected"}) {
            assertParseFailure(parser, index, MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_validTimeBoundariesAndNonHalfHour_success() {
        for (String time : new String[]{"00:00", "23:59", "10:17"}) {
            assertParseSuccess(parser, "d/01-01-2020 t/" + time,
                    new DeleteAppointmentCommand(new AppointmentDate("01-01-2020"), LocalTime.parse(time)));
        }
    }
    //@@author

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

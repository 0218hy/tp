package seedu.address.logic.parser.appointment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.getErrorMessageForMissingParameter;
import static seedu.address.logic.Messages.getErrorMessageForUnknownParameter;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.appointment.DeleteAppointmentCommand;
import seedu.address.model.appointment.AppointmentDate;

public class DeleteAppointmentCommandParserTest {
    private static final String VALID_DATE_TIME = "d/01-01-2020 st/10:30";
    private final DeleteAppointmentCommandParser parser = new DeleteAppointmentCommandParser();

    //@@author chunghean
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
        assertParseSuccess(parser, "  st/10:30   d/01-01-2020  ",
                new DeleteAppointmentCommand(new AppointmentDate("01-01-2020"), LocalTime.of(10, 30)));
    }

    @Test
    public void parse_missingArguments_failure() {
        assertParseFailure(parser, "", DeleteAppointmentCommandParser.MESSAGE_TARGET_REQUIRED);
        assertParseFailure(parser, "d/01-01-2020",
                getErrorMessageForMissingParameter("st/START_TIME (HH:mm)",
                        DeleteAppointmentCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "st/10:30",
                getErrorMessageForMissingParameter("d/DATE (DD-MM-YYYY)",
                        DeleteAppointmentCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_dateLikePreamble_reportsMissingDatePrefix() {
        assertParseFailure(parser, "12-12-1234 st/12:00",
                getErrorMessageForMissingParameter("d/DATE (DD-MM-YYYY)",
                        DeleteAppointmentCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "12- st/12:00",
                getErrorMessageForMissingParameter("d/DATE (DD-MM-YYYY)",
                        DeleteAppointmentCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_indexAndDateTime_failure() {
        assertParseFailure(parser, "1 d/01-01-2020 st/10:30",
                DeleteAppointmentCommandParser.MESSAGE_INDEX_AND_DATE_TIME);
    }

    @Test
    public void parse_indexAndTime_failure() {
        assertParseFailure(parser, "1 st/10:30",
                DeleteAppointmentCommandParser.MESSAGE_INDEX_AND_DATE_TIME);
    }
    //@@author

    //@@author 0218hy
    @Test
    public void parse_invalidDate_reportsDateConstraints() {
        for (String date : new String[]{"31-02-2020", "29-02-2027", "1-01-2020", "12-", ""}) {
            assertParseFailure(parser, "d/" + date + " st/10:30",
                    DeleteAppointmentCommandParser.MESSAGE_INVALID_DATE);
        }
    }

    @Test
    public void parse_invalidTime_reportsTimeConstraints() {
        for (String time : new String[]{"25:00", "24:00", "10:60", "10:3", "10:30:00", ""}) {
            assertParseFailure(parser, "d/01-01-2020 st/" + time,
                    DeleteAppointmentCommandParser.MESSAGE_INVALID_TIME);
        }
    }

    @Test
    public void parse_invalidIndex_reportsIndexConstraints() {
        for (String index : new String[]{"0", "-1", "1.5", "2147483648", "unexpected"}) {
            assertParseFailure(parser, index, DeleteAppointmentCommandParser.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_validStartTimeBoundaries_success() {
        for (String time : new String[]{"08:00", "19:30", "10:30"}) {
            assertParseSuccess(parser, "d/01-01-2020 st/" + time,
                    new DeleteAppointmentCommand(new AppointmentDate("01-01-2020"), LocalTime.parse(time)));
        }
    }

    @Test
    public void parse_invalidStartTimeRangeOrInterval_failure() {
        for (String time : new String[]{"00:00", "23:59", "10:17"}) {
            assertParseFailure(parser, "d/01-01-2020 st/" + time,
                    DeleteAppointmentCommandParser.MESSAGE_INVALID_TIME);
        }
    }
    //@@author

    //@@author chunghean
    @Test
    public void parse_duplicatePrefix_failure() {
        assertParseFailure(parser, VALID_DATE_TIME + " d/02-01-2020",
                Messages.getErrorMessageForDuplicatePrefixes(AppointmentCliSyntax.PREFIX_DATE));
        assertParseFailure(parser, VALID_DATE_TIME + " st/11:30",
                Messages.getErrorMessageForDuplicatePrefixes(AppointmentCliSyntax.PREFIX_START_TIME));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
    //@@author

    @Test
    public void parse_unknownParameter_failure() {
        assertParseFailure(parser, "1 t/10:30",
                getErrorMessageForUnknownParameter("t/", DeleteAppointmentCommand.MESSAGE_USAGE));
    }
}

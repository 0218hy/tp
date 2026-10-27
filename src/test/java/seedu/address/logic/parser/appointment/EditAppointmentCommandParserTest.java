package seedu.address.logic.parser.appointment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.Messages.getErrorMessageForMissingParameter;
import static seedu.address.logic.Messages.getErrorMessageForUnknownParameter;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_END_TIME;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.appointment.EditAppointmentCommand;
import seedu.address.logic.commands.appointment.EditAppointmentCommand.EditAppointmentDescriptor;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.PetName;

public class EditAppointmentCommandParserTest {
    private final EditAppointmentCommandParser parser = new EditAppointmentCommandParser();

    @Test
    public void parse_byDateAndStartTime_success() {
        EditAppointmentDescriptor descriptor = new EditAppointmentDescriptor();
        descriptor.setEndTime(new EndTime("12:00"));

        assertParseSuccess(parser, "d/18-09-2027 st/10:00 et/12:00",
                new EditAppointmentCommand(new AppointmentDate("18-09-2027"), new StartTime("10:00"), descriptor));
    }

    @Test
    public void parse_byIndex_success() {
        EditAppointmentDescriptor descriptor = new EditAppointmentDescriptor();
        descriptor.setService(Service.FULL_GROOM);

        assertParseSuccess(parser, "1 s/Full groom",
                new EditAppointmentCommand(Index.fromOneBased(1), descriptor));
    }

    @Test
    public void parse_allEditableFields_success() {
        EditAppointmentDescriptor descriptor = new EditAppointmentDescriptor();
        descriptor.setOwnerPhone(new Phone("91234567"));
        descriptor.setPetName(new PetName("Mochi"));
        descriptor.setDate(new AppointmentDate("19-09-2027"));
        descriptor.setStartTime(new StartTime("11:00"));
        descriptor.setEndTime(new EndTime("12:00"));
        descriptor.setService(Service.NAIL_TRIM);

        assertParseSuccess(parser, "d/18-09-2027 st/10:00 i/91234567 p/Mochi "
                        + "nd/19-09-2027 nst/11:00 et/12:00 s/Nail trim",
                new EditAppointmentCommand(new AppointmentDate("18-09-2027"), new StartTime("10:00"), descriptor));
    }

    @Test
    public void parse_missingTargetOrEditField_failure() {
        assertParseFailure(parser, "s/Full groom", EditAppointmentCommandParser.MESSAGE_TARGET_REQUIRED);
        assertParseFailure(parser, "d/18-09-2027 s/Full groom",
                getErrorMessageForMissingParameter("st/CURRENT_START_TIME (HH:mm)",
                        EditAppointmentCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "st/10:00 s/Full groom",
                getErrorMessageForMissingParameter("d/CURRENT_DATE (DD-MM-YYYY)",
                        EditAppointmentCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1", EditAppointmentCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, "d/18-09-2027 st/10:00", EditAppointmentCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_unprefixedStartTime_reportsMissingStartTimePrefix() {
        assertParseFailure(parser, "d/12-12-2026 12:00",
                getErrorMessageForMissingParameter("st/CURRENT_START_TIME (HH:mm)",
                        EditAppointmentCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_dateLikePreamble_reportsMissingDatePrefix() {
        assertParseFailure(parser, "12-12-2024 st/12:00 i/91239123",
                getErrorMessageForMissingParameter("d/CURRENT_DATE (DD-MM-YYYY)",
                        EditAppointmentCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_indexAndDateTime_failure() {
        assertParseFailure(parser, "1 d/18-09-2027 st/10:00",
                EditAppointmentCommand.MESSAGE_INDEX_AND_DATE_TIME);
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, "0 s/Full groom", EditAppointmentCommandParser.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "d/31-02-2027 st/10:00 et/12:00",
                EditAppointmentCommandParser.MESSAGE_INVALID_DATE);
        assertParseFailure(parser, "d/18-09-2027 st/07:00 et/12:00",
                EditAppointmentCommandParser.MESSAGE_INVALID_START_TIME);
        assertParseFailure(parser, "d/18-09-2027 st/10:00 et/10:15",
                EditAppointmentCommandParser.MESSAGE_INVALID_END_TIME);
        assertParseFailure(parser, "d/18-09-2027 st/10:00 i/1234 et/12:00",
                Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "d/18-09-2027 st/10:00 p/* et/12:00",
                PetName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "d/18-09-2027 st/10:00 nd/31-02-2027 et/12:00",
                EditAppointmentCommandParser.MESSAGE_INVALID_DATE);
        assertParseFailure(parser, "d/18-09-2027 st/10:00 nst/07:00 et/12:00",
                EditAppointmentCommandParser.MESSAGE_INVALID_START_TIME);
        assertParseFailure(parser, "d/18-09-2027 st/10:00 s/Unknown et/12:00",
                Service.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicatePrefix_failure() {
        assertParseFailure(parser, "d/18-09-2027 st/10:00 et/12:00 et/13:00",
                getErrorMessageForDuplicatePrefixes(PREFIX_END_TIME));
    }

    @Test
    public void parse_invalidPreamble_failure() {
        assertParseFailure(parser, "not-an-index s/Full groom", EditAppointmentCommandParser.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_unknownParameter_failure() {
        assertParseFailure(parser, "1 t/10:00",
                getErrorMessageForUnknownParameter("t/", EditAppointmentCommand.MESSAGE_USAGE));
    }
}

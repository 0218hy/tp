package seedu.address.logic.parser.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_DATE;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.appointment.ListAppointmentsCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Phone;

public class ListAppointmentsCommandParserTest {
    private final ListAppointmentsCommandParser parser = new ListAppointmentsCommandParser();

    @Test
    public void parse_noArguments_listsOngoingAndFuture() throws Exception {
        for (String args : List.of("", " \t ")) {
            assertEquals(ListAppointmentsCommand.MESSAGE_NO_APPOINTMENTS,
                    parser.parse(args).execute(new ModelManager()).getFeedbackToUser());
        }
    }

    @Test
    public void parse_dateFilter_selectsDateIncludingPastAppointments() throws Exception {
        Appointment appointment = createAppointment(new Phone("91234567"), "BuBu",
                new AppointmentDate("18-09-2020"), new StartTime("10:00"), new EndTime("11:00"),
                Service.FULL_GROOM);
        ModelManager model = new ModelManager();
        model.setAppointments(List.of(appointment));
        for (String args : List.of("d/18-09-2020", "  d/ 18-09-2020 \t")) {
            parser.parse(args).execute(model);
            assertEquals(List.of(appointment), model.getFilteredAppointmentList());
        }
    }

    @Test
    public void parse_invalidOrMissingDate_failure() {
        for (String args : List.of("d/", "d/31-02-2026", "d/2026-10-07", "d/7-10-2026",
                "d/07-10-2026 extra", "d/07-10-2026 i/91234567", "d/07-10-2026 p/BuBu")) {
            assertParseFailure(parser, args, AppointmentDate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unsupportedArguments_failure() {
        for (String args : List.of("i/91234567", "p/BuBu", "07-10-2026", "extra d/07-10-2026",
                "i/91234567 d/07-10-2026", "p/BuBu d/07-10-2026")) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListAppointmentsCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_duplicateDate_failure() {
        assertParseFailure(parser, "d/07-10-2026 d/08-10-2026",
                getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}

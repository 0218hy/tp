package seedu.address.logic.parser.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.appointment.ScheduleCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.Prefix;
import seedu.address.model.ModelManager;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.AppointmentParticipantLookup;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.person.Phone;

public class ScheduleCommandParserTest {
    private static final String VALID_ARGS = "i/91234567 p/BuBu d/01-01-9999 st/10:00 et/11:00 s/Full groom";
    private static final List<String> FIELDS = List.of("i/91234567", "p/BuBu", "d/01-01-9999",
            "st/10:00", "et/11:00", "s/Full groom");
    private static final List<Prefix> PREFIXES = List.of(AppointmentCliSyntax.PREFIX_OWNER_IDENTIFIER,
            AppointmentCliSyntax.PREFIX_PET_NAME, AppointmentCliSyntax.PREFIX_DATE,
            AppointmentCliSyntax.PREFIX_START_TIME, AppointmentCliSyntax.PREFIX_END_TIME,
            AppointmentCliSyntax.PREFIX_SERVICE);

    private final ParticipantLookupStub lookup = new ParticipantLookupStub();
    private final ScheduleCommandParser parser = new ScheduleCommandParser(lookup);

    @Test
    public void parse_validArgumentsWithOrWithoutLeadingSpace_success() {
        ScheduleCommand expected = expected("BuBu", "01-01-9999", "10:00", "11:00", Service.FULL_GROOM);
        assertParseSuccess(parser, VALID_ARGS, expected);
        assertParseSuccess(parser, "  " + VALID_ARGS + "  ", expected);
    }

    @Test
    public void parse_reorderedFieldsAndExtraSpaces_success() {
        assertParseSuccess(parser,
                " s/ full   GROOM   et/11:00 st/10:00 p/ BuBu   Junior d/01-01-9999 i/91234567 ",
                expected("BuBu Junior", "01-01-9999", "10:00", "11:00", Service.FULL_GROOM));
    }

    @Test
    public void parse_eachSupportedService_success() {
        for (Service service : Service.values()) {
            assertParseSuccess(parser, VALID_ARGS.replace("Full groom", service.toString()),
                    expected("BuBu", "01-01-9999", "10:00", "11:00", service));
        }
    }

    @Test
    public void parse_boundaryTimesAndLeapDate_success() {
        assertParseSuccess(parser, VALID_ARGS.replace("10:00", "08:00").replace("11:00", "08:30"),
                expected("BuBu", "01-01-9999", "08:00", "08:30", Service.FULL_GROOM));
        assertParseSuccess(parser, VALID_ARGS.replace("10:00", "19:30").replace("11:00", "20:00"),
                expected("BuBu", "01-01-9999", "19:30", "20:00", Service.FULL_GROOM));
        assertParseSuccess(parser, VALID_ARGS.replace("01-01-9999", "29-02-2028"),
                expected("BuBu", "29-02-2028", "10:00", "11:00", Service.FULL_GROOM));
    }

    @Test
    public void parse_missingFieldsOrPreamble_failure() {
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ScheduleCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", usage);
        assertParseFailure(parser, "  ", usage);
        assertParseFailure(parser, "unexpected " + VALID_ARGS, usage);
        for (String field : FIELDS) {
            assertParseFailure(parser, VALID_ARGS.replace(field, ""), usage);
        }
    }

    @Test
    public void parse_duplicateFieldsEvenWithSameValues_failure() {
        for (int i = 0; i < FIELDS.size(); i++) {
            String message = Messages.getErrorMessageForDuplicatePrefixes(PREFIXES.get(i));
            assertParseFailure(parser, VALID_ARGS + " " + FIELDS.get(i), message);
            assertParseFailure(parser, VALID_ARGS + " " + PREFIXES.get(i) + "different", message);
        }
    }

    @Test
    public void parse_invalidOrEmptyOwnerPhone_failure() {
        for (String phone : List.of("", "abc", "12", "9123 4567", "+6591234567")) {
            assertInvalid("91234567", phone, Phone.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidOrEmptyPetName_failure() {
        for (String name : List.of("", "   ", "Bu/Bu", "a".repeat(41))) {
            assertInvalid("BuBu", name, Appointment.MESSAGE_PET_NAME_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidOrEmptyDate_failure() {
        for (String date : List.of("", "31-02-2026", "29-02-2027", "1-01-9999", "9999-01-01")) {
            assertInvalid("01-01-9999", date, AppointmentDate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidOrEmptyStartTime_failure() {
        for (String start : List.of("", "07:30", "20:00", "10:15", "9:00", "24:00")) {
            assertInvalid("10:00", start, StartTime.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidEndTimeOrDuration_failure() {
        for (String end : List.of("", "08:00", "20:30", "11:15", "9:30", "09:30", "10:00")) {
            assertInvalid("11:00", end, EndTime.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unknownOrEmptyService_failure() {
        assertInvalid("Full groom", "unknown", Service.MESSAGE_CONSTRAINTS);
        assertInvalid("Full groom", "", Service.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unrecognisedPrefix_failure() {
        assertParseFailure(parser, VALID_ARGS + " x/extra", Service.MESSAGE_CONSTRAINTS);
        assertInvalid("BuBu", "BuBu x/extra", Appointment.MESSAGE_PET_NAME_CONSTRAINTS);
    }

    @Test
    public void parse_pastDate_defersExecutionChecks() {
        lookup.ownerExists = false;
        assertParseSuccess(parser, VALID_ARGS.replace("01-01-9999", "01-01-2000"),
                expected("BuBu", "01-01-2000", "10:00", "11:00", Service.FULL_GROOM));
        assertEquals(0, lookup.calls);
    }

    @Test
    public void parseAndExecute_validArguments_addsExpectedAppointment() throws Exception {
        ModelManager model = new ModelManager();
        parser.parse(VALID_ARGS).execute(model);
        Appointment appointment = new Appointment(new Phone("91234567"), "BuBu", new AppointmentDate("01-01-9999"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM);
        assertEquals(List.of(appointment), model.getAppointmentBook().getAppointmentList());
        assertEquals(2, lookup.calls);
    }

    @Test
    public void parseAndExecute_missingOwner_usesSuppliedLookup() throws Exception {
        ScheduleCommand command = parser.parse(VALID_ARGS);
        lookup.ownerExists = false;
        ModelManager model = new ModelManager();
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(ScheduleCommand.MESSAGE_OWNER_NOT_FOUND, exception.getMessage());
        assertTrue(model.getAppointmentBook().getAppointmentList().isEmpty());
    }

    @Test
    public void constructorAndParse_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new ScheduleCommandParser(null));
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    private ScheduleCommand expected(String petName, String date, String start, String end, Service service) {
        return new ScheduleCommand(new Appointment(new Phone("91234567"), petName, new AppointmentDate(date),
                new StartTime(start), new EndTime(end), service), lookup);
    }

    private void assertInvalid(String field, String replacement, String message) {
        assertParseFailure(parser, VALID_ARGS.replace(field, replacement), message);
    }

    /**
     * Test-only lookup that records whether parsing prematurely queries participants.
     */
    private static class ParticipantLookupStub implements AppointmentParticipantLookup {
        private boolean ownerExists = true;
        private int calls;

        @Override
        public boolean hasOwner(Phone ownerPhone) {
            calls++;
            return ownerExists && ownerPhone.equals(new Phone("91234567"));
        }

        @Override
        public boolean hasPet(Phone ownerPhone, String petName) {
            calls++;
            return ownerPhone.equals(new Phone("91234567")) && petName.equals("BuBu");
        }
    }
}

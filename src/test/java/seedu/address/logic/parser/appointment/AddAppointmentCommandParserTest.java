package seedu.address.logic.parser.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.appointment.AddAppointmentCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.Prefix;
import seedu.address.model.ModelManager;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.PetName;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

public class AddAppointmentCommandParserTest {
    private static final String VALID_ARGS = "i/91234567 p/BuBu d/01-01-9999 st/10:00 et/11:00 s/Full groom";
    private static final List<String> FIELDS = List.of("i/91234567", "p/BuBu", "d/01-01-9999",
            "st/10:00", "et/11:00", "s/Full groom");
    private static final List<Prefix> PREFIXES = List.of(AppointmentCliSyntax.PREFIX_OWNER_IDENTIFIER,
            AppointmentCliSyntax.PREFIX_PET_NAME, AppointmentCliSyntax.PREFIX_DATE,
            AppointmentCliSyntax.PREFIX_START_TIME, AppointmentCliSyntax.PREFIX_END_TIME,
            AppointmentCliSyntax.PREFIX_SERVICE);

    private final AddAppointmentCommandParser parser = new AddAppointmentCommandParser();

    //@@author 0218hy
    @Test
    public void parse_validArgumentsWithOrWithoutLeadingSpace_success() {
        AddAppointmentCommand expected = expected("BuBu", "01-01-9999", "10:00", "11:00", Service.FULL_GROOM);
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
        // EP: each enum value has its own expected command value; all services must remain reachable via parsing.
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
        // EPs: empty command, unexpected preamble, and each compulsory field missing independently.
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddAppointmentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", usage);
        assertParseFailure(parser, "  ", usage);
        assertParseFailure(parser, "unexpected " + VALID_ARGS, usage);
        for (String field : FIELDS) {
            assertParseFailure(parser, VALID_ARGS.replace(field, ""), usage);
        }
    }

    @Test
    public void parse_duplicateFieldsEvenWithSameValues_failure() {
        // EPs: repeated identical/conflicting values. Reject both instead of silently keeping one occurrence.
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
            assertInvalid("BuBu", name, PetName.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidOrEmptyDate_failure() {
        // EPs: missing value, impossible calendar date, malformed format. Detailed BVA is in AppointmentDateTest.
        for (String date : List.of("", "31-02-2026", "1-01-9999")) {
            assertInvalid("01-01-9999", date, AppointmentDate.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidOrEmptyStartTime_failure() {
        // Parser responsibility: report the right constraint for missing, out-of-range, off-grid, malformed values.
        for (String start : List.of("", "07:30", "10:15", "9:00")) {
            assertInvalid("10:00", start, StartTime.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidOrEmptyEndTime_failure() {
        // Same parser EPs as start time; detailed working-hour boundaries belong to EndTimeTest.
        for (String end : List.of("", "20:30", "11:15", "9:30")) {
            assertInvalid("11:00", end, EndTime.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidDuration_failure() {
        // #124: each field is valid alone, but the pair has negative/zero duration. Change only the end time.
        for (String end : List.of("09:30", "10:00")) {
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
        assertInvalid("BuBu", "BuBu x/extra", PetName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_pastDate_defersExecutionChecks() {
        assertParseSuccess(parser, VALID_ARGS.replace("01-01-9999", "01-01-2000"),
                expected("BuBu", "01-01-2000", "10:00", "11:00", Service.FULL_GROOM));
    }

    @Test
    public void parseAndExecute_validArguments_addsExpectedAppointment() throws Exception {
        ModelManager model = new ModelManager();
        Client owner = new ClientBuilder().withPhone("91234567").build();
        model.addClient(owner);
        model.addPet(new PetBuilder(owner).withName("BuBu").build());
        parser.parse(VALID_ARGS).execute(model);
        Appointment appointment = createAppointment(new Phone("91234567"), "BuBu", new AppointmentDate("01-01-9999"),
                new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM);
        assertEquals(List.of(appointment), model.getBubuBook().getAppointmentList());
    }

    @Test
    public void parseAndExecute_missingOwner_rejectsAppointment() throws Exception {
        AddAppointmentCommand command = parser.parse(VALID_ARGS);
        ModelManager model = new ModelManager();
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(AddAppointmentCommand.MESSAGE_OWNER_NOT_FOUND, exception.getMessage());
        assertTrue(model.getBubuBook().getAppointmentList().isEmpty());
    }

    @Test
    public void parse_nullArguments_rejected() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    private AddAppointmentCommand expected(String petName, String date, String start, String end,
            Service service) {
        return new AddAppointmentCommand(new Phone("91234567"), petName, new AppointmentDate(date),
                new StartTime(start), new EndTime(end), service);
    }

    private void assertInvalid(String field, String replacement, String message) {
        assertParseFailure(parser, VALID_ARGS.replace(field, replacement), message);
    }
    //@@author

}

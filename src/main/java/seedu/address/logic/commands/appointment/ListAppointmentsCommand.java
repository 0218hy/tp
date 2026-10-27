package seedu.address.logic.commands.appointment;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;

/**
 * Lists ongoing and future appointments in chronological order without changing saved records.
 */
public class ListAppointmentsCommand extends Command {
    public static final String COMMAND_WORD = "list-appointments";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists ongoing and future appointments in chronological order (Singapore time).\n"
            + "This command takes no parameters.\nExample: " + COMMAND_WORD;
    public static final String MESSAGE_SUCCESS = "Ongoing or future appointments found: %d.";
    public static final String MESSAGE_NO_APPOINTMENTS = "No ongoing or future appointments.";
    private static final ZoneId SINGAPORE_ZONE = ZoneId.of("Asia/Singapore");
    private final Clock clock;

    //@@author 0218hy
    /** Creates a command using the current time in Singapore. */
    public ListAppointmentsCommand() {
        this(Clock.system(SINGAPORE_ZONE));
    }
    //@@author

    //@@author 0218hy
    /** Creates a command with a supplied clock for deterministic time-based tests. */
    public ListAppointmentsCommand(Clock clock) {
        this.clock = requireNonNull(clock).withZone(SINGAPORE_ZONE);
    }
    //@@author

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDateTime now = LocalDateTime.now(clock);
        model.updateFilteredAppointmentList(appointment ->
                        appointment.getDate().value.atTime(appointment.getEndTime().value)
                        .isAfter(now));
        List<Appointment> appointments = model.getFilteredAppointmentList();
        if (appointments.isEmpty()) {
            return new CommandResult(MESSAGE_NO_APPOINTMENTS, CommandResult.DisplayType.APPOINTMENTS);
        }

        return new CommandResult(String.format(MESSAGE_SUCCESS, appointments.size()),
                CommandResult.DisplayType.APPOINTMENTS);
    }
}

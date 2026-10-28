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
import seedu.address.model.appointment.AppointmentDate;

/**
 * Lists ongoing and future appointments, or all appointments on a date, in chronological order.
 */
public class ListAppointmentsCommand extends Command {
    public static final String COMMAND_WORD = "list-appointments";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists ongoing and future appointments in chronological order (Singapore time).\n"
            + "Use d/DD-MM-YYYY to show all appointments on that date, including completed appointments.\n"
            + "Parameters: [d/DD-MM-YYYY]\nExample: " + COMMAND_WORD + " d/18-09-2027";
    public static final String MESSAGE_SUCCESS = "Ongoing or future appointments found: %d.";
    public static final String MESSAGE_NO_APPOINTMENTS = "No ongoing or future appointments.";
    public static final String MESSAGE_DATE_SUCCESS = "Appointments on %s found: %d.";
    public static final String MESSAGE_NO_APPOINTMENTS_ON_DATE = "No appointments on %s.";
    private static final ZoneId SINGAPORE_ZONE = ZoneId.of("Asia/Singapore");
    private final Clock clock;
    private final AppointmentDate date;

    //@@author 0218hy
    /** Creates a command using the current time in Singapore. */
    public ListAppointmentsCommand() {
        this(Clock.system(SINGAPORE_ZONE));
    }

    /** Creates a command with a supplied clock for deterministic time-based tests. */
    public ListAppointmentsCommand(Clock clock) {
        this(null, clock);
    }

    /** Creates a command for all appointments on the supplied date. */
    public ListAppointmentsCommand(AppointmentDate date) {
        this(requireNonNull(date), Clock.system(SINGAPORE_ZONE));
    }

    /** Creates a command with an optional date filter and a supplied clock. */
    public ListAppointmentsCommand(AppointmentDate date, Clock clock) {
        this.date = date;
        this.clock = requireNonNull(clock).withZone(SINGAPORE_ZONE);
    }
    //@@author

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDateTime now = LocalDateTime.now(clock);
        model.updateFilteredAppointmentList(appointment -> date != null
                        ? appointment.getDate().equals(date)
                        : appointment.getDate().value.atTime(appointment.getEndTime().value)
                        .isAfter(now));
        List<Appointment> appointments = model.getFilteredAppointmentList();
        if (date != null) {
            String message = appointments.isEmpty()
                    ? String.format(MESSAGE_NO_APPOINTMENTS_ON_DATE, date)
                    : String.format(MESSAGE_DATE_SUCCESS, date, appointments.size());
            return new CommandResult(message, CommandResult.DisplayType.APPOINTMENTS);
        }
        if (appointments.isEmpty()) {
            return new CommandResult(MESSAGE_NO_APPOINTMENTS, CommandResult.DisplayType.APPOINTMENTS);
        }

        return new CommandResult(String.format(MESSAGE_SUCCESS, appointments.size()),
                CommandResult.DisplayType.APPOINTMENTS);
    }
}

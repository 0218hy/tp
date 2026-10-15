package seedu.address.logic.commands.appointment;

import static java.util.Objects.requireNonNull;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;

/**
 * Deletes a grooming appointment identified by a date and a time within the appointment,
 * or by its displayed index.
 */
public class DeleteAppointmentCommand extends Command {
    public static final String COMMAND_WORD = "delete-appointment";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes an appointment by a date and a time within it, or by displayed index.\n"
            + "Parameters: d/DD-MM-YYYY t/HH:mm, or INDEX (must be a positive integer)\n"
            + "Examples: " + COMMAND_WORD + " d/18-09-2027 t/10:30\n"
            + "          " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Deleted appointment: %1$s";
    public static final String MESSAGE_DELETE_APPOINTMENT_SUCCESS = MESSAGE_SUCCESS;
    public static final String MESSAGE_APPOINTMENT_NOT_FOUND =
            "No appointment was found for the specified date and time.";
    public static final String MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX =
            "The appointment index provided is invalid.";

    private final Index targetIndex;
    private final AppointmentDate targetDate;
    private final LocalTime targetTime;

    /** Creates a command that deletes the appointment at the given displayed index. */
    public DeleteAppointmentCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
        this.targetDate = null;
        this.targetTime = null;
    }

    /** Creates a command that deletes the appointment containing the supplied date and time. */
    public DeleteAppointmentCommand(AppointmentDate targetDate, LocalTime targetTime) {
        this.targetIndex = null;
        this.targetDate = requireNonNull(targetDate);
        this.targetTime = requireNonNull(targetTime);
    }

    @Override
    public Optional<CommandResult.DisplayType> getRequiredDisplayType() {
        return targetIndex == null
                ? Optional.empty()
                : Optional.of(CommandResult.DisplayType.APPOINTMENTS);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Appointment appointmentToDelete = findAppointment(model);
        model.deleteAppointment(appointmentToDelete);
        return new CommandResult(String.format(MESSAGE_SUCCESS, formatAppointment(appointmentToDelete)));
    }

    private Appointment findAppointment(Model model) throws CommandException {
        List<Appointment> appointments = model.getFilteredAppointmentList();
        if (targetIndex != null) {
            if (targetIndex.getZeroBased() >= appointments.size()) {
                throw new CommandException(MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
            }
            return appointments.get(targetIndex.getZeroBased());
        }

        return model.getGroomingBook().getAppointmentList().stream()
                .filter(appointment -> appointment.matches(targetDate, targetTime))
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_APPOINTMENT_NOT_FOUND));
    }

    private static String formatAppointment(Appointment appointment) {
        return appointment.getPetName() + "; Owner phone: " + appointment.getOwnerPhone()
                + "; Date: " + appointment.getDate() + "; Time: " + appointment.getStartTime()
                + "-" + appointment.getEndTime() + "; Service: " + appointment.getService();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DeleteAppointmentCommand otherCommand)) {
            return false;
        }
        return Objects.equals(targetIndex, otherCommand.targetIndex)
                && Objects.equals(targetDate, otherCommand.targetDate)
                && Objects.equals(targetTime, otherCommand.targetTime);
    }

    @Override
    public String toString() {
        if (targetIndex != null) {
            return new ToStringBuilder(this).add("targetIndex", targetIndex).toString();
        }
        return new ToStringBuilder(this)
                .add("targetDate", targetDate)
                .add("targetTime", targetTime)
                .toString();
    }
}

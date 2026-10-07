package seedu.address.logic.commands.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentParticipantLookup;
import seedu.address.model.appointment.exceptions.OverlappingAppointmentException;

/**
 * Schedules a grooming appointment after verifying its participants and start time.
 * Requires an owner/pet lookup implementation to be supplied by the corresponding features.
 */
public class ScheduleCommand extends Command {
    public static final String COMMAND_WORD = "schedule";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Schedules a grooming appointment. "
            + "The owner identifier is their phone number.\n"
            + "Parameters: i/OWNER_IDENTIFIER p/PET_NAME d/DD-MM-YYYY st/HH:mm et/HH:mm s/SERVICE\n"
            + "Example: schedule i/91234567 p/BuBu d/18-09-2027 st/10:00 et/11:00 s/Full groom";
    public static final String MESSAGE_SUCCESS = "Appointment scheduled: %1$s";
    public static final String MESSAGE_OWNER_NOT_FOUND =
            "This phone number does not identify a unique registered owner.";
    public static final String MESSAGE_PET_NOT_FOUND = "This pet is not registered under the specified owner.";
    public static final String MESSAGE_PAST_START = "Appointment must start in the future (Singapore time).";
    public static final String MESSAGE_OVERLAP = "This appointment overlaps an existing appointment.";
    private static final ZoneId SINGAPORE_ZONE = ZoneId.of("Asia/Singapore");

    private final Appointment toSchedule;
    private final AppointmentParticipantLookup participantLookup;
    private final Clock clock;

    /**
     * Creates a command for normal application use, using the real clock in Singapore.
     * Delegates to the constructor below so both constructors share the same setup.
     */
    public ScheduleCommand(Appointment appointment, AppointmentParticipantLookup participantLookup) {
        this(appointment, participantLookup, Clock.system(SINGAPORE_ZONE));
    }

    /**
     * Creates a command with a supplied clock. Tests use a fixed clock so their results
     * do not depend on the actual date and time when the tests run.
     */
    public ScheduleCommand(Appointment appointment, AppointmentParticipantLookup participantLookup, Clock clock) {
        requireAllNonNull(appointment, participantLookup, clock);
        toSchedule = appointment;
        this.participantLookup = participantLookup;
        this.clock = clock.withZone(SINGAPORE_ZONE);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        // TODO: Replace participantLookup with Model owner/pet lookup methods once those features are available.
        // TODO: Remove AppointmentParticipantLookup and its constructor parameter after updating the tests.
        if (!participantLookup.hasOwner(toSchedule.getOwnerPhone())) {
            throw new CommandException(MESSAGE_OWNER_NOT_FOUND);
        }
        if (!participantLookup.hasPet(toSchedule.getOwnerPhone(), toSchedule.getPetName())) {
            throw new CommandException(MESSAGE_PET_NOT_FOUND);
        }

        LocalDateTime start = toSchedule.getDate().value.atTime(toSchedule.getStartTime().value);
        if (!start.isAfter(LocalDateTime.now(clock))) {
            throw new CommandException(MESSAGE_PAST_START);
        }

        try {
            model.addAppointment(toSchedule);
        } catch (OverlappingAppointmentException e) {
            throw new CommandException(MESSAGE_OVERLAP, e);
        }
        String details = toSchedule.getPetName() + "; Owner phone: " + toSchedule.getOwnerPhone()
                + "; Date: " + toSchedule.getDate() + "; Time: " + toSchedule.getStartTime()
                + "-" + toSchedule.getEndTime() + "; Service: " + toSchedule.getService();
        return new CommandResult(String.format(MESSAGE_SUCCESS, details));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ScheduleCommand otherCommand
                && toSchedule.equals(otherCommand.toSchedule));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("toSchedule", toSchedule).toString();
    }
}

package seedu.address.logic.commands.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.appointment.exceptions.OverlappingAppointmentException;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;

/**
 * Schedules a grooming appointment after verifying its participants and start time.
 * Resolves participants from the saved client and pet records when the command executes.
 */
public class AddAppointmentCommand extends Command {
    public static final String COMMAND_WORD = "add-appointment";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Schedules a grooming appointment. "
            + "The owner identifier is their phone number.\n"
            + "Parameters: i/OWNER_IDENTIFIER p/PET_NAME d/DD-MM-YYYY st/HH:mm et/HH:mm s/SERVICE\n"
            + "Example: " + COMMAND_WORD + " i/91234567 p/BuBu d/18-09-2027 st/10:00 et/11:00 s/Full groom";
    public static final String MESSAGE_SUCCESS = "Appointment scheduled: %1$s";
    public static final String MESSAGE_OWNER_NOT_FOUND =
            "This phone number does not identify a unique registered owner.";
    public static final String MESSAGE_PET_NOT_FOUND = "This pet is not registered under the specified owner.";
    public static final String MESSAGE_PAST_START = "Appointment must start in the future (Singapore time).";
    public static final String MESSAGE_OVERLAP =
            "This appointment overlaps an existing booking for %s on %s, %s–%s.";
    private static final ZoneId SINGAPORE_ZONE = ZoneId.of("Asia/Singapore");
    private static final Logger logger = LogsCenter.getLogger(AddAppointmentCommand.class);

    private final Phone ownerPhone;
    private final PetName petName;
    private final AppointmentDate date;
    private final StartTime startTime;
    private final EndTime endTime;
    private final Service service;
    private final Clock clock;

    //@@author 0218hy
    /** Creates a command using participant identifiers and the current Singapore time. */
    public AddAppointmentCommand(Phone ownerPhone, String petName, AppointmentDate date,
            StartTime startTime, EndTime endTime, Service service) {
        this(ownerPhone, petName, date, startTime, endTime, service, Clock.system(SINGAPORE_ZONE));
    }

    /** Creates a command with a supplied clock for deterministic time checks. */
    public AddAppointmentCommand(Phone ownerPhone, String petName, AppointmentDate date,
            StartTime startTime, EndTime endTime, Service service, Clock clock) {
        requireAllNonNull(ownerPhone, petName, date, startTime, endTime, service, clock);
        checkArgument(endTime.isAtLeastThirtyMinutesAfter(startTime), EndTime.MESSAGE_CONSTRAINTS);
        this.ownerPhone = ownerPhone;
        this.petName = new PetName(petName.trim().replaceAll("\\s+", " "));
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.service = service;
        this.clock = clock.withZone(SINGAPORE_ZONE);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Client> matchingOwners = model.getBubuBook().getClientList().stream()
                .filter(client -> client.getPhone().equals(ownerPhone))
                .toList();
        if (matchingOwners.size() != 1) {
            throw new CommandException(MESSAGE_OWNER_NOT_FOUND);
        }
        Client owner = matchingOwners.get(0);
        Pet pet = model.getBubuBook().getPetList().stream()
                .filter(candidate -> candidate.getOwner().equals(owner)
                        && candidate.getName().getNormalizedName().equals(petName.getNormalizedName()))
                .findFirst().orElseThrow(() -> new CommandException(MESSAGE_PET_NOT_FOUND));
        Appointment toSchedule = new Appointment(owner, pet, date, startTime, endTime, service);

        LocalDateTime start = date.value.atTime(startTime.value);
        if (!start.isAfter(LocalDateTime.now(clock))) {
            throw new CommandException(MESSAGE_PAST_START);
        }

        try {
            model.addAppointment(toSchedule);
        } catch (OverlappingAppointmentException e) {
            logger.fine(() -> "Appointment rejected due to overlap: " + date + " " + startTime + "-" + endTime);
            Appointment conflict = e.getConflictingAppointment();
            throw new CommandException(String.format(MESSAGE_OVERLAP, conflict.getPetName(), conflict.getDate(),
                    conflict.getStartTime(), conflict.getEndTime()), e);
        }
        logger.info(() -> "Appointment added to model: " + date + " " + startTime + "-" + endTime);
        String details = toSchedule.getPetName() + "; Owner phone: " + toSchedule.getOwnerPhone()
                + "; Date: " + toSchedule.getDate() + "; Time: " + toSchedule.getStartTime()
                + "-" + toSchedule.getEndTime() + "; Service: " + toSchedule.getService();
        return new CommandResult(String.format(MESSAGE_SUCCESS, details));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof AddAppointmentCommand otherCommand
                && ownerPhone.equals(otherCommand.ownerPhone)
                && petName.equals(otherCommand.petName)
                && date.equals(otherCommand.date)
                && startTime.equals(otherCommand.startTime)
                && endTime.equals(otherCommand.endTime)
                && service == otherCommand.service);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("ownerPhone", ownerPhone).add("petName", petName)
                .add("date", date).add("startTime", startTime).add("endTime", endTime)
                .add("service", service).toString();
    }
    //@@author
}

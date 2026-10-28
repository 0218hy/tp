package seedu.address.logic.commands.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_END_TIME;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_NEW_DATE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_NEW_START_TIME;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_SERVICE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_START_TIME;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
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

/** Edits the details of an existing appointment in the BuBu. */
public class EditAppointmentCommand extends Command {

    public static final String COMMAND_WORD = "edit-appointment";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits an appointment identified by its date and "
            + "start time, or by the index in the displayed appointment list. Existing values will be overwritten "
            + "by the input values.\nTarget: " + PREFIX_DATE + "CURRENT_DATE "
            + PREFIX_START_TIME + "CURRENT_START_TIME, or INDEX\n"
            + "Optional replacements: "
            + "[" + PREFIX_OWNER_IDENTIFIER + "NEW_OWNER_PHONE] "
            + "[" + PREFIX_PET_NAME + "NEW_PET_NAME] "
            + "[" + PREFIX_NEW_DATE + "NEW_DATE] "
            + "[" + PREFIX_NEW_START_TIME + "NEW_START_TIME] "
            + "[" + PREFIX_END_TIME + "NEW_END_TIME] "
            + "[" + PREFIX_SERVICE + "NEW_SERVICE]\n"
            + "Date format: DD-MM-YYYY; time format: HH:mm.\n"
            + "Start time: 08:00-19:30 in 30-minute intervals; end time: 08:30-20:00 in 30-minute intervals, "
            + "at least 30 minutes after the start.\n"
            + "Example: " + COMMAND_WORD + " d/18-09-2027 st/10:00 "
            + PREFIX_END_TIME + "12:00";

    public static final String MESSAGE_SUCCESS = "Edited appointment: %1$s";
    public static final String MESSAGE_EDIT_APPOINTMENT_SUCCESS = MESSAGE_SUCCESS;
    public static final String MESSAGE_NOT_EDITED =
            "Specify at least one appointment detail to change using i/NEW_OWNER_PHONE, p/NEW_PET_NAME, "
                    + "nd/NEW_DATE, nst/NEW_START_TIME, et/NEW_END_TIME or s/NEW_SERVICE.";
    public static final String MESSAGE_NO_CHANGES =
            "No changes were made. The appointment already has these details.";
    public static final String MESSAGE_INVALID_DURATION =
            "The appointment duration must be at least 30 minutes.";
    public static final String MESSAGE_INVALID_START_TIME =
            "The new start time must be at least 30 minutes before the existing end time.";
    public static final String MESSAGE_INVALID_END_TIME =
            "The new end time must be at least 30 minutes after the existing start time.";
    public static final String MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX =
            "The appointment index provided is not in the displayed appointment list.";
    public static final String MESSAGE_APPOINTMENT_NOT_FOUND =
            "No appointment starts at the specified date and start time.";
    public static final String MESSAGE_INDEX_AND_DATE_TIME =
            "Choose either an appointment index or a date and start time, not both.";
    public static final String MESSAGE_OWNER_NOT_FOUND =
            "No registered owner matches the specified phone number.";
    public static final String MESSAGE_PET_NOT_FOUND = AddAppointmentCommand.MESSAGE_PET_NOT_FOUND;
    public static final String MESSAGE_PAST_START =
            "The updated appointment must start in the future (Singapore time).";
    public static final String MESSAGE_OVERLAP =
            "The updated appointment overlaps another appointment. Choose an available time slot.";

    private static final ZoneId SINGAPORE_ZONE = ZoneId.of("Asia/Singapore");
    private static final Logger logger = LogsCenter.getLogger(EditAppointmentCommand.class);

    private final Index index;
    private final AppointmentDate targetDate;
    private final StartTime targetStartTime;
    private final EditAppointmentDescriptor editAppointmentDescriptor;
    private final Clock clock;

    /**
     * Creates an index-based edit command using the real clock in Singapore.
     *
     * @param index displayed one-based appointment index to edit
     * @param editAppointmentDescriptor requested replacement fields
     */
    public EditAppointmentCommand(Index index, EditAppointmentDescriptor editAppointmentDescriptor) {
        this(index, editAppointmentDescriptor, Clock.system(SINGAPORE_ZONE));
    }

    /**
     * Creates an index-based edit command with a supplied clock.
     *
     * @param index displayed one-based appointment index to edit
     * @param editAppointmentDescriptor requested replacement fields
     * @param clock clock used for the future-start validation
     */
    public EditAppointmentCommand(Index index, EditAppointmentDescriptor editAppointmentDescriptor, Clock clock) {
        requireAllNonNull(index, editAppointmentDescriptor, clock);
        this.index = index;
        this.targetDate = null;
        this.targetStartTime = null;
        this.editAppointmentDescriptor = new EditAppointmentDescriptor(editAppointmentDescriptor);
        this.clock = clock.withZone(SINGAPORE_ZONE);
    }

    /**
     * Creates an identifier-based edit command using the real clock in Singapore.
     *
     * @param targetDate current date of the appointment to edit
     * @param targetStartTime current start time of the appointment to edit
     * @param editAppointmentDescriptor requested replacement fields
     */
    public EditAppointmentCommand(AppointmentDate targetDate, StartTime targetStartTime,
                                  EditAppointmentDescriptor editAppointmentDescriptor) {
        this(targetDate, targetStartTime, editAppointmentDescriptor, Clock.system(SINGAPORE_ZONE));
    }

    /**
     * Creates an identifier-based edit command with a supplied clock.
     *
     * @param targetDate current date of the appointment to edit
     * @param targetStartTime current start time of the appointment to edit
     * @param editAppointmentDescriptor requested replacement fields
     * @param clock clock used for the future-start validation
     */
    public EditAppointmentCommand(AppointmentDate targetDate, StartTime targetStartTime,
                                  EditAppointmentDescriptor editAppointmentDescriptor, Clock clock) {
        requireAllNonNull(targetDate, targetStartTime, editAppointmentDescriptor, clock);
        this.index = null;
        this.targetDate = targetDate;
        this.targetStartTime = targetStartTime;
        this.editAppointmentDescriptor = new EditAppointmentDescriptor(editAppointmentDescriptor);
        this.clock = clock.withZone(SINGAPORE_ZONE);
    }

    //@@author chunghean
    /** Returns the appointment display requirement for index-based edits. */
    @Override
    public Optional<CommandResult.DisplayType> getRequiredDisplayType() {
        return index == null
                ? Optional.empty()
                : Optional.of(CommandResult.DisplayType.APPOINTMENTS);
    }
    //@@author

    /**
     * Finds and edits the selected appointment, validating participants, timing, and overlap constraints.
     *
     * @param model model containing the appointment and participant records
     * @return the result message describing the edited appointment
     * @throws CommandException if the target or participants cannot be found, the duration is invalid,
     *                          the start is not in the future, or the edited appointment overlaps another appointment
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Appointment appointmentToEdit = findAppointment(model);
        Appointment editedAppointment = createEditedAppointment(model, appointmentToEdit, editAppointmentDescriptor);

        if (appointmentToEdit.equals(editedAppointment)) {
            logger.fine("edit-appointment rejected: the requested details match the existing appointment.");
            throw new CommandException(MESSAGE_NO_CHANGES);
        }

        LocalDateTime start = editedAppointment.getDate().value.atTime(editedAppointment.getStartTime().value);
        if (!start.isAfter(LocalDateTime.now(clock))) {
            logger.fine("edit-appointment rejected: appointment start is not in the future.");
            throw new CommandException(MESSAGE_PAST_START);
        }

        List<Appointment> replacement = new ArrayList<>(model.getBubuBook().getAppointmentList());
        int storedIndex = replacement.indexOf(appointmentToEdit);
        if (storedIndex < 0) {
            logger.fine("edit-appointment rejected: appointment is no longer present in the model.");
            throw new CommandException(MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
        }
        replacement.set(storedIndex, editedAppointment);

        try {
            // setAppointments validates the complete replacement before mutating the model,
            // including overlap checks against every appointment except the original slot.
            model.setAppointments(replacement);
        } catch (OverlappingAppointmentException e) {
            logger.fine(() -> "edit-appointment rejected due to overlap: "
                    + editedAppointment.getDate() + " " + editedAppointment.getStartTime()
                    + "-" + editedAppointment.getEndTime());
            throw new CommandException(MESSAGE_OVERLAP, e);
        }
        logger.info(() -> "edit-appointment: appointment updated in model; slot changed="
                + !appointmentToEdit.isSameAppointment(editedAppointment) + "; awaiting storage save.");
        return new CommandResult(String.format(MESSAGE_SUCCESS, formatAppointment(editedAppointment)));
    }

    /** Finds the target appointment using either the displayed index or date/start-time identifier. */
    private Appointment findAppointment(Model model) throws CommandException {
        if (index != null) {
            List<Appointment> displayedAppointments = model.getFilteredAppointmentList();
            if (index.getZeroBased() >= displayedAppointments.size()) {
                logger.fine("edit-appointment rejected: invalid displayed appointment index.");
                throw new CommandException(MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
            }
            return displayedAppointments.get(index.getZeroBased());
        }

        return model.getBubuBook().getAppointmentList().stream()
                .filter(appointment -> appointment.getDate().equals(targetDate)
                        && appointment.getStartTime().equals(targetStartTime))
                .findFirst()
                .orElseThrow(() -> {
                    logger.fine("edit-appointment rejected: no appointment matches the date and start time.");
                    return new CommandException(MESSAGE_APPOINTMENT_NOT_FOUND);
                });
    }

    /** Creates a complete appointment by applying the descriptor to the existing appointment. */
    private static Appointment createEditedAppointment(Model model, Appointment appointmentToEdit,
                                                       EditAppointmentDescriptor descriptor) throws CommandException {
        assert appointmentToEdit != null;
        Phone ownerPhone = descriptor.getOwnerPhone().orElse(appointmentToEdit.getOwnerPhone());
        PetName petName = descriptor.getPetName().orElse(appointmentToEdit.getPetName());
        AppointmentDate date = descriptor.getDate().orElse(appointmentToEdit.getDate());
        StartTime startTime = descriptor.getStartTime().orElse(appointmentToEdit.getStartTime());
        EndTime endTime = descriptor.getEndTime().orElse(appointmentToEdit.getEndTime());
        Service service = descriptor.getService().orElse(appointmentToEdit.getService());

        long matchingOwners = model.getBubuBook().getClientList().stream()
                .filter(client -> client.getPhone().equals(ownerPhone))
                .count();
        if (matchingOwners != 1) {
            logger.fine("edit-appointment rejected: no unique saved owner matches the phone identifier.");
            throw new CommandException(MESSAGE_OWNER_NOT_FOUND);
        }
        Client owner = model.getBubuBook().getClientList().stream()
                .filter(client -> client.getPhone().equals(ownerPhone))
                .findFirst()
                .orElseThrow();

        Pet pet = model.getBubuBook().getPetList().stream()
                .filter(candidate -> candidate.getOwner().equals(owner)
                        && candidate.getName().getNormalizedName().equals(petName.getNormalizedName()))
                .findFirst()
                .orElse(null);
        if (pet == null) {
            logger.fine("edit-appointment rejected: pet is not registered under the specified owner.");
            throw new CommandException(MESSAGE_PET_NOT_FOUND);
        }

        if (!endTime.isAtLeastThirtyMinutesAfter(startTime)) {
            logger.fine("edit-appointment rejected: end time is less than 30 minutes after the start time.");
            if (descriptor.getStartTime().isPresent() && descriptor.getEndTime().isEmpty()) {
                throw new CommandException(MESSAGE_INVALID_START_TIME);
            }
            if (descriptor.getStartTime().isEmpty() && descriptor.getEndTime().isPresent()) {
                throw new CommandException(MESSAGE_INVALID_END_TIME);
            }
            throw new CommandException(MESSAGE_INVALID_DURATION);
        }

        return new Appointment(owner, pet, date, startTime, endTime, service);
    }

    /** Formats an appointment for the command result message. */
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
        if (!(other instanceof EditAppointmentCommand otherCommand)) {
            return false;
        }
        return Objects.equals(index, otherCommand.index)
                && Objects.equals(targetDate, otherCommand.targetDate)
                && Objects.equals(targetStartTime, otherCommand.targetStartTime)
                && editAppointmentDescriptor.equals(otherCommand.editAppointmentDescriptor);
    }

    @Override
    public String toString() {
        if (index != null) {
            return new ToStringBuilder(this)
                    .add("targetIndex", index)
                    .add("editAppointmentDescriptor", editAppointmentDescriptor)
                    .toString();
        }
        return new ToStringBuilder(this)
                .add("targetDate", targetDate)
                .add("targetStartTime", targetStartTime)
                .add("editAppointmentDescriptor", editAppointmentDescriptor)
                .toString();
    }

    /** Stores the appointment fields that should replace the corresponding existing values. */
    public static class EditAppointmentDescriptor {
        private Phone ownerPhone;
        private PetName petName;
        private AppointmentDate date;
        private StartTime startTime;
        private EndTime endTime;
        private Service service;

        /** Creates an empty descriptor with no requested changes. */
        public EditAppointmentDescriptor() {}

        /** Creates a defensive copy of the supplied descriptor. */
        public EditAppointmentDescriptor(EditAppointmentDescriptor toCopy) {
            requireNonNull(toCopy);
            setOwnerPhone(toCopy.ownerPhone);
            setPetName(toCopy.petName);
            setDate(toCopy.date);
            setStartTime(toCopy.startTime);
            setEndTime(toCopy.endTime);
            setService(toCopy.service);
        }

        /** Returns whether at least one replacement field has been supplied. */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(ownerPhone, petName, date, startTime, endTime, service);
        }

        /** Sets the replacement owner phone number. */
        public void setOwnerPhone(Phone ownerPhone) {
            this.ownerPhone = ownerPhone;
        }

        /** Returns the replacement owner phone number, if one was supplied. */
        public Optional<Phone> getOwnerPhone() {
            return Optional.ofNullable(ownerPhone);
        }

        /** Sets the replacement pet name. */
        public void setPetName(PetName petName) {
            this.petName = petName;
        }

        //@@author chunghean
        /** Parses and sets the replacement pet name from text. */
        public void setPetName(String petName) {
            this.petName = petName == null ? null : new PetName(petName);
        }
        //@@author

        /** Returns the replacement pet name, if one was supplied. */
        public Optional<PetName> getPetName() {
            return Optional.ofNullable(petName);
        }

        /** Sets the replacement appointment date. */
        public void setDate(AppointmentDate date) {
            this.date = date;
        }

        /** Returns the replacement appointment date, if one was supplied. */
        public Optional<AppointmentDate> getDate() {
            return Optional.ofNullable(date);
        }

        /** Sets the replacement appointment start time. */
        public void setStartTime(StartTime startTime) {
            this.startTime = startTime;
        }

        /** Returns the replacement appointment start time, if one was supplied. */
        public Optional<StartTime> getStartTime() {
            return Optional.ofNullable(startTime);
        }

        /** Sets the replacement appointment end time. */
        public void setEndTime(EndTime endTime) {
            this.endTime = endTime;
        }

        /** Returns the replacement appointment end time, if one was supplied. */
        public Optional<EndTime> getEndTime() {
            return Optional.ofNullable(endTime);
        }

        /** Sets the replacement grooming service. */
        public void setService(Service service) {
            this.service = service;
        }

        /** Returns the replacement grooming service, if one was supplied. */
        public Optional<Service> getService() {
            return Optional.ofNullable(service);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof EditAppointmentDescriptor otherDescriptor)) {
                return false;
            }
            return Objects.equals(ownerPhone, otherDescriptor.ownerPhone)
                    && Objects.equals(petName, otherDescriptor.petName)
                    && Objects.equals(date, otherDescriptor.date)
                    && Objects.equals(startTime, otherDescriptor.startTime)
                    && Objects.equals(endTime, otherDescriptor.endTime)
                    && Objects.equals(service, otherDescriptor.service);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("ownerPhone", ownerPhone)
                    .add("petName", petName)
                    .add("date", date)
                    .add("startTime", startTime)
                    .add("endTime", endTime)
                    .add("service", service)
                    .toString();
        }
    }
}

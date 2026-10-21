package seedu.address.logic.commands.appointment;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_END_TIME;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_SERVICE;
import static seedu.address.logic.parser.appointment.AppointmentCliSyntax.PREFIX_START_TIME;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_APPOINTMENTS;

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
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the appointment identified by the index "
            + "number used in the displayed appointment list. Existing values will be overwritten by the input "
            + "values.\nParameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_OWNER_IDENTIFIER + "OWNER_PHONE] "
            + "[" + PREFIX_PET_NAME + "PET_NAME] "
            + "[" + PREFIX_DATE + "DD-MM-YYYY] "
            + "[" + PREFIX_START_TIME + "HH:mm] "
            + "[" + PREFIX_END_TIME + "HH:mm] "
            + "[" + PREFIX_SERVICE + "SERVICE]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_START_TIME + "11:00 " + PREFIX_END_TIME + "12:00";

    public static final String MESSAGE_SUCCESS = "Edited appointment: %1$s";
    public static final String MESSAGE_EDIT_APPOINTMENT_SUCCESS = MESSAGE_SUCCESS;
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX =
            "The appointment index provided is invalid.";
    public static final String MESSAGE_OWNER_NOT_FOUND = AddAppointmentCommand.MESSAGE_OWNER_NOT_FOUND;
    public static final String MESSAGE_PET_NOT_FOUND = AddAppointmentCommand.MESSAGE_PET_NOT_FOUND;
    public static final String MESSAGE_PAST_START = AddAppointmentCommand.MESSAGE_PAST_START;
    public static final String MESSAGE_OVERLAP = AddAppointmentCommand.MESSAGE_OVERLAP;

    private static final ZoneId SINGAPORE_ZONE = ZoneId.of("Asia/Singapore");
    private static final Logger logger = LogsCenter.getLogger(EditAppointmentCommand.class);

    private final Index index;
    private final EditAppointmentDescriptor editAppointmentDescriptor;
    private final Clock clock;

    /** Creates a command using the real clock in Singapore. */
    public EditAppointmentCommand(Index index, EditAppointmentDescriptor editAppointmentDescriptor) {
        this(index, editAppointmentDescriptor, Clock.system(SINGAPORE_ZONE));
    }

    /** Creates a command with a supplied clock, which keeps time-sensitive tests deterministic. */
    public EditAppointmentCommand(Index index, EditAppointmentDescriptor editAppointmentDescriptor, Clock clock) {
        requireAllNonNull(index, editAppointmentDescriptor, clock);
        this.index = index;
        this.editAppointmentDescriptor = new EditAppointmentDescriptor(editAppointmentDescriptor);
        this.clock = clock.withZone(SINGAPORE_ZONE);
    }

    @Override
    public Optional<CommandResult.DisplayType> getRequiredDisplayType() {
        return Optional.of(CommandResult.DisplayType.APPOINTMENTS);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Appointment> displayedAppointments = model.getFilteredAppointmentList();
        if (index.getZeroBased() >= displayedAppointments.size()) {
            logger.fine("edit-appointment rejected: invalid displayed appointment index.");
            throw new CommandException(MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
        }

        Appointment appointmentToEdit = displayedAppointments.get(index.getZeroBased());
        Appointment editedAppointment = createEditedAppointment(model, appointmentToEdit, editAppointmentDescriptor);

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
        model.updateFilteredAppointmentList(PREDICATE_SHOW_ALL_APPOINTMENTS);
        logger.info(() -> "edit-appointment: appointment updated in model; slot changed="
                + !appointmentToEdit.isSameAppointment(editedAppointment) + "; awaiting storage save.");
        return new CommandResult(String.format(MESSAGE_SUCCESS, formatAppointment(editedAppointment)));
    }

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

        return new Appointment(owner, pet, date, startTime, endTime, service);
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
        if (!(other instanceof EditAppointmentCommand otherCommand)) {
            return false;
        }
        return index.equals(otherCommand.index)
                && editAppointmentDescriptor.equals(otherCommand.editAppointmentDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
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

        public EditAppointmentDescriptor() {}

        /** Copy constructor. */
        public EditAppointmentDescriptor(EditAppointmentDescriptor toCopy) {
            requireNonNull(toCopy);
            setOwnerPhone(toCopy.ownerPhone);
            setPetName(toCopy.petName);
            setDate(toCopy.date);
            setStartTime(toCopy.startTime);
            setEndTime(toCopy.endTime);
            setService(toCopy.service);
        }

        /** Returns true if at least one field is edited. */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(ownerPhone, petName, date, startTime, endTime, service);
        }

        public void setOwnerPhone(Phone ownerPhone) {
            this.ownerPhone = ownerPhone;
        }

        public Optional<Phone> getOwnerPhone() {
            return Optional.ofNullable(ownerPhone);
        }

        public void setPetName(PetName petName) {
            this.petName = petName;
        }

        public void setPetName(String petName) {
            this.petName = petName == null ? null : new PetName(petName);
        }

        public Optional<PetName> getPetName() {
            return Optional.ofNullable(petName);
        }

        public void setDate(AppointmentDate date) {
            this.date = date;
        }

        public Optional<AppointmentDate> getDate() {
            return Optional.ofNullable(date);
        }

        public void setStartTime(StartTime startTime) {
            this.startTime = startTime;
        }

        public Optional<StartTime> getStartTime() {
            return Optional.ofNullable(startTime);
        }

        public void setEndTime(EndTime endTime) {
            this.endTime = endTime;
        }

        public Optional<EndTime> getEndTime() {
            return Optional.ofNullable(endTime);
        }

        public void setService(Service service) {
            this.service = service;
        }

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

package seedu.address.logic.commands.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.client.DeleteClientCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.GroomingBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Phone;

/** Contains integration and unit tests for {@code DeleteAppointmentCommand}. */
public class DeleteAppointmentCommandTest {
    private static final AppointmentDate DATE = new AppointmentDate("01-01-2020");
    private static final Appointment APPOINTMENT = create("10:00", "11:00");
    private static final Appointment ADJACENT_APPOINTMENT = create("11:00", "12:00");

    private Model model;

    @BeforeEach
    public void setUp() {
        GroomingBook groomingBook = new GroomingBook();
        groomingBook.addAppointment(APPOINTMENT);
        groomingBook.addAppointment(ADJACENT_APPOINTMENT);
        model = new ModelManager(groomingBook, new UserPrefs());
    }

    @Test
    public void execute_validIndex_deletesAppointment() {
        Model expectedModel = new ModelManager(model.getGroomingBook(), new UserPrefs());
        expectedModel.deleteAppointment(APPOINTMENT);

        assertCommandSuccess(new DeleteAppointmentCommand(Index.fromOneBased(1)), model,
                successMessage(APPOINTMENT), expectedModel);
    }

    @Test
    public void execute_validDateAndTime_deletesContainingAppointment() {
        Model expectedModel = new ModelManager(model.getGroomingBook(), new UserPrefs());
        expectedModel.deleteAppointment(APPOINTMENT);

        assertCommandSuccess(new DeleteAppointmentCommand(DATE, LocalTime.of(10, 30)), model,
                successMessage(APPOINTMENT), expectedModel);
    }

    @Test
    public void execute_startTime_deletesAppointment() throws CommandException {
        new DeleteAppointmentCommand(DATE, LocalTime.of(10, 0)).execute(model);

        assertEquals(List.of(ADJACENT_APPOINTMENT), model.getGroomingBook().getAppointmentList());
    }

    @Test
    public void execute_endTime_doesNotMatchPreviousAppointment() {
        Model modelWithOneAppointment = new ModelManager();
        modelWithOneAppointment.addAppointment(APPOINTMENT);

        assertCommandFailure(new DeleteAppointmentCommand(DATE, LocalTime.of(11, 0)), modelWithOneAppointment,
                DeleteAppointmentCommand.MESSAGE_APPOINTMENT_NOT_FOUND);
    }

    @Test
    public void execute_appointmentDoesNotMatch_throwsCommandException() {
        assertCommandFailure(new DeleteAppointmentCommand(new AppointmentDate("02-01-2020"),
                LocalTime.of(10, 30)), model, DeleteAppointmentCommand.MESSAGE_APPOINTMENT_NOT_FOUND);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        assertCommandFailure(new DeleteAppointmentCommand(Index.fromOneBased(3)), model,
                DeleteAppointmentCommand.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new DeleteAppointmentCommand(Index.fromOneBased(1)).execute(null));
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteAppointmentCommand((Index) null));
        assertThrows(NullPointerException.class, () -> new DeleteAppointmentCommand(null, LocalTime.NOON));
        assertThrows(NullPointerException.class, () -> new DeleteAppointmentCommand(DATE, null));
    }

    @Test
    public void equals() {
        DeleteAppointmentCommand byIndex = new DeleteAppointmentCommand(Index.fromOneBased(1));
        DeleteAppointmentCommand byDateAndTime = new DeleteAppointmentCommand(DATE, LocalTime.of(10, 30));

        assertEquals(byIndex, byIndex);
        assertEquals(byIndex, new DeleteAppointmentCommand(Index.fromOneBased(1)));
        assertFalse(byIndex.equals(new DeleteAppointmentCommand(Index.fromOneBased(2))));
        assertEquals(byDateAndTime, new DeleteAppointmentCommand(DATE, LocalTime.of(10, 30)));
        assertFalse(byDateAndTime.equals(new DeleteAppointmentCommand(DATE, LocalTime.of(11, 30))));
        assertFalse(byIndex.equals(null));
        assertFalse(byIndex.equals(new DeleteClientCommand(Index.fromOneBased(1))));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        assertEquals(DeleteAppointmentCommand.class.getCanonicalName() + "{targetIndex=" + index + "}",
                new DeleteAppointmentCommand(index).toString());
        assertEquals(DeleteAppointmentCommand.class.getCanonicalName()
                        + "{targetDate=01-01-2020, targetTime=10:30}",
                new DeleteAppointmentCommand(DATE, LocalTime.of(10, 30)).toString());
    }

    private static String successMessage(Appointment appointment) {
        return String.format(DeleteAppointmentCommand.MESSAGE_SUCCESS,
                appointment.getPetName() + "; Owner phone: " + appointment.getOwnerPhone()
                        + "; Date: " + appointment.getDate() + "; Time: " + appointment.getStartTime()
                        + "-" + appointment.getEndTime() + "; Service: " + appointment.getService());
    }

    private static Appointment create(String start, String end) {
        return new Appointment(new Phone("91234567"), "BuBu", DATE, new StartTime(start),
                new EndTime(end), Service.FULL_GROOM);
    }
}

package seedu.address.ui.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.ui.JavaFxTestUtils;

@EnabledOnOs(OS.LINUX)
public class AppointmentListPanelTest {

    @Test
    public void constructorAndCell_updateGraphicForEmptyAndPopulatedItems() {
        JavaFxTestUtils.runAndWait(() -> {
            Appointment appointment = createAppointment(ALICE.getPhone(), "Milo",
                    new AppointmentDate("01-01-2027"), new StartTime("10:00"),
                    new EndTime("11:00"), Service.FULL_GROOM);
            ObservableList<Appointment> appointments = FXCollections.observableArrayList(appointment);
            AppointmentListPanel panel = new AppointmentListPanel(appointments);
            AppointmentListPanel.AppointmentListViewCell cell = panel.new AppointmentListViewCell();

            assertEquals(appointments, panelList(panel));
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());

            cell.updateItem(appointment, false);
            assertNotNull(cell.getGraphic());

            cell.updateItem(null, false);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());
        });
    }

    @SuppressWarnings("unchecked")
    private static ObservableList<Appointment> panelList(AppointmentListPanel panel) {
        try {
            Field field = AppointmentListPanel.class.getDeclaredField("appointmentListView");
            field.setAccessible(true);
            return ((javafx.scene.control.ListView<Appointment>) field.get(panel)).getItems();
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}

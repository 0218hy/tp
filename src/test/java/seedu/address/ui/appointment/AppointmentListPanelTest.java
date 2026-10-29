package seedu.address.ui.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.testutil.PetBuilder;
import seedu.address.ui.JavaFxTestUtils;

public class AppointmentListPanelTest {

    private static final double DEFAULT_WINDOW_WIDTH = 740;
    private static final double MINIMUM_WINDOW_WIDTH = 450;
    private static final double WIDTH_TOLERANCE = 1;
    private static final String LONG_REQUIREMENT = "long note ".repeat(24);

    //@@author chunghean
    @Test
    public void constructorAndCell_updateGraphicForEmptyAndPopulatedItems() {
        JavaFxTestUtils.runAndWait(() -> {
            Appointment appointment = createAppointment(ALICE.getPhone(), "Milo",
                    new AppointmentDate("01-01-2027"), new StartTime("10:00"),
                    new EndTime("11:00"), Service.FULL_GROOM);
            ObservableList<Appointment> appointments = FXCollections.observableArrayList(appointment);
            AppointmentListPanel panel = new AppointmentListPanel(appointments);
            AppointmentListPanel.AppointmentListViewCell cell = panel.new AppointmentListViewCell();

            // EP: empty cell
            assertEquals(appointments, panelList(panel));
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());

            // EP: populated cell
            cell.updateItem(appointment, false);
            assertNotNull(cell.getGraphic());

            // EP: cell with a null item despite not being marked empty
            cell.updateItem(null, false);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());
        });
    }
    //@@author

    // @@author xinyu-zxy
    @Test
    public void updateItem_dateGroups_showsOnlyFirstHeadingAndKeepsContinuousIndices() {
        JavaFxTestUtils.runAndWait(() -> {
            Appointment first = appointmentOn("01-01-2027", "10:00", "11:00");
            Appointment second = appointmentOn("01-01-2027", "12:00", "13:00");
            Appointment third = appointmentOn("02-01-2027", "10:00", "11:00");
            ObservableList<Appointment> appointments = FXCollections.observableArrayList(first, second, third);
            AppointmentListPanel panel = new AppointmentListPanel(appointments);
            AppointmentListPanel.AppointmentListViewCell cell = panel.new AppointmentListViewCell();

            // EP: first appointment in a date group
            // BVA: index zero (first valid list index)
            cell.updateIndex(0);
            cell.updateItem(first, false);
            assertCard(cell, true, "1. ", "Appointments for 01-01-2027");

            // EP: appointment on the same date as the previous appointment
            cell.updateIndex(1);
            cell.updateItem(second, false);
            assertCard(cell, false, "2. ", "Appointments for 01-01-2027");

            // EP: appointment on a different date from the previous appointment
            cell.updateIndex(2);
            cell.updateItem(third, false);
            assertCard(cell, true, "3. ", "Appointments for 02-01-2027");
        });
    }

    @Test
    public void updateItem_temporaryIndices_showsHeadingWithoutReadingPreviousAppointment() {
        JavaFxTestUtils.runAndWait(() -> {
            Appointment appointment = appointmentOn("01-01-2027", "10:00", "11:00");
            ObservableList<Appointment> appointments = FXCollections.observableArrayList(appointment);
            AppointmentListPanel panel = new AppointmentListPanel(appointments);
            AppointmentListPanel.AppointmentListViewCell cell = panel.new AppointmentListViewCell();

            // EP: index below the valid list range
            // BVA: index minus one (immediately below the first valid index)
            cell.updateIndex(-1);
            cell.updateItem(appointment, false);
            assertCard(cell, true, "0. ", "Appointments for 01-01-2027");

            // EP: index at or above the list size
            // BVA: index equal to the list size (immediately above the last valid index)
            cell.updateIndex(appointments.size());
            cell.updateItem(appointment, false);
            assertCard(cell, true, "2. ", "Appointments for 01-01-2027");

            cell.updateIndex(appointments.size() + 1);
            cell.updateItem(appointment, false);
            assertCard(cell, true, "3. ", "Appointments for 01-01-2027");
        });
    }

    @Test
    public void updateItem_afterFilteringAndDeletion_recalculatesGroupHeading() {
        JavaFxTestUtils.runAndWait(() -> {
            Appointment first = appointmentOn("01-01-2027", "10:00", "11:00");
            Appointment second = appointmentOn("01-01-2027", "12:00", "13:00");
            ObservableList<Appointment> savedAppointments = FXCollections.observableArrayList(first, second);
            FilteredList<Appointment> displayedAppointments = new FilteredList<>(savedAppointments);
            AppointmentListPanel panel = new AppointmentListPanel(displayedAppointments);
            AppointmentListPanel.AppointmentListViewCell cell = panel.new AppointmentListViewCell();
            cell.updateIndex(1);
            cell.updateItem(second, false);
            assertCard(cell, false, "2. ", "Appointments for 01-01-2027");

            // EP: filtering makes an existing appointment the first item in its date group
            displayedAppointments.setPredicate(appointment -> appointment.equals(second));
            cell.updateIndex(0);
            cell.updateItem(second, false);
            assertCard(cell, true, "1. ", "Appointments for 01-01-2027");

            // EP: removing the filter restores a preceding appointment in the same date group
            displayedAppointments.setPredicate(appointment -> true);
            cell.updateIndex(1);
            cell.updateItem(second, false);
            assertCard(cell, false, "2. ", "Appointments for 01-01-2027");

            // EP: deletion makes an existing appointment the first item in its date group
            savedAppointments.remove(first);
            cell.updateIndex(0);
            cell.updateItem(second, false);
            assertCard(cell, true, "1. ", "Appointments for 01-01-2027");

            // EP: deleting all appointments produces an empty cell
            savedAppointments.clear();
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
        });
    }

    @Test
    public void longCareNotes_inActualListView_wrapWithinDefaultAndMinimumWidths() {
        JavaFxTestUtils.runAndWait(() -> {
            assertLongCareNotesFitListView(DEFAULT_WINDOW_WIDTH);
            assertLongCareNotesFitListView(MINIMUM_WINDOW_WIDTH);
        });
    }

    private static void assertLongCareNotesFitListView(double windowWidth) {
        Appointment appointment = new Appointment(ALICE,
                new PetBuilder(ALICE).withRequirement(LONG_REQUIREMENT).build(),
                new AppointmentDate("01-01-2027"), new StartTime("10:00"),
                new EndTime("11:00"), Service.FULL_GROOM);
        AppointmentListPanel panel = new AppointmentListPanel(FXCollections.observableArrayList(appointment));
        Region panelRoot = panel.getRoot();
        StackPane sceneRoot = new StackPane(panelRoot);
        new Scene(sceneRoot, windowWidth, 600);
        sceneRoot.applyCss();
        sceneRoot.layout();

        ListView<Appointment> listView = panelListView(panel);
        ListCell<Appointment> cell = listViewCell(listView);
        Region card = (Region) cell.getGraphic();
        Label careNotes = (Label) card.lookup("#careNotes");

        assertTrue(listView.getWidth() > 0);
        assertTrue(card.getWidth() > 0);
        assertTrue(card.getBoundsInParent().getWidth() <= listView.getWidth() + WIDTH_TOLERANCE);
        assertTrue(careNotes.getHeight() > careNotes.prefHeight(Double.MAX_VALUE));
    }

    private static Appointment appointmentOn(String date, String startTime, String endTime) {
        return createAppointment(ALICE.getPhone(), "Milo", new AppointmentDate(date),
                new StartTime(startTime), new EndTime(endTime), Service.FULL_GROOM);
    }

    private static void assertCard(AppointmentListPanel.AppointmentListViewCell cell, boolean headingVisible,
            String expectedIndex, String expectedDateHeading) {
        Label heading = getLabel(cell, "dateHeading");
        assertEquals(headingVisible, heading.isVisible());
        assertEquals(headingVisible, heading.isManaged());
        assertEquals(expectedIndex, getLabel(cell, "id").getText());
        assertEquals(expectedDateHeading, heading.getText());
    }

    private static Label getLabel(AppointmentListPanel.AppointmentListViewCell cell, String id) {
        return (Label) cell.getGraphic().lookup("#" + id);
    }
    // @@author

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

    @SuppressWarnings("unchecked")
    private static ListView<Appointment> panelListView(AppointmentListPanel panel) {
        try {
            Field field = AppointmentListPanel.class.getDeclaredField("appointmentListView");
            field.setAccessible(true);
            return (ListView<Appointment>) field.get(panel);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static ListCell<Appointment> listViewCell(ListView<Appointment> listView) {
        return listView.getSkin().getNode().lookupAll(".list-cell").stream()
                .map(node -> (ListCell<Appointment>) node)
                .filter(cell -> !cell.isEmpty())
                .findFirst()
                .orElseThrow(() -> new AssertionError("The appointment ListView did not create a populated cell."));
    }
}

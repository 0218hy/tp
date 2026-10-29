package seedu.address.ui.appointment;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.appointment.Appointment;
import seedu.address.ui.UiPart;

/** Panel containing the list of appointments. */
public class AppointmentListPanel extends UiPart<Region> {
    private static final String FXML = "AppointmentListPanel.fxml";

    @FXML
    private ListView<Appointment> appointmentListView;

    /** Creates an {@code AppointmentListPanel} with the given observable appointment list. */
    public AppointmentListPanel(ObservableList<Appointment> appointmentList) {
        super(FXML);
        appointmentListView.setItems(appointmentList);
        appointmentListView.setCellFactory(listView -> new AppointmentListViewCell());
        // Refresh headings when a deletion or filter change moves the first appointment of a date.
        appointmentList.addListener((ListChangeListener<Appointment>) change -> appointmentListView.refresh());
    }

    // @@author xinyu-zxy
    /** Custom cell that displays one appointment using an {@link AppointmentCard}. */
    class AppointmentListViewCell extends ListCell<Appointment> {
        AppointmentListViewCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setMaxWidth(Double.MAX_VALUE);
            setPrefWidth(0);
            indexProperty().addListener((observable, oldIndex, newIndex) -> updateItem(getItem(), isEmpty()));
        }

        @Override
        protected void updateItem(Appointment appointment, boolean empty) {
            super.updateItem(appointment, empty);

            if (empty || appointment == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            boolean showDateHeading = startsDateGroup(appointment, getIndex());
            AppointmentCard card = new AppointmentCard(appointment, getIndex() + 1, showDateHeading);
            card.getRoot().prefWidthProperty().bind(widthProperty());
            setGraphic(card.getRoot());
        }

        /**
         * Returns whether this appointment starts a new date group in the displayed list.
         * Temporary out-of-range cell indices are treated as group starts during list updates.
         */
        private boolean startsDateGroup(Appointment appointment, int index) {
            ObservableList<Appointment> appointments = appointmentListView.getItems();
            // Virtualised cells can temporarily have an index outside the current list.
            if (index <= 0 || index >= appointments.size()) {
                return true;
            }
            Appointment previousAppointment = appointments.get(index - 1);
            return !appointment.getDate().equals(previousAppointment.getDate());
        }
    }
    // @@author
}

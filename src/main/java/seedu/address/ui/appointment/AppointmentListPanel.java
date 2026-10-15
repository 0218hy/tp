package seedu.address.ui.appointment;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
    }

    /** Custom cell that displays one appointment using an {@link AppointmentCard}. */
    class AppointmentListViewCell extends ListCell<Appointment> {
        @Override
        protected void updateItem(Appointment appointment, boolean empty) {
            super.updateItem(appointment, empty);

            if (empty || appointment == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new AppointmentCard(appointment, getIndex() + 1).getRoot());
            }
        }
    }
}

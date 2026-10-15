package seedu.address.ui.appointment;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.appointment.Appointment;
import seedu.address.ui.UiPart;

/** A UI component that displays information of an {@code Appointment}. */
public class AppointmentCard extends UiPart<Region> {
    private static final String FXML = "AppointmentCard.fxml";

    public final Appointment appointment;

    @FXML
    private HBox cardPane;
    @FXML
    private Label id;
    @FXML
    private Label dateTime;
    @FXML
    private Label pet;
    @FXML
    private Label owner;
    @FXML
    private Label service;

    /** Creates an {@code AppointmentCard} with the given appointment and displayed index. */
    public AppointmentCard(Appointment appointment, int displayedIndex) {
        super(FXML);
        this.appointment = appointment;
        id.setText(displayedIndex + ". ");
        dateTime.setText(appointment.getDate() + " " + appointment.getStartTime()
                + "-" + appointment.getEndTime());
        pet.setText("Pet: " + appointment.getPetName());
        owner.setText("Owner phone: " + appointment.getOwnerPhone());
        service.setText("Service: " + appointment.getService());
    }
}

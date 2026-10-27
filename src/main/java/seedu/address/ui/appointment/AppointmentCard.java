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
    private static final String INDEX_FORMAT = "%d. ";
    private static final String DATE_HEADING_FORMAT = "Appointments for %s";
    private static final String TITLE_FORMAT = "%s (%s-%s)";
    private static final String PET_FORMAT = "Pet: %s";
    private static final String OWNER_FORMAT = "Owner: %s";
    private static final String PHONE_FORMAT = "Owner phone: %s";
    private static final String ADDRESS_FORMAT = "Address: %s";
    private static final String CARE_NOTES_FORMAT = "Care notes: %s";

    public final Appointment appointment;

    @FXML
    private HBox cardPane;
    @FXML
    private Label id;
    @FXML
    private Label appointmentTitle;
    @FXML
    private Label dateHeading;
    @FXML
    private Label pet;
    @FXML
    private Label owner;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label careNotes;

    // @@author xinyu-zxy
    /** Creates an {@code AppointmentCard} with the given appointment and displayed index. */
    public AppointmentCard(Appointment appointment, int displayedIndex) {
        this(appointment, displayedIndex, true);
    }

    /**
     * Creates an appointment card with an optional date heading.
     *
     * @param appointment Appointment to display.
     * @param displayedIndex Index to display; callers are expected to supply a one-based value.
     * @param showDateHeading Whether to show the heading; the panel determines date-group boundaries.
     */
    public AppointmentCard(Appointment appointment, int displayedIndex, boolean showDateHeading) {
        super(FXML);
        this.appointment = appointment;
        id.setText(String.format(INDEX_FORMAT, displayedIndex));
        dateHeading.setText(String.format(DATE_HEADING_FORMAT, appointment.getDate()));
        dateHeading.setVisible(showDateHeading);
        dateHeading.setManaged(showDateHeading);
        appointmentTitle.setText(String.format(TITLE_FORMAT, appointment.getService(),
                appointment.getStartTime(), appointment.getEndTime()));
        pet.setText(String.format(PET_FORMAT, appointment.getPetName()));
        owner.setText(String.format(OWNER_FORMAT, appointment.getOwner().getName()));
        phone.setText(String.format(PHONE_FORMAT, appointment.getOwnerPhone()));
        address.setText(String.format(ADDRESS_FORMAT, appointment.getOwner().getAddress()));
        careNotes.setText(String.format(CARE_NOTES_FORMAT, appointment.getPet().getRequirement()));
    }
    // @@author
}

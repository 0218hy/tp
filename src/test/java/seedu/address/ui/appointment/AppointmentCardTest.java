package seedu.address.ui.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.testutil.PetBuilder;
import seedu.address.ui.JavaFxTestUtils;

public class AppointmentCardTest {

    @Test
    public void constructor_displaysAppointmentDetails() {
        JavaFxTestUtils.runAndWait(() -> {
            Appointment appointment = new Appointment(ALICE, new PetBuilder(ALICE).withName("Milo").build(),
                    new AppointmentDate("01-01-2027"), new StartTime("10:00"),
                    new EndTime("11:00"), Service.FULL_GROOM);
            AppointmentCard card = new AppointmentCard(appointment, 3);

            assertEquals(appointment, card.appointment);
            assertEquals("3. ", getLabel(card, "id").getText());
            assertEquals("Appointments for 01-01-2027", getLabel(card, "dateHeading").getText());
            assertTrue(getLabel(card, "dateHeading").isVisible());
            assertEquals("Full groom (10:00-11:00)", getLabel(card, "appointmentTitle").getText());
            assertEquals("Pet: Milo", getLabel(card, "pet").getText());
            assertEquals("Owner: Alice Pauline", getLabel(card, "owner").getText());
            assertEquals("Owner phone: 94351253", getLabel(card, "phone").getText());
            assertEquals("Address: " + ALICE.getAddress(), getLabel(card, "address").getText());
            assertEquals("Care notes: Daily walk", getLabel(card, "careNotes").getText());
            assertNotNull(card.getRoot());
        });
    }

    //@@author 0218hy
    @Test
    public void constructor_longCareNotes_wrapWithoutTruncation() {
        JavaFxTestUtils.runAndWait(() -> {
            String notes = "Use fragrance-free shampoo for sensitive skin. Avoid the ears and take breaks "
                    + "during brushing. Handle paws gently and contact the owner if the pet becomes distressed.";
            Appointment appointment = new Appointment(ALICE,
                    new PetBuilder(ALICE).withRequirement(notes).build(), new AppointmentDate("01-01-2027"),
                    new StartTime("10:00"), new EndTime("11:00"), Service.FULL_GROOM);
            AppointmentCard card = new AppointmentCard(appointment, 1);
            StackPane root = new StackPane(card.getRoot());
            new Scene(root, 300, 500);
            root.applyCss();
            root.layout();

            Label careNotes = getLabel(card, "careNotes");
            assertEquals("Care notes: " + notes, careNotes.getText());
            assertTrue(careNotes.isWrapText());
            assertTrue(getLabel(card, "address").isWrapText());
            assertTrue(careNotes.getHeight() > getLabel(card, "phone").getHeight());
            assertTrue(careNotes.getHeight() >= careNotes.prefHeight(careNotes.getWidth()));
        });
    }
    //@@author

    // @@author xinyu-zxy
    @Test
    public void constructor_sameDate_hidesHeadingWithoutResettingIndex() {
        JavaFxTestUtils.runAndWait(() -> {
            // EP: appointment does not start a new date group
            Appointment appointment = new Appointment(ALICE, new PetBuilder(ALICE).build(),
                    new AppointmentDate("01-01-2027"), new StartTime("10:00"),
                    new EndTime("11:00"), Service.FULL_GROOM);
            AppointmentCard card = new AppointmentCard(appointment, 2, false);
            assertFalse(getLabel(card, "dateHeading").isVisible());
            assertFalse(getLabel(card, "dateHeading").isManaged());
            assertEquals("2. ", getLabel(card, "id").getText());
        });
    }
    // @@author

    private static Label getLabel(AppointmentCard card, String fieldName) {
        try {
            Field field = AppointmentCard.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Label) field.get(card);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}

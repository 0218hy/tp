package seedu.address.ui.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.scene.control.Label;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.ui.JavaFxTestUtils;

@EnabledOnOs(OS.LINUX)
public class AppointmentCardTest {

    @Test
    public void constructor_displaysAppointmentDetails() {
        JavaFxTestUtils.runAndWait(() -> {
            Appointment appointment = new Appointment(ALICE.getPhone(), "Milo",
                    new AppointmentDate("01-01-2027"), new StartTime("10:00"),
                    new EndTime("11:00"), Service.FULL_GROOM);
            AppointmentCard card = new AppointmentCard(appointment, 3);

            assertEquals(appointment, card.appointment);
            assertEquals("3. ", getLabel(card, "id").getText());
            assertEquals("01-01-2027 10:00-11:00", getLabel(card, "dateTime").getText());
            assertEquals("Pet: Milo", getLabel(card, "pet").getText());
            assertEquals("Owner phone: 94351253", getLabel(card, "owner").getText());
            assertEquals("Service: Full groom", getLabel(card, "service").getText());
            assertNotNull(card.getRoot());
        });
    }

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

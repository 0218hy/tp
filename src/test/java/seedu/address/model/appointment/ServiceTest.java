package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ServiceTest {
    @Test
    public void fromString_supportedServices_returnsMatchingEnum() {
        assertEquals(Service.BATH_AND_BRUSH, Service.fromString("Bath and brush"));
        assertEquals(Service.FULL_GROOM, Service.fromString("Full groom"));
        assertEquals(Service.NAIL_TRIM, Service.fromString("Nail trim"));
        assertEquals(Service.OTHER, Service.fromString("Other"));
    }

    @Test
    public void fromString_caseAndWhitespace_areIgnored() {
        assertEquals(Service.BATH_AND_BRUSH, Service.fromString("  bAtH  AND\tbrush  "));
        assertEquals(Service.FULL_GROOM, Service.fromString("FULL   GROOM"));
        assertEquals(Service.NAIL_TRIM, Service.fromString(" nail trim "));
        assertEquals(Service.OTHER, Service.fromString("other"));
    }

    @Test
    public void fromString_invalidServices_rejectedRatherThanMappedToOther() {
        for (String input : new String[]{"", " ", "Haircut", "Bath", "Full groom extra", "FULL_GROOM", "Nail/trim"}) {
            IllegalArgumentException error = assertThrows(
                    IllegalArgumentException.class, () -> Service.fromString(input), input);
            assertEquals(Service.MESSAGE_CONSTRAINTS, error.getMessage());
        }
    }

    @Test
    public void fromString_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Service.fromString(null));
    }

    @Test
    public void toString_usesReadableServiceLabels() {
        assertEquals("Bath and brush", Service.BATH_AND_BRUSH.toString());
        assertEquals("Full groom", Service.FULL_GROOM.toString());
        assertEquals("Nail trim", Service.NAIL_TRIM.toString());
        assertEquals("Other", Service.OTHER.toString());
    }
}

package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ServiceTest {
    //@@author 0218hy
    @Test
    public void fromString_supportedServices_returnsMatchingEnum() {
        // EP: each supported label maps to a distinct enum, so one representative per service is necessary.
        assertEquals(Service.BATH_AND_BRUSH, Service.fromString("Bath and brush"));
        assertEquals(Service.FULL_GROOM, Service.fromString("Full groom"));
        assertEquals(Service.NAIL_TRIM, Service.fromString("Nail trim"));
        assertEquals(Service.OTHER, Service.fromString("Other"));
    }

    @Test
    public void fromString_caseAndWhitespace_areIgnored() {
        // Normalization EPs: case, leading/trailing whitespace, repeated internal spaces, and tab separators.
        // Isolate transformations to identify which rule failed; service mappings are already tested above.
        assertEquals(Service.BATH_AND_BRUSH, Service.fromString("bAtH and brush"));
        assertEquals(Service.BATH_AND_BRUSH, Service.fromString("  Bath and brush  "));
        assertEquals(Service.BATH_AND_BRUSH, Service.fromString("Bath  and brush"));
        assertEquals(Service.BATH_AND_BRUSH, Service.fromString("Bath\tand brush"));
    }

    @Test
    public void fromString_invalidServices_rejectedRatherThanMappedToOther() {
        // EPs: empty/blank, unknown label, partial label, extra suffix, enum identifier, invalid separator.
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
    //@@author
}

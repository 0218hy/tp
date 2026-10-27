package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RequirementTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Requirement(null));
    }

    @Test
    public void constructor_invalidRequirement_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Requirement(""));
    }

    @Test
    public void isValidRequirement() {
        // EP: null input
        assertFalse(Requirement.isValidRequirement(null));

        // EP: blank requirements
        // BVA: zero characters
        assertFalse(Requirement.isValidRequirement(""));
        assertFalse(Requirement.isValidRequirement(" "));

        // EP: non-blank requirements shorter than the minimum length
        assertFalse(Requirement.isValidRequirement("-"));
        // BVA: four characters (just below the minimum length)
        assertFalse(Requirement.isValidRequirement("Care"));

        // EP: requirements within the length limits and without unsupported characters
        // BVA: five characters (minimum valid length)
        assertTrue(Requirement.isValidRequirement("Brush"));
        assertTrue(Requirement.isValidRequirement("Daily brushing"));
        assertTrue(Requirement.isValidRequirement("Bath every 2 weeks"));

        // EP: requirements containing an unsupported slash
        assertFalse(Requirement.isValidRequirement("Bath/brush"));

        // BVA: 240 characters (maximum valid length)
        assertTrue(Requirement.isValidRequirement("A".repeat(240)));
        // BVA: 241 characters (just above the maximum length)
        assertFalse(Requirement.isValidRequirement("A".repeat(241)));
    }

    //@@author khangsian0412
    @Test
    public void isValidRequirement_boundaryLengths() {
        // BVA: check values immediately below, at, and above the 5- and 240-char limits.
        assertFalse(Requirement.isValidRequirement("R".repeat(4)));
        assertTrue(Requirement.isValidRequirement("R".repeat(5)));
        assertTrue(Requirement.isValidRequirement("R".repeat(6)));

        assertTrue(Requirement.isValidRequirement("R".repeat(239)));
        assertTrue(Requirement.isValidRequirement("R".repeat(240)));
        assertFalse(Requirement.isValidRequirement("R".repeat(241)));
    }
    //@@author

    @Test
    public void equals() {
        Requirement requirement = new Requirement("Daily brushing");

        assertTrue(requirement.equals(requirement));
        assertTrue(requirement.equals(new Requirement("Daily brushing")));
        assertFalse(requirement.equals(null));
        assertFalse(requirement.equals(1));
        assertFalse(requirement.equals(new Requirement("Monthly grooming")));
    }
}

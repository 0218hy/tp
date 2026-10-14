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
        assertFalse(Requirement.isValidRequirement(null));

        assertFalse(Requirement.isValidRequirement(""));
        assertFalse(Requirement.isValidRequirement(" "));

        assertTrue(Requirement.isValidRequirement("Daily brushing"));
        assertTrue(Requirement.isValidRequirement("Bath every 2 weeks"));
        assertFalse(Requirement.isValidRequirement("-"));
        assertFalse(Requirement.isValidRequirement("Bath/brush"));
        assertFalse(Requirement.isValidRequirement("A".repeat(241)));
    }

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

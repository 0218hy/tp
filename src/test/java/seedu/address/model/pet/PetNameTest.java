package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PetNameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PetName(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PetName(""));
    }

    @Test
    public void isValidName() {
        // EP: null input
        assertFalse(PetName.isValidName(null));

        // EP: empty name
        // BVA: zero characters
        assertFalse(PetName.isValidName(""));

        // EP: blank name
        assertFalse(PetName.isValidName(" "));

        // EP: names containing unsupported characters
        assertFalse(PetName.isValidName("Milo!"));

        // EP: names containing only supported characters and within the length limits
        // BVA: one character (minimum valid length)
        assertTrue(PetName.isValidName("M"));
        assertTrue(PetName.isValidName("Milo"));
        assertTrue(PetName.isValidName("Sir Fluffy 2nd"));
        assertTrue(PetName.isValidName("12345"));
        assertTrue(PetName.isValidName("Mochi-Jr.'s"));

        // BVA: 40 characters (maximum valid length)
        assertTrue(PetName.isValidName("A".repeat(40)));
        // BVA: 41 characters (just above the maximum length)
        assertFalse(PetName.isValidName("A".repeat(41)));
    }

    @Test
    public void hasSameName_comparesNormalizedNames() {
        PetName name = new PetName("  MiLo  ");

        assertTrue(name.hasSameName(new PetName("milo")));
        assertFalse(name.hasSameName(new PetName("Luna")));
        assertThrows(NullPointerException.class, () -> name.hasSameName(null));
    }

    @Test
    public void equals() {
        PetName petName = new PetName("Milo");

        assertTrue(petName.equals(petName));
        assertTrue(petName.equals(new PetName("Milo")));
        assertFalse(petName.equals(null));
        assertFalse(petName.equals(1));
        assertFalse(petName.equals(new PetName("Luna")));
    }
}

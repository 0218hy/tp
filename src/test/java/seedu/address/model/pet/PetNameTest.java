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
        assertFalse(PetName.isValidName(null));

        assertFalse(PetName.isValidName(""));
        assertFalse(PetName.isValidName(" "));
        assertFalse(PetName.isValidName("Milo!"));

        assertTrue(PetName.isValidName("Milo"));
        assertTrue(PetName.isValidName("Sir Fluffy 2nd"));
        assertTrue(PetName.isValidName("12345"));
        assertTrue(PetName.isValidName("Mochi-Jr.'s"));
        assertFalse(PetName.isValidName("A".repeat(41)));
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

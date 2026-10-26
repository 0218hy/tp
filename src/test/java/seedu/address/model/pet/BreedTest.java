package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class BreedTest {

    @Test
    public void isValidBreed_boundaryLengths() {
        // Boundary value analysis: check values immediately below, at, and above the 2- and 50-char limits.
        assertFalse(Breed.isValidBreed("B"));
        assertTrue(Breed.isValidBreed("Be"));
        assertTrue(Breed.isValidBreed("Bea"));

        assertTrue(Breed.isValidBreed("B".repeat(49)));
        assertTrue(Breed.isValidBreed("B".repeat(50)));
        assertFalse(Breed.isValidBreed("B".repeat(51)));
    }
}

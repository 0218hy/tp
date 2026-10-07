package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

public class SpeciesTest {

    @Test
    public void values_containsSupportedGroomingSpeciesAndOtherFallback() {
        assertEquals(EnumSet.of(Species.DOG, Species.CAT, Species.RABBIT, Species.GUINEA_PIG, Species.OTHER),
                EnumSet.allOf(Species.class));
    }
}

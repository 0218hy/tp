package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.testutil.PetBuilder;

/** Tests collection ownership and validation at the pet book boundary. */
public class PetBookTest {
    private final Pet milo = new PetBuilder(ALICE).build();

    @Test
    public void constructor_copiesSourceWithoutSharingMutableList() {
        PetBook original = new PetBook();
        original.addPet(milo);
        PetBook copy = new PetBook(original);
        original.removePet(milo);

        assertEquals(List.of(milo), copy.getPetList());
    }

    @Test
    public void resetData_duplicatePets_rejectsWithoutLosingExistingData() {
        PetBook book = new PetBook();
        book.addPet(milo);
        Pet luna = new PetBuilder(ALICE).withName("Luna").build();
        ReadOnlyPetBook invalid = () -> FXCollections.observableArrayList(luna, luna);

        assertThrows(DuplicatePetException.class, () -> book.resetData(invalid));
        assertEquals(List.of(milo), book.getPetList());
    }

    @Test
    public void resetData_validReplacement_updatesExistingReadOnlyView() {
        PetBook book = new PetBook();
        var view = book.getPetList();
        PetBook replacement = new PetBook();
        replacement.addPet(milo);

        book.resetData(replacement);

        assertEquals(List.of(milo), view);
        assertThrows(UnsupportedOperationException.class, () -> view.clear());
    }

    @Test
    public void setPet_duplicateIdentity_preservesBothPets() {
        PetBook book = new PetBook();
        Pet luna = new PetBuilder(ALICE).withName("Luna").build();
        book.addPet(milo);
        book.addPet(luna);

        assertThrows(DuplicatePetException.class, () -> book.setPet(milo, luna));
        assertEquals(List.of(milo, luna), book.getPetList());
    }

    @Test
    public void removePet_onlyRemovesRequestedRecord() {
        PetBook book = new PetBook();
        Pet luna = new PetBuilder(ALICE).withName("Luna").build();
        book.addPet(milo);
        book.addPet(luna);

        book.removePet(milo);

        assertFalse(book.hasPet(milo));
        assertEquals(List.of(luna), book.getPetList());
    }
}

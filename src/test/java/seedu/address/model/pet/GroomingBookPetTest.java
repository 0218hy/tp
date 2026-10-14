package seedu.address.model.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalClients.ALICE;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import seedu.address.model.GroomingBook;
import seedu.address.model.ReadOnlyGroomingBook;
import seedu.address.model.pet.exceptions.DuplicatePetException;
import seedu.address.testutil.PetBuilder;

/** Tests collection ownership and validation at the grooming book boundary. */
public class GroomingBookPetTest {
    private final Pet milo = new PetBuilder(ALICE).build();

    @Test
    public void constructor_copiesSourceWithoutSharingMutableList() {
        GroomingBook original = bookWithOwner();
        original.addPet(milo);
        GroomingBook copy = new GroomingBook(original);
        original.removePet(milo);

        assertEquals(List.of(milo), copy.getPetList());
    }

    @Test
    public void resetData_duplicatePets_rejectsWithoutLosingExistingData() {
        GroomingBook book = bookWithOwner();
        book.addPet(milo);
        Pet luna = new PetBuilder(ALICE).withName("Luna").build();
        ReadOnlyGroomingBook invalid = new GroomingBook(book) {
            @Override
            public javafx.collections.ObservableList<Pet> getPetList() {
                return FXCollections.observableArrayList(luna, luna);
            }
        };

        assertThrows(DuplicatePetException.class, () -> book.resetData(invalid));
        assertEquals(List.of(milo), book.getPetList());
    }

    @Test
    public void resetData_validReplacement_updatesExistingReadOnlyView() {
        GroomingBook book = bookWithOwner();
        var view = book.getPetList();
        GroomingBook replacement = bookWithOwner();
        replacement.addPet(milo);

        book.resetData(replacement);

        assertEquals(List.of(milo), view);
        assertThrows(UnsupportedOperationException.class, () -> view.clear());
    }

    @Test
    public void setPet_duplicateIdentity_preservesBothPets() {
        GroomingBook book = bookWithOwner();
        Pet luna = new PetBuilder(ALICE).withName("Luna").build();
        book.addPet(milo);
        book.addPet(luna);

        assertThrows(DuplicatePetException.class, () -> book.setPet(milo, luna));
        assertEquals(List.of(milo, luna), book.getPetList());
    }

    @Test
    public void removePet_onlyRemovesRequestedRecord() {
        GroomingBook book = bookWithOwner();
        Pet luna = new PetBuilder(ALICE).withName("Luna").build();
        book.addPet(milo);
        book.addPet(luna);

        book.removePet(milo);

        assertFalse(book.hasPet(milo));
        assertEquals(List.of(luna), book.getPetList());
    }
    private GroomingBook bookWithOwner() {
        GroomingBook book = new GroomingBook();
        book.addClient(ALICE);
        return book;
    }
}


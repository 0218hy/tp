package seedu.address.storage;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.model.client.ClientBook;
import seedu.address.model.client.ReadOnlyClientBook;
import seedu.address.model.pet.PetBook;
import seedu.address.model.pet.ReadOnlyPetBook;

/** A snapshot of the two books stored together in the existing client and pet JSON file. */
public final class ClientPetData {
    private final ClientBook clientBook;
    private final PetBook petBook;

    /** Copies both books so subsequent model changes cannot affect the saved snapshot. */
    public ClientPetData(ReadOnlyClientBook clientBook, ReadOnlyPetBook petBook) {
        requireAllNonNull(clientBook, petBook);
        this.clientBook = new ClientBook(clientBook);
        this.petBook = new PetBook(petBook);
    }

    public ReadOnlyClientBook getClientBook() {
        return clientBook;
    }

    public ReadOnlyPetBook getPetBook() {
        return petBook;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClientPetData otherData
                && clientBook.equals(otherData.clientBook) && petBook.equals(otherData.petBook);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientBook, petBook);
    }
}

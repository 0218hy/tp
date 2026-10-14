package seedu.address.testutil;

import seedu.address.model.GroomingBook;
import seedu.address.model.client.Client;

/**
 * A utility class to help with building GroomingBook objects.
 * Example usage: <br>
 *     {@code GroomingBook ab = new GroomingBookBuilder().withClient("John", "Doe").build();}
 */
public class GroomingBookBuilder {

    private GroomingBook groomingBook;

    public GroomingBookBuilder() {
        groomingBook = new GroomingBook();
    }

    public GroomingBookBuilder(GroomingBook groomingBook) {
        this.groomingBook = groomingBook;
    }

    /**
     * Adds a new {@code Client} to the {@code GroomingBook} that we are building.
     */
    public GroomingBookBuilder withClient(Client client) {
        groomingBook.addClient(client);
        return this;
    }

    public GroomingBook build() {
        return groomingBook;
    }
}

package seedu.address.testutil;

import seedu.address.model.GroomingBook;
import seedu.address.model.client.Client;

/**
 * A utility class to help with building GroomingBook objects.
 * Example usage: <br>
 *     {@code GroomingBook ab = new GroomingBookBuilder().withClient("John", "Doe").build();}
 */
public class GroomingBookBuilder {

    private GroomingBook clientBook;

    public GroomingBookBuilder() {
        clientBook = new GroomingBook();
    }

    public GroomingBookBuilder(GroomingBook clientBook) {
        this.clientBook = clientBook;
    }

    /**
     * Adds a new {@code Client} to the {@code GroomingBook} that we are building.
     */
    public GroomingBookBuilder withClient(Client client) {
        clientBook.addClient(client);
        return this;
    }

    public GroomingBook build() {
        return clientBook;
    }
}

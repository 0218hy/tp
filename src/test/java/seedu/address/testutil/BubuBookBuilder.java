package seedu.address.testutil;

import seedu.address.model.BubuBook;
import seedu.address.model.client.Client;

/**
 * A utility class to help with building BubuBook objects.
 * Example usage: <br>
 *     {@code BubuBook ab = new BubuBookBuilder().withClient("John", "Doe").build();}
 */
public class BubuBookBuilder {

    private BubuBook bubuBook;

    public BubuBookBuilder() {
        bubuBook = new BubuBook();
    }

    public BubuBookBuilder(BubuBook bubuBook) {
        this.bubuBook = bubuBook;
    }

    /**
     * Adds a new {@code Client} to the {@code BubuBook} that we are building.
     */
    public BubuBookBuilder withClient(Client client) {
        bubuBook.addClient(client);
        return this;
    }

    public BubuBook build() {
        return bubuBook;
    }
}

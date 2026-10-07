package seedu.address.model.client;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;

/** Stores unique clients. Relationships with other books are managed by ModelManager. */
public class ClientBook implements ReadOnlyClientBook {
    private final UniqueClientList clients = new UniqueClientList();

    public ClientBook() {}

    public ClientBook(ReadOnlyClientBook source) {
        resetData(source);
    }

    /** Replaces this book's contents with a validated copy of the supplied data. */
    public void resetData(ReadOnlyClientBook source) {
        requireNonNull(source);
        setClients(source.getClientList());
    }

    public void setClients(List<Client> clients) {
        this.clients.setClients(clients);
    }

    /** Returns whether a client with the same identity exists. */
    public boolean hasClient(Client client) {
        return clients.contains(client);
    }

    /** Adds a client that does not duplicate an existing record. */
    public void addClient(Client client) {
        clients.add(client);
    }

    public void setClient(Client target, Client editedClient) {
        clients.setClient(target, editedClient);
    }

    /** Removes an existing client. */
    public void removeClient(Client client) {
        clients.remove(client);
    }

    @Override
    public ObservableList<Client> getClientList() {
        return clients.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClientBook otherBook && clients.equals(otherBook.clients);
    }

    @Override
    public int hashCode() {
        return clients.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("clients", clients).toString();
    }
}

package seedu.address.model.client;

import javafx.collections.ObservableList;

/** Unmodifiable view of the client book. */
public interface ReadOnlyClientBook {
    /** Returns the unique clients in insertion order. */
    ObservableList<Client> getClientList();
}

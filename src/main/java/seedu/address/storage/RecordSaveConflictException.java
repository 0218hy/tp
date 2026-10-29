package seedu.address.storage;

import java.io.IOException;

/** Indicates that saving would conflict with another writer or with changed records on disk. */
public class RecordSaveConflictException extends IOException {
    public static final String MESSAGE_CHANGED = "Records changed outside this BuBu window. "
            + "Close and reopen BuBu, then retry your command.";
    public static final String MESSAGE_BUSY = "Another BuBu window is saving records. Retry your command.";

    //@@author 0218hy
    public RecordSaveConflictException(String message) {
        super(message);
    }
    //@@author
}

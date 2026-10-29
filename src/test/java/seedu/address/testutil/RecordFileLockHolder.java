package seedu.address.testutil;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Holds a record-file lock in a separate JVM until its parent test signals release. */
public class RecordFileLockHolder {
    //@@author 0218hy
    public static void main(String[] args) throws Exception {
        try (FileChannel channel = FileChannel.open(Path.of(args[0]),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock = channel.lock()) {
            if (lock.isValid()) {
                System.out.println("locked");
                System.out.flush();
                System.in.read();
            }
        }
    }
    //@@author
}

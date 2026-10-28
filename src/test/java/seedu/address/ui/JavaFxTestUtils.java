package seedu.address.ui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;

/**
 * Utilities for executing assertions on the JavaFX application thread.
 * Toolkit startup and dispatched actions each have a 30-second deadline.
 */
public final class JavaFxTestUtils {

    private static final long WAIT_TIMEOUT_SECONDS = 30;
    private static boolean isToolkitStarted;

    private JavaFxTestUtils() {}

    //@@author khangsian0412
    /** Executes the action on the JavaFX application thread, waiting at most 30 seconds for completion. */
    public static synchronized void runAndWait(Runnable action) {
        startToolkitIfNeeded();

        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch completion = new CountDownLatch(1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                thrown.set(throwable);
            } finally {
                completion.countDown();
            }
        });
        await(completion, "dispatched action", WAIT_TIMEOUT_SECONDS);

        if (thrown.get() != null) {
            throw new AssertionError(thrown.get());
        }
    }

    private static void startToolkitIfNeeded() {
        if (isToolkitStarted) {
            return;
        }

        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException exception) {
            startup.countDown();
        }
        await(startup, "toolkit startup", WAIT_TIMEOUT_SECONDS);
        isToolkitStarted = true;
    }

    /** Waits for a JavaFX phase to finish, failing with its name if the deadline expires. */
    static void await(CountDownLatch latch, String phase, long timeoutSeconds) {
        try {
            if (!latch.await(timeoutSeconds, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out after " + timeoutSeconds + " seconds waiting for JavaFX "
                        + phase + ".");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for JavaFX " + phase + ".", exception);
        }
    }
    //@@author
}

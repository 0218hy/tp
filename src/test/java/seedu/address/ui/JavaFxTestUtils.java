package seedu.address.ui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * Utilities for executing assertions on the JavaFX application thread.
 */
public final class JavaFxTestUtils {

    private static boolean isToolkitStarted;

    private JavaFxTestUtils() {}

    //@@author khangsian0412
    /** Executes the action on the JavaFX application thread and waits for it to finish. */
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
        await(completion);

        if (thrown.get() != null) {
            throw new AssertionError(thrown.get());
        }
    }

    /** Applies CSS and lays out the root of the given scene. */
    public static void layoutScene(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    /** Returns the visible list cell at the given index. */
    public static <T extends ListCell<?>> T getCellAt(ListView<?> listView, int index,
            Class<T> cellClass) {
        for (Node node : listView.lookupAll(".list-cell")) {
            if (cellClass.isInstance(node) && ((ListCell<?>) node).getIndex() == index) {
                return cellClass.cast(node);
            }
        }
        throw new AssertionError("No visible list cell at index " + index);
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
        await(startup);
        isToolkitStarted = true;
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for the JavaFX application thread.", exception);
        }
    }
    //@@author
}

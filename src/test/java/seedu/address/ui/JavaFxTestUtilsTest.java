package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

/** Tests the bounded waits used by JavaFX UI tests. */
public class JavaFxTestUtilsTest {

    @Test
    public void await_toolkitStartupDoesNotFinish_reportsStartupTimeout() {
        AssertionError failure = assertThrows(AssertionError.class, () ->
                JavaFxTestUtils.await(new CountDownLatch(1), "toolkit startup", 0));

        assertEquals("Timed out after 0 seconds waiting for JavaFX toolkit startup.", failure.getMessage());
    }

    @Test
    public void await_dispatchedActionDoesNotFinish_reportsActionTimeout() {
        AssertionError failure = assertThrows(AssertionError.class, () ->
                JavaFxTestUtils.await(new CountDownLatch(1), "dispatched action", 0));

        assertEquals("Timed out after 0 seconds waiting for JavaFX dispatched action.", failure.getMessage());
    }

    @Test
    public void await_interrupted_preservesInterruptStatus() {
        boolean wasInterrupted = Thread.currentThread().isInterrupted();
        Thread.currentThread().interrupt();
        try {
            AssertionError failure = assertThrows(AssertionError.class, () ->
                    JavaFxTestUtils.await(new CountDownLatch(1), "dispatched action", 30));

            assertEquals("Interrupted while waiting for JavaFX dispatched action.", failure.getMessage());
            assertInstanceOf(InterruptedException.class, failure.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            if (!wasInterrupted) {
                Thread.interrupted();
            }
        }
    }
}

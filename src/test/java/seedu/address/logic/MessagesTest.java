//@@author anderschow
package seedu.address.logic;

import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class MessagesTest {
    @Test
    public void getErrorMessageForDuplicatePrefixes_noDuplicates_rejectsInvalidCaller() {
        assertThrows(AssertionError.class, "Duplicate-prefix reporting requires at least one detected duplicate",
                Messages::getErrorMessageForDuplicatePrefixes);
    }
}
//@@author

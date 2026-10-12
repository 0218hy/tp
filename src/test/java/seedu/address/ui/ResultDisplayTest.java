package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ResultDisplayTest {

    @Test
    public void getHelpLineStyleClass_heading_returnsHeadingStyle() {
        assertEquals("help-heading", ResultDisplay.getHelpLineStyleClass("AVAILABLE COMMANDS"));
    }

    @Test
    public void getHelpLineStyleClass_commandName_returnsCommandNameStyle() {
        assertEquals("help-command-name", ResultDisplay.getHelpLineStyleClass("Add client"));
    }

    @Test
    public void getHelpLineStyleClass_commandFormat_returnsCommandFormatStyle() {
        assertEquals("help-command-format",
                ResultDisplay.getHelpLineStyleClass("  add-client n/NAME i/PHONE_NUMBER"));
    }

    @Test
    public void getHelpLineStyleClass_blankLine_returnsSpacingStyle() {
        assertEquals("help-spacing", ResultDisplay.getHelpLineStyleClass("   "));
    }
}

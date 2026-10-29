package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import javafx.css.PseudoClass;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ResultDisplayTest {

    //@@author khangsian0412
    @Test
    public void setHelpContent_displaysStyledHelpReference() {
        JavaFxTestUtils.runAndWait(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();

            resultDisplay.setHelpContent("AVAILABLE COMMANDS\n\nAdd client\n  add-client n/NAME");

            TextArea feedbackDisplay = getField(resultDisplay, "resultDisplay", TextArea.class);
            ScrollPane helpDisplay = getField(resultDisplay, "helpDisplay", ScrollPane.class);
            TextFlow helpContent = getField(resultDisplay, "helpContent", TextFlow.class);
            assertFalse(feedbackDisplay.isVisible());
            assertTrue(helpDisplay.isVisible());
            assertTrue(((Text) helpContent.getChildren().get(0)).getStyleClass().contains("help-heading"));
            assertTrue(((Text) helpContent.getChildren().get(2)).getStyleClass().contains("help-command-name"));
            assertTrue(((Text) helpContent.getChildren().get(3)).getStyleClass().contains("help-command-format"));
        });
    }

    @Test
    public void setFeedbackToUser_hidesHelpReferenceAndDisplaysFeedback() {
        JavaFxTestUtils.runAndWait(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();
            resultDisplay.setHelpContent("AVAILABLE COMMANDS");

            resultDisplay.setFeedbackToUser("Listed all clients.");

            TextArea feedbackDisplay = getField(resultDisplay, "resultDisplay", TextArea.class);
            ScrollPane helpDisplay = getField(resultDisplay, "helpDisplay", ScrollPane.class);
            assertTrue(feedbackDisplay.isVisible());
            assertFalse(helpDisplay.isVisible());
            assertEquals("Listed all clients.", feedbackDisplay.getText());
        });
    }
    //@@author

    // @@author xinyu-zxy
    @Test
    public void setErrorFeedbackToUser_afterSuccess_displaysError() {
        JavaFxTestUtils.runAndWait(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();

            // EP: error feedback replaces existing success feedback
            resultDisplay.setFeedbackToUser("Pet added.");

            resultDisplay.setErrorFeedbackToUser("Owner not found.\nPlease add the client first.");

            TextArea feedbackDisplay = getField(resultDisplay, "resultDisplay", TextArea.class);
            assertEquals("Owner not found.\nPlease add the client first.", feedbackDisplay.getText());
            assertFeedbackState(feedbackDisplay, false, true);
        });
    }
    // @@author

    // @@author xinyu-zxy
    @Test
    public void setFeedbackToUser_afterError_displaysSuccess() {
        JavaFxTestUtils.runAndWait(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();

            // EP: success feedback replaces existing error feedback
            resultDisplay.setErrorFeedbackToUser("Invalid command.");

            resultDisplay.setFeedbackToUser("Listed all pets.");

            TextArea feedbackDisplay = getField(resultDisplay, "resultDisplay", TextArea.class);
            assertEquals("Listed all pets.", feedbackDisplay.getText());
            assertFeedbackState(feedbackDisplay, true, false);
        });
    }

    @Test
    public void setHelpContent_afterError_clearsFeedbackState() {
        JavaFxTestUtils.runAndWait(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();

            // EP: help content replaces error feedback and clears both feedback states
            resultDisplay.setErrorFeedbackToUser("Invalid command.");

            resultDisplay.setHelpContent("AVAILABLE COMMANDS");

            TextArea feedbackDisplay = getField(resultDisplay, "resultDisplay", TextArea.class);
            assertFeedbackState(feedbackDisplay, false, false);
            assertFalse(feedbackDisplay.isVisible());

            resultDisplay.setErrorFeedbackToUser("Invalid command.");
            assertTrue(feedbackDisplay.isVisible());
            assertFalse(getField(resultDisplay, "helpDisplay", ScrollPane.class).isVisible());
            assertFeedbackState(feedbackDisplay, false, true);
        });
    }
    // @@author

    //@@author khangsian0412
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
    //@@author

    // @@author xinyu-zxy
    private static void assertFeedbackState(TextArea feedbackDisplay, boolean isSuccess, boolean isError) {
        assertEquals(isSuccess,
                feedbackDisplay.getPseudoClassStates().contains(PseudoClass.getPseudoClass("feedback-success")));
        assertEquals(isError,
                feedbackDisplay.getPseudoClassStates().contains(PseudoClass.getPseudoClass("feedback-error")));
    }
    // @@author

    //@@author khangsian0412
    private static <T> T getField(Object target, String fieldName, Class<T> fieldType) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return fieldType.cast(field.get(target));
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new AssertionError(exception);
        }
    }
    //@@author
}

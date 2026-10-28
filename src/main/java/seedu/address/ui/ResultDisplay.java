package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final PseudoClass SUCCESS = PseudoClass.getPseudoClass("feedback-success");
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("feedback-error");

    @FXML
    private TextArea resultDisplay;

    @FXML
    private ScrollPane helpDisplay;

    @FXML
    private TextFlow helpContent;

    public ResultDisplay() {
        super(FXML);
    }

    // @@author xinyu-zxy
    /**
     * Displays successful command feedback and clears any previous error styling.
     *
     * @param feedbackToUser Feedback to display.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        showFeedback(feedbackToUser, false);
    }

    /**
     * Displays failed command feedback and clears any previous success styling.
     *
     * @param feedbackToUser Feedback to display.
     */
    public void setErrorFeedbackToUser(String feedbackToUser) {
        showFeedback(feedbackToUser, true);
    }

    /**
     * Replaces any help content with command feedback and applies the matching visual state.
     *
     * @param feedbackToUser Non-null message to display.
     * @param isError Whether to use error styling instead of success styling.
     */
    private void showFeedback(String feedbackToUser, boolean isError) {
        requireNonNull(feedbackToUser);
        showFeedbackDisplay();
        resultDisplay.pseudoClassStateChanged(SUCCESS, !isError);
        resultDisplay.pseudoClassStateChanged(ERROR, isError);
        resultDisplay.setText(feedbackToUser);
    }
    // @@author

    /**
     * Displays the command reference with visually distinct command names and formats.
     */
    public void setHelpContent(String commandReference) {
        requireNonNull(commandReference);
        resultDisplay.pseudoClassStateChanged(SUCCESS, false);
        resultDisplay.pseudoClassStateChanged(ERROR, false);
        showHelpDisplay();
        helpContent.getChildren().clear();

        for (String line : commandReference.split("\\R", -1)) {
            Text text = new Text(line + System.lineSeparator());
            text.getStyleClass().add(getHelpLineStyleClass(line));
            helpContent.getChildren().add(text);
        }
        helpDisplay.setVvalue(0);
    }

    //@@author khangsian0412
    private void showFeedbackDisplay() {
        resultDisplay.setManaged(true);
        resultDisplay.setVisible(true);
        helpDisplay.setManaged(false);
        helpDisplay.setVisible(false);
    }

    private void showHelpDisplay() {
        resultDisplay.setManaged(false);
        resultDisplay.setVisible(false);
        helpDisplay.setManaged(true);
        helpDisplay.setVisible(true);
    }

    static String getHelpLineStyleClass(String line) {
        if (line.isBlank()) {
            return "help-spacing";
        }
        if (line.startsWith("  ")) {
            return "help-command-format";
        }
        if (line.equals("AVAILABLE COMMANDS")) {
            return "help-heading";
        }
        return "help-command-name";
    }
    //@@author

}

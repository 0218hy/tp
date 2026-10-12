package seedu.address.ui;

import static java.util.Objects.requireNonNull;

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

    @FXML
    private TextArea resultDisplay;

    @FXML
    private ScrollPane helpDisplay;

    @FXML
    private TextFlow helpContent;

    public ResultDisplay() {
        super(FXML);
    }

    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        showFeedbackDisplay();
        resultDisplay.setText(feedbackToUser);
    }

    /**
     * Displays the command reference with visually distinct command names and formats.
     */
    public void setHelpContent(String commandReference) {
        requireNonNull(commandReference);
        showHelpDisplay();
        helpContent.getChildren().clear();

        for (String line : commandReference.split("\\R", -1)) {
            Text text = new Text(line + System.lineSeparator());
            text.getStyleClass().add(getHelpLineStyleClass(line));
            helpContent.getChildren().add(text);
        }
        helpDisplay.setVvalue(0);
    }

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

    private String getHelpLineStyleClass(String line) {
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

}

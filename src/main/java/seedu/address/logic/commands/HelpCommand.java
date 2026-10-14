package seedu.address.logic.commands;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import seedu.address.model.Model;

/**
 * Formats full help instructions for every command for display.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows program usage instructions.\n"
            + "Example: " + COMMAND_WORD;

    private static final String COMMAND_REFERENCE_PATH = "/help/CommandReference.md";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(getCommandReference(), true, false);
    }

    /**
     * Loads the concise command reference bundled with the application.
     */
    static String getCommandReference() {
        try (InputStream commandReferenceStream = HelpCommand.class.getResourceAsStream(COMMAND_REFERENCE_PATH)) {
            if (commandReferenceStream == null) {
                throw new IllegalStateException("Command reference resource is missing.");
            }
            return new String(commandReferenceStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load the command reference.", exception);
        }
    }
}

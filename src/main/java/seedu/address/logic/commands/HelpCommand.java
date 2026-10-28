package seedu.address.logic.commands;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Formats full help instructions for every command for display.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows program usage instructions.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_UNABLE_TO_LOAD_COMMAND_REFERENCE =
            "Unable to load the command reference. Check that you are using a complete BuBu release JAR.";

    private static final String COMMAND_REFERENCE_PATH = "/help/CommandReference.md";
    private static final Logger logger = LogsCenter.getLogger(HelpCommand.class);

    private final CommandReferenceLoader commandReferenceLoader;

    /** Creates a help command that reads the command reference bundled with the application. */
    public HelpCommand() {
        this(() -> HelpCommand.class.getResourceAsStream(COMMAND_REFERENCE_PATH));
    }

    /** Creates a help command with a resource loader, for testing loading failures. */
    HelpCommand(CommandReferenceLoader commandReferenceLoader) {
        this.commandReferenceLoader = commandReferenceLoader;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        return new CommandResult(getCommandReference(commandReferenceLoader), true, false);
    }

    //@@author khangsian0412
    /**
     * Loads the concise command reference bundled with the application.
     */
    static String getCommandReference() throws CommandException {
        return getCommandReference(() -> HelpCommand.class.getResourceAsStream(COMMAND_REFERENCE_PATH));
    }

    private static String getCommandReference(CommandReferenceLoader commandReferenceLoader) throws CommandException {
        try (InputStream commandReferenceStream = commandReferenceLoader.open()) {
            if (commandReferenceStream == null) {
                logger.warning("Missing command reference resource: " + COMMAND_REFERENCE_PATH);
                throw new CommandException(MESSAGE_UNABLE_TO_LOAD_COMMAND_REFERENCE);
            }
            return new String(commandReferenceStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            logger.log(Level.WARNING, "Unable to read command reference resource: " + COMMAND_REFERENCE_PATH,
                    exception);
            throw new CommandException(MESSAGE_UNABLE_TO_LOAD_COMMAND_REFERENCE, exception);
        }
    }

    @FunctionalInterface
    interface CommandReferenceLoader {
        InputStream open() throws IOException;
    }
    //@@author
}

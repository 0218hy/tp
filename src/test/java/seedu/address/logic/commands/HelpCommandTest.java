package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.BubuBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyBubuBook;
import seedu.address.testutil.TypicalClients;

public class HelpCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void execute_help_success() throws Exception {
        CommandResult expectedCommandResult = new CommandResult(HelpCommand.getCommandReference(), true, false);
        assertCommandSuccess(new HelpCommand(), model, expectedCommandResult, expectedModel);
    }

    @Test
    public void execute_missingCommandReference_throwsCommandExceptionAndPreservesRecords() {
        model.addClient(TypicalClients.ALICE);
        ReadOnlyBubuBook recordsBeforeExecution = new BubuBook(model.getBubuBook());
        List<LogRecord> logRecords = captureHelpCommandLogs(() -> {
            HelpCommand command = new HelpCommand(() -> null);
            CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
            assertEquals(HelpCommand.MESSAGE_UNABLE_TO_LOAD_COMMAND_REFERENCE, exception.getMessage());
            assertNull(exception.getCause());
        });

        assertEquals(recordsBeforeExecution, model.getBubuBook());
        assertEquals(1, logRecords.size());
        assertEquals(Level.WARNING, logRecords.get(0).getLevel());
        assertTrue(logRecords.get(0).getMessage().contains("/help/CommandReference.md"));
        assertNull(logRecords.get(0).getThrown());
    }

    @Test
    public void execute_unreadableCommandReference_throwsCommandExceptionAndPreservesDiagnostics() {
        model.addClient(TypicalClients.ALICE);
        ReadOnlyBubuBook recordsBeforeExecution = new BubuBook(model.getBubuBook());
        IOException readFailure = new IOException("simulated read failure");
        List<LogRecord> logRecords = captureHelpCommandLogs(() -> {
            InputStream unreadableStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw readFailure;
                }
            };
            HelpCommand command = new HelpCommand(() -> unreadableStream);
            CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
            assertEquals(HelpCommand.MESSAGE_UNABLE_TO_LOAD_COMMAND_REFERENCE, exception.getMessage());
            assertSame(readFailure, exception.getCause());
        });

        assertEquals(recordsBeforeExecution, model.getBubuBook());
        assertEquals(1, logRecords.size());
        assertEquals(Level.WARNING, logRecords.get(0).getLevel());
        assertTrue(logRecords.get(0).getMessage().contains("/help/CommandReference.md"));
        assertSame(readFailure, logRecords.get(0).getThrown());
    }

    //@@author anderschow
    @Test
    public void getCommandReference_containsAllDocumentedCommands() throws Exception {
        String commandReference = HelpCommand.getCommandReference();

        assertTrue(commandReference.contains("help"));
        assertTrue(commandReference.contains("add-client n/NAME i/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]..."));
        assertTrue(commandReference.contains(
                "edit-client i/CURRENT_PHONE [n/NAME] [p/NEW_PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]..."));
        assertTrue(commandReference.contains("delete-client INDEX"));
        assertTrue(commandReference.contains("delete-client i/PHONE_NUMBER"));
        assertTrue(commandReference.contains(
                "add-pet p/PET_NAME i/OWNER_IDENTIFIER s/SPECIES [b/BREED] r/REQUIREMENTS"));
        assertTrue(commandReference.contains("list-clients [q/KEYWORD]"));
        assertTrue(commandReference.contains("list-pets [i/OWNER_IDENTIFIER]"));
        assertTrue(commandReference.contains(
                "edit-pet p/PET_NAME i/OWNER_IDENTIFIER [n/NEW_PET_NAME] [s/SPECIES] [b/BREED] [r/REQUIREMENTS]"));
        assertTrue(commandReference.contains("delete-pet INDEX"));
        assertTrue(commandReference.contains("delete-pet p/PET_NAME i/OWNER_IDENTIFIER"));
        assertTrue(commandReference.contains(
                "add-appointment i/OWNER_IDENTIFIER p/PET_NAME d/DD-MM-YYYY st/HH:mm et/HH:mm s/SERVICE"));
        assertTrue(commandReference.contains("list-appointments"));
        assertTrue(commandReference.contains("delete-appointment INDEX"));
        assertTrue(commandReference.contains("delete-appointment d/DD-MM-YYYY st/HH:mm"));
        assertTrue(commandReference.contains("clear"));
        assertTrue(commandReference.contains("exit"));
    }

    /** Captures warning diagnostics emitted while executing a simulated help reference failure. */
    private List<LogRecord> captureHelpCommandLogs(Runnable action) {
        Logger logger = Logger.getLogger("ab3.HelpCommand");
        List<LogRecord> records = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        try {
            action.run();
        } finally {
            logger.removeHandler(handler);
        }
        return records;
    }
    //@@author
}

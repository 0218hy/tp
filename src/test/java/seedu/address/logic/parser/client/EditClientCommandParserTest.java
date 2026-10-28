package seedu.address.logic.parser.client;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_IDENTIFIER;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_NEW_PHONE;

import org.junit.jupiter.api.Test;

//@@author anderschow
import seedu.address.commons.core.index.Index;
//@@author
import seedu.address.logic.Messages;
import seedu.address.logic.commands.client.EditClientCommand;
import seedu.address.logic.commands.client.EditClientCommand.EditClientDescriptor;
//@@author anderschow
import seedu.address.logic.parser.ParserUtil;
//@@author
import seedu.address.model.client.Address;
import seedu.address.model.client.ClientName;
import seedu.address.model.client.Email;
import seedu.address.model.client.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.ClientUtil;
import seedu.address.testutil.EditClientDescriptorBuilder;

public class EditClientCommandParserTest {
    //@@author anderschow
    private final EditClientCommandParser parser = new EditClientCommandParser();
    private final Phone identifier = new Phone("91234567");
    //@@author

    //@@author anderschow
    @Test
    public void parse_index_success() {
        assertParseSuccess(parser, " 1 n/Alex Tan p/89998888 t/ ",
                new EditClientCommand(Index.fromOneBased(1), new EditClientDescriptorBuilder()
                        .withName("Alex Tan").withPhone("89998888").withTags().build()));
    }

    @Test
    public void parse_invalidIndex_failure() {
        for (String index : new String[]{"0", "-1", "1.5", "2147483648"}) {
            assertParseFailure(parser, index + " n/Alex Tan", ParserUtil.MESSAGE_INVALID_INDEX);
        }
        assertParseFailure(parser, "1", EditClientCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, "1 p/123", EditClientCommandParser.MESSAGE_INVALID_NEW_PHONE);
    }

    @Test
    public void parse_generatedDescriptor_updatesPhoneWithoutReplacingIdentifier() {
        EditClientDescriptor descriptor = new EditClientDescriptorBuilder().withPhone("89998888").build();
        assertParseSuccess(parser, "i/91234567 " + ClientUtil.getEditClientDescriptorDetails(descriptor),
                new EditClientCommand(identifier, descriptor));
    }

    @Test
    public void parse_missingIdentifierOrIndex_failure() {
        for (String input : new String[]{"", "n/Alex Tan"}) {
            assertParseFailure(parser, input, EditClientCommandParser.MESSAGE_MISSING_TARGET);
        }
        assertParseFailure(parser, "i/ n/Alex Tan", EditClientCommandParser.MESSAGE_BLANK_IDENTIFIER);
        assertParseFailure(parser, "1 i/91234567 n/Alex Tan", EditClientCommandParser.MESSAGE_MIXED_TARGETS);
        for (String input : new String[]{"abc", "1 2", "hello i/91234567"}) {
            assertParseFailure(parser, input + " n/Alex Tan", EditClientCommandParser.MESSAGE_UNEXPECTED_TEXT);
        }
    }

    @Test
    public void parse_identifierWithoutUpdate_failure() {
        assertParseFailure(parser, "i/91234567", EditClientCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_allFields_success() {
        assertParseSuccess(parser,
                "i/91234567 n/Alex Tan p/89998888 e/alex@example.com a/10 Clementi Road t/friend t/regular",
                new EditClientCommand(identifier, new EditClientDescriptorBuilder().withName("Alex Tan")
                        .withPhone("89998888").withEmail("alex@example.com").withAddress("10 Clementi Road")
                        .withTags("friend", "regular").build()));
    }

    @Test
    public void parse_identifierAfterUpdate_success() {
        assertParseSuccess(parser, " n/Alex Tan i/91234567 ",
                new EditClientCommand(identifier, new EditClientDescriptorBuilder().withName("Alex Tan").build()));
    }

    @Test
    public void parse_clearTags_success() {
        assertParseSuccess(parser, "i/91234567 t/",
                new EditClientCommand(identifier, new EditClientDescriptorBuilder().withTags().build()));
    }

    @Test
    public void parse_invalidFields_failure() {
        assertParseFailure(parser, "i/123 n/Alex Tan", EditClientCommandParser.MESSAGE_INVALID_CURRENT_PHONE);
        assertParseFailure(parser, "i/91234567 p/123", EditClientCommandParser.MESSAGE_INVALID_NEW_PHONE);
        assertParseFailure(parser, "i/91234567 p/", EditClientCommandParser.MESSAGE_INVALID_NEW_PHONE);
        assertParseFailure(parser, "i/91234567 n/A", ClientName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "i/91234567 e/invalid", Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "i/91234567 a/1234", Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "i/91234567 t/friend t/", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedIdentifiersAndNewPhones_failure() {
        assertParseFailure(parser, "i/91234567 i/89998888 n/Alex Tan",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_IDENTIFIER));
        assertParseFailure(parser, "i/91234567 p/89998888 p/87776666",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NEW_PHONE));
    }
    //@@author
}

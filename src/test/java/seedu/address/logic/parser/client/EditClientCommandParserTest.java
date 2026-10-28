package seedu.address.logic.parser.client;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_IDENTIFIER;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_NEW_PHONE;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.client.EditClientCommand;
import seedu.address.logic.commands.client.EditClientCommand.EditClientDescriptor;
import seedu.address.model.client.Address;
import seedu.address.model.client.ClientName;
import seedu.address.model.client.Email;
import seedu.address.model.client.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.ClientUtil;
import seedu.address.testutil.EditClientDescriptorBuilder;

public class EditClientCommandParserTest {
    private final EditClientCommandParser parser = new EditClientCommandParser();
    private final Phone identifier = new Phone("91234567");

    //@@author anderschow
    @Test
    public void parse_generatedDescriptor_updatesPhoneWithoutReplacingIdentifier() {
        EditClientDescriptor descriptor = new EditClientDescriptorBuilder().withPhone("89998888").build();
        assertParseSuccess(parser, "i/91234567 " + ClientUtil.getEditClientDescriptorDetails(descriptor),
                new EditClientCommand(identifier, descriptor));
    }

    @Test
    public void parse_missingIdentifierOrIndex_failure() {
        for (String input : new String[]{"", "n/Alex Tan", "i/ n/Alex Tan", "1 n/Alex Tan",
            "1 i/91234567 n/Alex Tan"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditClientCommand.MESSAGE_USAGE));
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
        assertParseFailure(parser, "i/123 n/Alex Tan", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "i/91234567 p/123", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "i/91234567 p/", Phone.MESSAGE_CONSTRAINTS);
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

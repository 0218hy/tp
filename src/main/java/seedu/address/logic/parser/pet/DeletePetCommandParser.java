package seedu.address.logic.parser.pet;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_PET_NAME;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.pet.DeletePetCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.client.ClientParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.PetName;

/**
 * Parses input arguments and creates a {@code DeletePetCommand} object.
 */
public class DeletePetCommandParser implements Parser<DeletePetCommand> {
    public static final String MESSAGE_INDEX_AND_IDENTIFIERS =
            "A pet index cannot be combined with a pet name or owner identifier. "
                    + "Use either INDEX or both p/PET_NAME and i/OWNER_IDENTIFIER.";

    /**
     * Parses the given {@code String} of arguments in the context of the {@code DeletePetCommand}.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeletePetCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_PET_NAME, PREFIX_OWNER_IDENTIFIER);

        boolean hasIndex = !argMultimap.getPreamble().isEmpty();
        boolean hasPetName = argMultimap.getValue(PREFIX_PET_NAME).isPresent();
        boolean hasOwnerIdentifier = argMultimap.getValue(PREFIX_OWNER_IDENTIFIER).isPresent();

        if (hasIndex && (hasPetName || hasOwnerIdentifier)) {
            throw new ParseException(MESSAGE_INDEX_AND_IDENTIFIERS);
        }
        if (hasIndex) {
            Index index = ParserUtil.parseIndex(argMultimap.getPreamble());
            return new DeletePetCommand(index);
        }
        if (!hasPetName || !hasOwnerIdentifier) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeletePetCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_PET_NAME, PREFIX_OWNER_IDENTIFIER);
        PetName petName = PetParserUtil.parsePetName(argMultimap.getValue(PREFIX_PET_NAME).get());
        Phone ownerPhone = ClientParserUtil.parsePhone(argMultimap.getValue(PREFIX_OWNER_IDENTIFIER).get());

        return new DeletePetCommand(petName, ownerPhone);
    }
}

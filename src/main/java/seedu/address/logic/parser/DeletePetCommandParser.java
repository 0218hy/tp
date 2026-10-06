package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PET_NAME;

import java.util.stream.Stream;

import seedu.address.logic.commands.DeletePetCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Phone;
import seedu.address.model.pet.PetName;

/**
 * Parses input arguments and creates a {@code DeletePetCommand} object.
 */
public class DeletePetCommandParser implements Parser<DeletePetCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code DeletePetCommand}.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeletePetCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_PET_NAME, PREFIX_OWNER_IDENTIFIER);

        if (!arePrefixesPresent(argMultimap, PREFIX_PET_NAME, PREFIX_OWNER_IDENTIFIER)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeletePetCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_PET_NAME, PREFIX_OWNER_IDENTIFIER);
        PetName petName = ParserUtil.parsePetName(argMultimap.getValue(PREFIX_PET_NAME).get());
        Phone ownerPhone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_OWNER_IDENTIFIER).get());

        return new DeletePetCommand(petName, ownerPhone);
    }

    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}

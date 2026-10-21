package seedu.address.logic.parser.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_NEW_PET_NAME;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_REQUIREMENTS;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_SPECIES;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import seedu.address.logic.commands.pet.EditPetCommand;
import seedu.address.logic.commands.pet.EditPetCommand.EditPetDescriptor;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.Prefix;
import seedu.address.logic.parser.client.ClientParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;

/** Parses arguments into an identifier-based {@link EditPetCommand}. */
public class EditPetCommandParser implements Parser<EditPetCommand> {
    public static final String MESSAGE_MISSING_FIELD = "Missing required field: %s.";
    public static final String MESSAGE_DUPLICATE_FIELD = "Each pet field may be specified only once.";
    public static final String MESSAGE_INVALID_FORMAT =
            "Invalid command format. Check the command syntax and try again.";
    public static final String MESSAGE_INVALID_SPECIES =
            "Species must be Dog, Cat, Rabbit, Guinea Pig, or Other.";

    private static final String FIELD_PET_NAME = "PET_NAME";
    private static final String FIELD_OWNER_IDENTIFIER = "OWNER_IDENTIFIER";
    private static final String PARAMETER_PREFIX_REGEX = "(?:^|\\s)[a-zA-Z]/";
    private static final Prefix[] PET_FIELD_PREFIXES = {PREFIX_PET_NAME, PREFIX_OWNER_IDENTIFIER, PREFIX_NEW_PET_NAME,
        PREFIX_SPECIES, PREFIX_BREED, PREFIX_REQUIREMENTS};
    private static final Set<String> SUPPORTED_PREFIXES = Set.of(PREFIX_PET_NAME.toString(),
            PREFIX_OWNER_IDENTIFIER.toString(), PREFIX_NEW_PET_NAME.toString(), PREFIX_SPECIES.toString(),
            PREFIX_BREED.toString(), PREFIX_REQUIREMENTS.toString());
    private static final Pattern PARAMETER_PREFIX_PATTERN = Pattern.compile(PARAMETER_PREFIX_REGEX);

    @Override
    public EditPetCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(" " + args.trim(), PET_FIELD_PREFIXES);
        validateStructure(args, fields);

        PetName petName = parseRequiredPetName(fields, PREFIX_PET_NAME, FIELD_PET_NAME);
        String ownerPhone = getRequiredValue(fields, PREFIX_OWNER_IDENTIFIER, FIELD_OWNER_IDENTIFIER);
        EditPetDescriptor descriptor = new EditPetDescriptor();
        if (fields.getValue(PREFIX_NEW_PET_NAME).isPresent()) {
            descriptor.setName(PetParserUtil.parsePetName(fields.getValue(PREFIX_NEW_PET_NAME).orElseThrow()));
        }
        if (fields.getValue(PREFIX_SPECIES).isPresent()) {
            descriptor.setSpecies(parseSpecies(fields.getValue(PREFIX_SPECIES).orElseThrow()));
        }
        if (fields.getValue(PREFIX_BREED).isPresent()) {
            String breed = fields.getValue(PREFIX_BREED).orElseThrow();
            descriptor.setBreed(breed.isEmpty() ? null : parseBreed(breed));
        }
        if (fields.getValue(PREFIX_REQUIREMENTS).isPresent()) {
            descriptor.setRequirement(parseRequirement(fields.getValue(PREFIX_REQUIREMENTS).orElseThrow()));
        }

        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(EditPetCommand.MESSAGE_NOT_EDITED);
        }
        return new EditPetCommand(petName, ClientParserUtil.parsePhone(ownerPhone), descriptor);
    }

    private void validateStructure(String args, ArgumentMultimap fields) throws ParseException {
        if (!fields.getPreamble().isEmpty() || hasUnknownPrefix(args)) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        if (Arrays.stream(PET_FIELD_PREFIXES).anyMatch(prefix -> fields.getAllValues(prefix).size() > 1)) {
            throw new ParseException(MESSAGE_DUPLICATE_FIELD);
        }
    }

    private PetName parseRequiredPetName(ArgumentMultimap fields, Prefix prefix, String label) throws ParseException {
        return PetParserUtil.parsePetName(getRequiredValue(fields, prefix, label));
    }

    private String getRequiredValue(ArgumentMultimap fields, Prefix prefix, String label) throws ParseException {
        String value = fields.getValue(prefix).orElse("");
        if (value.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_MISSING_FIELD, label));
        }
        return value;
    }

    private Breed parseBreed(String value) throws ParseException {
        if (!Breed.isValidBreed(value)) {
            throw new ParseException(Breed.MESSAGE_CONSTRAINTS);
        }
        return new Breed(value);
    }

    private Requirement parseRequirement(String value) throws ParseException {
        if (!Requirement.isValidRequirement(value)) {
            throw new ParseException(Requirement.MESSAGE_CONSTRAINTS);
        }
        return new Requirement(value);
    }

    private Species parseSpecies(String value) throws ParseException {
        try {
            return Species.valueOf(value.replaceAll("\\s+", "_").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(MESSAGE_INVALID_SPECIES);
        }
    }

    private boolean hasUnknownPrefix(String args) {
        return PARAMETER_PREFIX_PATTERN.matcher(args).results()
                .map(match -> match.group().trim())
                .anyMatch(prefix -> !SUPPORTED_PREFIXES.contains(prefix));
    }

}

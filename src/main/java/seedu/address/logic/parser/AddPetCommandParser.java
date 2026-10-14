package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.PetCliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.PetCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.PetCliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.PetCliSyntax.PREFIX_REQUIREMENTS;
import static seedu.address.logic.parser.PetCliSyntax.PREFIX_SPECIES;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddPetCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;

/**
 * Parses add-pet arguments into a command.
 * Owner lookup and duplicate checks are performed when the command executes.
 */
public class AddPetCommandParser implements Parser<AddPetCommand> {
    public static final String MESSAGE_MISSING_FIELD = "Missing required field: %s.";
    public static final String MESSAGE_DUPLICATE_FIELD = "Each pet field may be specified only once.";
    public static final String MESSAGE_INVALID_FORMAT =
            "Invalid command format. Check the command syntax and try again.";
    public static final String MESSAGE_INVALID_OWNER =
            "Owner identifier must be a valid phone number belonging to the specified client.";
    public static final String MESSAGE_INVALID_SPECIES =
            "Species must be Dog, Cat, Rabbit, Guinea Pig, or Other.";
    private static final String FIELD_PET_NAME = "PET_NAME";
    private static final String FIELD_OWNER_IDENTIFIER = "OWNER_IDENTIFIER";
    private static final String FIELD_SPECIES = "SPECIES";
    private static final String FIELD_REQUIREMENTS = "REQUIREMENTS";
    private static final String OWNER_PHONE_REGEX = "[89]\\d{7}";
    private static final String PARAMETER_PREFIX_REGEX = "(?:^|\\s)[a-zA-Z]/";
    private static final int MAX_FIELD_OCCURRENCES = 1;
    private static final Prefix[] PET_FIELD_PREFIXES = {PREFIX_PET_NAME, PREFIX_OWNER_IDENTIFIER,
        PREFIX_SPECIES, PREFIX_BREED, PREFIX_REQUIREMENTS};
    private static final Set<String> SUPPORTED_PREFIXES = Set.of(PREFIX_PET_NAME.toString(),
            PREFIX_OWNER_IDENTIFIER.toString(), PREFIX_SPECIES.toString(), PREFIX_BREED.toString(),
            PREFIX_REQUIREMENTS.toString());
    private static final Pattern PARAMETER_PREFIX_PATTERN = Pattern.compile(PARAMETER_PREFIX_REGEX);

    /**
     * Parses and validates the supplied pet fields, including the optional breed.
     *
     * @throws ParseException if the command structure or a field value is invalid.
     */
    @Override
    public AddPetCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(" " + args.trim(), PET_FIELD_PREFIXES);
        validateStructure(args, fields);

        String nameValue = getRequiredValue(fields, PREFIX_PET_NAME, FIELD_PET_NAME);
        String ownerPhoneValue = getRequiredValue(fields, PREFIX_OWNER_IDENTIFIER, FIELD_OWNER_IDENTIFIER);
        String speciesValue = getRequiredValue(fields, PREFIX_SPECIES, FIELD_SPECIES);
        String requirementValue = getRequiredValue(fields, PREFIX_REQUIREMENTS, FIELD_REQUIREMENTS);

        PetName name = parsePetName(nameValue);
        String ownerPhone = parseOwnerPhone(ownerPhoneValue);
        Species species = parseSpecies(speciesValue);
        Breed breed = parseBreed(fields);
        Requirement requirement = parseRequirement(requirementValue);
        return new AddPetCommand(name, ownerPhone, species, breed, requirement);
    }

    /**
     * Rejects unknown text or prefixes and repeated pet fields.
     * An invalid requirements value takes precedence over an unknown-prefix error.
     */
    private void validateStructure(String args, ArgumentMultimap fields) throws ParseException {
        if (!fields.getPreamble().isEmpty()) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        if (fields.getValue(PREFIX_REQUIREMENTS)
                .filter(value -> !value.isEmpty() && !Requirement.isValidRequirement(value)).isPresent()) {
            throw new ParseException(Requirement.MESSAGE_CONSTRAINTS);
        }
        if (hasUnknownPrefix(args)) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        if (hasDuplicateField(fields)) {
            throw new ParseException(MESSAGE_DUPLICATE_FIELD);
        }
    }

    /**
     * Parses a pet name that meets the model constraints.
     */
    private PetName parsePetName(String value) throws ParseException {
        if (!PetName.isValidName(value)) {
            throw new ParseException(PetName.MESSAGE_CONSTRAINTS);
        }
        return new PetName(value);
    }

    /**
     * Returns a valid Singapore mobile phone number for owner lookup.
     */
    private String parseOwnerPhone(String value) throws ParseException {
        if (!value.matches(OWNER_PHONE_REGEX)) {
            throw new ParseException(MESSAGE_INVALID_OWNER);
        }
        return value;
    }

    /**
     * Parses the pet's care requirements.
     */
    private Requirement parseRequirement(String value) throws ParseException {
        if (!Requirement.isValidRequirement(value)) {
            throw new ParseException(Requirement.MESSAGE_CONSTRAINTS);
        }
        return new Requirement(value);
    }

    /**
     * Returns a required field's value.
     *
     * @throws ParseException if the field is absent or empty.
     */
    private String getRequiredValue(ArgumentMultimap fields, Prefix prefix, String label) throws ParseException {
        String value = fields.getValue(prefix).orElse("");
        if (value.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_MISSING_FIELD, label));
        }
        return value;
    }

    /**
     * Returns whether any single-valued pet field was supplied more than once.
     */
    private boolean hasDuplicateField(ArgumentMultimap fields) {
        return Arrays.stream(PET_FIELD_PREFIXES)
                .anyMatch(prefix -> fields.getAllValues(prefix).size() > MAX_FIELD_OCCURRENCES);
    }

    /**
     * Parses any supported species without regard to letter case.
     * Spaces and underscores are both accepted between words in a species name.
     */
    private Species parseSpecies(String value) throws ParseException {
        try {
            String enumName = value.replaceAll("\\s+", "_").toUpperCase(Locale.ROOT);
            return Species.valueOf(enumName);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(MESSAGE_INVALID_SPECIES);
        }
    }

    /**
     * Returns the validated breed, or {@code null} if it was omitted.
     *
     * @throws ParseException if a supplied breed does not meet the text constraints.
     */
    private Breed parseBreed(ArgumentMultimap fields) throws ParseException {
        if (fields.getValue(PREFIX_BREED).isEmpty()) {
            return null;
        }
        String breedText = fields.getValue(PREFIX_BREED).orElseThrow();
        if (!Breed.isValidBreed(breedText)) {
            throw new ParseException(Breed.MESSAGE_CONSTRAINTS);
        }
        return new Breed(breedText);
    }

    /**
     * Returns whether the input contains an unsupported parameter prefix.
     */
    private boolean hasUnknownPrefix(String args) {
        return PARAMETER_PREFIX_PATTERN
                .matcher(args).results()
                .map(match -> match.group().trim())
                .anyMatch(prefix -> !SUPPORTED_PREFIXES.contains(prefix));
    }
}

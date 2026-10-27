package seedu.address.logic.parser.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_OWNER_IDENTIFIER;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_PET_NAME;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_REQUIREMENTS;
import static seedu.address.logic.parser.pet.PetCliSyntax.PREFIX_SPECIES;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.pet.AddPetCommand;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.Prefix;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;

// @@author xinyu-zxy
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
     * @param args The text following the {@code add-pet} command word.
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
     *
     * @throws ParseException if the structure or a supplied requirement is invalid.
     */
    private void validateStructure(String args, ArgumentMultimap fields) throws ParseException {
        if (!fields.getPreamble().isEmpty()) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        validateRequirementsIfPresent(fields);
        if (hasUnknownPrefix(args)) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        if (hasDuplicateField(fields)) {
            throw new ParseException(MESSAGE_DUPLICATE_FIELD);
        }
    }

    /**
     * Checks non-empty requirements before unknown-prefix and duplicate-field errors.
     * Missing or empty requirements are reported later by {@link #getRequiredValue}.
     *
     * @throws ParseException if a non-empty requirement is invalid.
     */
    private void validateRequirementsIfPresent(ArgumentMultimap fields) throws ParseException {
        String requirements = fields.getValue(PREFIX_REQUIREMENTS).orElse("");
        if (!requirements.isEmpty() && !Requirement.isValidRequirement(requirements)) {
            throw new ParseException(Requirement.MESSAGE_CONSTRAINTS);
        }
    }

    /**
     * Validates the pet name before creating its model value.
     *
     * @throws ParseException if the pet name is invalid.
     */
    private PetName parsePetName(String value) throws ParseException {
        if (!PetName.isValidName(value)) {
            throw new ParseException(PetName.MESSAGE_CONSTRAINTS);
        }
        return new PetName(value);
    }

    /**
     * Validates the phone number's format; the command checks whether the owner exists.
     *
     * @throws ParseException if the phone number is invalid.
     */
    private String parseOwnerPhone(String value) throws ParseException {
        if (!Phone.isValidPhone(value)) {
            throw new ParseException(MESSAGE_INVALID_OWNER);
        }
        return value;
    }

    /**
     * Validates the care requirements before creating their model value.
     *
     * @throws ParseException if the care requirements are invalid.
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
        assert fields.getAllValues(prefix).size() <= MAX_FIELD_OCCURRENCES
                : "Repeated fields must be rejected before reading their values";
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
        for (Prefix prefix : PET_FIELD_PREFIXES) {
            if (fields.getAllValues(prefix).size() > MAX_FIELD_OCCURRENCES) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses any supported species without regard to letter case.
     * Spaces and underscores are both accepted between words in a species name.
     *
     * @throws ParseException if the species is unsupported.
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
        Optional<String> breedValue = fields.getValue(PREFIX_BREED);
        if (breedValue.isEmpty()) {
            return null;
        }
        String breedText = breedValue.orElseThrow();
        if (!Breed.isValidBreed(breedText)) {
            throw new ParseException(Breed.MESSAGE_CONSTRAINTS);
        }
        return new Breed(breedText);
    }

    /**
     * Returns whether the input contains an unsupported parameter prefix.
     */
    private boolean hasUnknownPrefix(String args) {
        Matcher matcher = PARAMETER_PREFIX_PATTERN.matcher(args);
        while (matcher.find()) {
            String prefix = matcher.group().trim();
            if (!SUPPORTED_PREFIXES.contains(prefix)) {
                return true;
            }
        }
        return false;
    }
}

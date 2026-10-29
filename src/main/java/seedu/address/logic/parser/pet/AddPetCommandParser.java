package seedu.address.logic.parser.pet;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
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
    public static final String MESSAGE_MISSING_FIELD = "Missing required field: %s.\n"
            + "Use the command format below:\n" + AddPetCommand.MESSAGE_USAGE;
    public static final String MESSAGE_DUPLICATE_FIELD = "Each pet field may be specified only once.";
    public static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddPetCommand.MESSAGE_USAGE);
    public static final String MESSAGE_INVALID_OWNER = Phone.MESSAGE_CONSTRAINTS;
    public static final String MESSAGE_INVALID_SPECIES =
            "Species must be Dog, Cat, Rabbit, Guinea Pig, or Other.";
    private static final String FIELD_PET_NAME = "PET_NAME";
    private static final String FIELD_OWNER_IDENTIFIER = "OWNER_PHONE";
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
     * Supplied non-empty values are validated before missing required fields are reported.
     *
     * @param args Non-null text following the {@code add-pet} command word.
     * @return A command whose owner existence and duplicate pet checks are deferred until execution.
     * @throws ParseException if the command structure or a field value is invalid.
     */
    @Override
    public AddPetCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(" " + args.trim(), PET_FIELD_PREFIXES);
        validateStructure(args, fields);

        String nameValue = getFieldValue(fields, PREFIX_PET_NAME);
        String ownerPhoneValue = getFieldValue(fields, PREFIX_OWNER_IDENTIFIER);
        String speciesValue = getFieldValue(fields, PREFIX_SPECIES);
        String requirementValue = getFieldValue(fields, PREFIX_REQUIREMENTS);

        PetName name = nameValue.isEmpty() ? null : parsePetName(nameValue);
        String ownerPhone = ownerPhoneValue.isEmpty() ? null : parseOwnerPhone(ownerPhoneValue);
        Species species = speciesValue.isEmpty() ? null : parseSpecies(speciesValue);
        Breed breed = parseBreed(fields);
        Requirement requirement = requirementValue.isEmpty() ? null : parseRequirement(requirementValue);

        ensureRequiredFieldsPresent(name, ownerPhone, species, requirement);
        return new AddPetCommand(name, ownerPhone, species, breed, requirement);
    }

    /**
     * Rejects unknown text or prefixes and repeated pet fields.
     *
     * @throws ParseException if the command structure is invalid.
     */
    private void validateStructure(String args, ArgumentMultimap fields) throws ParseException {
        if (!fields.getPreamble().isEmpty()) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        if (hasUnknownPrefix(args)) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        if (hasDuplicateField(fields)) {
            throw new ParseException(MESSAGE_DUPLICATE_FIELD);
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
     * Returns the supplied field value, or an empty string if the field is missing.
     * Repeated fields must have been rejected by structure validation.
     */
    private String getFieldValue(ArgumentMultimap fields, Prefix prefix) {
        assert fields.getAllValues(prefix).size() <= MAX_FIELD_OCCURRENCES
                : "Repeated fields must be rejected before reading their values";
        return fields.getValue(prefix).orElse("");
    }

    /**
     * Reports the first missing required field after all supplied values have been validated.
     *
     * @throws ParseException if a required field is missing.
     */
    private void ensureRequiredFieldsPresent(PetName name, String ownerPhone, Species species,
                                             Requirement requirement) throws ParseException {
        if (name == null) {
            throw new ParseException(String.format(MESSAGE_MISSING_FIELD, FIELD_PET_NAME));
        }
        if (ownerPhone == null) {
            throw new ParseException(String.format(MESSAGE_MISSING_FIELD, FIELD_OWNER_IDENTIFIER));
        }
        if (species == null) {
            throw new ParseException(String.format(MESSAGE_MISSING_FIELD, FIELD_SPECIES));
        }
        if (requirement == null) {
            throw new ParseException(String.format(MESSAGE_MISSING_FIELD, FIELD_REQUIREMENTS));
        }
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
     * Returns whether the input contains an unsupported parameter prefix outside the requirements value.
     * Prefix-like text after {@code r/} is treated as part of the requirements until another supported prefix starts.
     */
    private boolean hasUnknownPrefix(String args) {
        Matcher matcher = PARAMETER_PREFIX_PATTERN.matcher(args);
        boolean isReadingRequirements = false;
        while (matcher.find()) {
            String prefix = matcher.group().trim();
            if (SUPPORTED_PREFIXES.contains(prefix)) {
                isReadingRequirements = prefix.equals(PREFIX_REQUIREMENTS.toString());
            } else if (!isReadingRequirements) {
                return true;
            }
        }
        return false;
    }
}

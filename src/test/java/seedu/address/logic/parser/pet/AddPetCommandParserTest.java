package seedu.address.logic.parser.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.pet.AddPetCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;

// @@author xinyu-zxy
/**
 * Tests input validation for {@code add-pet}.
 */
public class AddPetCommandParserTest {
    private static final String VALID_FIELDS = " p/Mochi i/91234567 s/Dog r/Calm with baths";
    private static final int MAX_PET_NAME_LENGTH = 40;
    private static final int MAX_BREED_LENGTH = 50;
    private static final int MAX_REQUIREMENT_LENGTH = 240;
    private final AddPetCommandParser parser = new AddPetCommandParser();

    @Test
    public void parse_optionalBreed_createsCommands() throws Exception {
        // EP: optional breed omitted
        AddPetCommand withoutBreed = new AddPetCommand(new PetName("Mochi"), "91234567",
                Species.DOG, null, new Requirement("Calm with baths"));
        // EP: optional breed supplied
        AddPetCommand withBreed = new AddPetCommand(new PetName("Mochi"), "91234567",
                Species.DOG, new Breed("Golden retriever"), new Requirement("Calm with baths"));
        assertEquals(withoutBreed, parser.parse(VALID_FIELDS));
        assertEquals(withBreed, parser.parse(VALID_FIELDS + " b/Golden retriever"));
        assertEquals(withBreed.hashCode(), parser.parse(VALID_FIELDS + " b/Golden retriever").hashCode());
    }

    @Test
    public void parse_leadingSpaceAndReorderedFields_createsSameCommand() throws Exception {
        // EP: required fields supplied once, in either standard or reordered form
        AddPetCommand expected = new AddPetCommand(new PetName("Mochi"), "91234567",
                Species.CAT, new Breed("British Shorthair"), new Requirement("Calm with baths"));

        assertEquals(expected, parser.parse("p/Mochi i/91234567 s/Cat b/British Shorthair r/Calm with baths"));
        assertEquals(expected, parser.parse("  r/Calm with baths b/British Shorthair "
                + "s/cAt i/91234567 p/Mochi  "));
    }

    @Test
    public void parse_maximumFieldLengths_createsCommand() throws Exception {
        // EP: valid field values at their upper length boundaries
        // BVA: 40-character name, 50-character breed and 240-character requirement
        String name = "M".repeat(MAX_PET_NAME_LENGTH);
        String breed = "B".repeat(MAX_BREED_LENGTH);
        String requirements = "R".repeat(MAX_REQUIREMENT_LENGTH);
        AddPetCommand expected = new AddPetCommand(new PetName(name), "91234567",
                Species.DOG, new Breed(breed), new Requirement(requirements));

        assertEquals(expected, parser.parse("p/" + name + " i/91234567 s/Dog b/" + breed
                + " r/" + requirements));
    }

    @Test
    public void parse_minimumFieldLengths_createsCommand() throws Exception {
        // EP: valid field values at their lower length boundaries
        // BVA: 1-character name, 2-character breed and 5-character requirement
        AddPetCommand expected = new AddPetCommand(new PetName("M"), "91234567",
                Species.DOG, new Breed("AB"), new Requirement("Brush"));

        assertEquals(expected, parser.parse("p/M i/91234567 s/Dog b/AB r/Brush"));
    }

    @Test
    public void parse_species_acceptsAllEnumValuesIgnoringCase() throws Exception {
        // EP: every supported species label, irrespective of letter case
        for (Species species : Species.values()) {
            String speciesInput = species.name().toLowerCase(Locale.ROOT);
            AddPetCommand expected = new AddPetCommand(new PetName("Mochi"), "91234567",
                    species, null, new Requirement("Calm with baths"));

            assertEquals(expected, parser.parse(VALID_FIELDS.replace("s/Dog", "s/" + speciesInput)));
        }

        AddPetCommand guineaPig = new AddPetCommand(new PetName("Mochi"), "91234567",
                Species.GUINEA_PIG, null, new Requirement("Calm with baths"));
        assertEquals(guineaPig, parser.parse(VALID_FIELDS.replace("s/Dog", "s/GuInEa PiG")));
    }

    @Test
    public void parse_missingOrRepeatedFields_throwsParseException() {
        // EP: required field omitted
        assertThrows(ParseException.class, "Missing required field: PET_NAME.", () ->
                parser.parse(" i/91234567 s/Dog r/Calm with baths"));

        // EP: required pet-name field contains an empty value
        // BVA: zero pet-name characters, immediately below the 1-character minimum
        assertThrows(ParseException.class, "Missing required field: PET_NAME.", () ->
                parser.parse("p/ i/91234567 s/Dog r/Calm with baths"));

        // EP: field supplied more than once
        assertThrows(ParseException.class, AddPetCommandParser.MESSAGE_DUPLICATE_FIELD, () ->
                parser.parse(VALID_FIELDS + " p/Milo"));
    }

    @Test
    public void parse_eachRequiredFieldMissing_throwsParseException() {
        // EP: each required field omitted in turn
        List<String> fields = List.of("p/Mochi", "i/91234567", "s/Dog", "r/Calm with baths");
        List<String> labels = List.of("PET_NAME", "OWNER_IDENTIFIER", "SPECIES", "REQUIREMENTS");
        for (int index = 0; index < fields.size(); index++) {
            String args = VALID_FIELDS.replace(fields.get(index), "");
            String expectedMessage = String.format(AddPetCommandParser.MESSAGE_MISSING_FIELD, labels.get(index));
            assertThrows(ParseException.class, expectedMessage, () -> parser.parse(args));
        }
    }

    @Test
    public void parse_repeatedFieldsIncludingBreed_throwsParseException() {
        // EP: each required field repeated in turn
        for (String field : List.of("p/Mochi", "i/91234567", "s/Dog", "r/Calm with baths")) {
            assertThrows(ParseException.class, AddPetCommandParser.MESSAGE_DUPLICATE_FIELD, () ->
                    parser.parse(VALID_FIELDS + " " + field));
        }

        // EP: optional breed field repeated
        assertThrows(ParseException.class, AddPetCommandParser.MESSAGE_DUPLICATE_FIELD, () ->
                parser.parse(VALID_FIELDS + " b/Bengal b/Bengal"));
    }

    @Test
    public void parse_invalidValues_throwsParseException() {
        // EP: owner phone with an unsupported starting digit
        assertThrows(ParseException.class, AddPetCommandParser.MESSAGE_INVALID_OWNER, () ->
                parser.parse(VALID_FIELDS.replace("91234567", "71234567")));

        // EP: unsupported species
        assertThrows(ParseException.class, AddPetCommandParser.MESSAGE_INVALID_SPECIES, () ->
                parser.parse(VALID_FIELDS.replace("s/Dog", "s/Horse")));

        // EP: breed shorter than its minimum length
        // BVA: 1-character breed, immediately below the 2-character minimum
        assertThrows(ParseException.class, Breed.MESSAGE_CONSTRAINTS, () ->
                parser.parse(VALID_FIELDS + " b/X"));

        // EP: requirement containing an unsupported slash
        assertThrows(ParseException.class, Requirement.MESSAGE_CONSTRAINTS, () ->
                parser.parse(VALID_FIELDS.replace("Calm with baths", "Bad/care")));
    }

    @Test
    public void parse_aboveMaximumFieldLengths_throwsParseException() {
        // EP: pet name longer than the maximum
        // BVA: 41 characters, immediately above the 40-character maximum
        assertThrows(ParseException.class, PetName.MESSAGE_CONSTRAINTS, () ->
                parser.parse(VALID_FIELDS.replace("Mochi", "M".repeat(MAX_PET_NAME_LENGTH + 1))));

        // EP: breed longer than the maximum
        // BVA: 51 characters, immediately above the 50-character maximum
        assertThrows(ParseException.class, Breed.MESSAGE_CONSTRAINTS, () ->
                parser.parse(VALID_FIELDS + " b/" + "B".repeat(MAX_BREED_LENGTH + 1)));

        // EP: requirement longer than the maximum
        // BVA: 241 characters, immediately above the 240-character maximum
        assertThrows(ParseException.class, Requirement.MESSAGE_CONSTRAINTS, () ->
                parser.parse(VALID_FIELDS.replace("Calm with baths", "R".repeat(MAX_REQUIREMENT_LENGTH + 1))));
    }

    @Test
    public void parse_belowMinimumRequirementLength_throwsParseException() {
        // EP: non-blank requirement shorter than the minimum
        // BVA: 4 characters, immediately below the 5-character minimum
        assertThrows(ParseException.class, Requirement.MESSAGE_CONSTRAINTS, () ->
                parser.parse(VALID_FIELDS.replace("Calm with baths", "Care")));
    }

    @Test
    public void parse_unknownPrefix_throwsParseException() {
        // EP: unsupported command prefix
        assertThrows(ParseException.class, AddPetCommandParser.MESSAGE_INVALID_FORMAT, () ->
                parser.parse("p/Mochi i/91234567 s/Dog x/extra r/Calm with baths"));
    }

    @Test
    public void parse_unknownPrefixInsideRequirements_throwsParseException() {
        // EP: slash-prefixed text inside a requirement value
        assertThrows(ParseException.class, Requirement.MESSAGE_CONSTRAINTS, () ->
                parser.parse(VALID_FIELDS.replace("Calm with baths", "Needs x/ care")));
    }

    @Test
    public void parse_recognizedPrefixAfterRequirements_throwsParseException() {
        // EP: recognized prefix repeated inside a requirement value
        assertThrows(ParseException.class, AddPetCommandParser.MESSAGE_DUPLICATE_FIELD, () ->
                parser.parse(VALID_FIELDS.replace("Calm with baths", "Needs p/ care")));
    }
}

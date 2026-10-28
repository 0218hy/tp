package seedu.address.logic.parser.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.pet.EditPetCommand;
import seedu.address.logic.commands.pet.EditPetCommand.EditPetDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Breed;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Requirement;
import seedu.address.model.pet.Species;

/** Tests input validation for {@code edit-pet}. */
public class EditPetCommandParserTest {
    private final EditPetCommandParser parser = new EditPetCommandParser();

    //@@author khangsian0412
    @Test
    public void parse_optionalFields_createsCommand() throws Exception {
        EditPetDescriptor descriptor = new EditPetDescriptor();
        descriptor.setName(new PetName("Mochi Bean"));
        descriptor.setSpecies(Species.CAT);
        descriptor.setBreed(new Breed("British Shorthair"));
        descriptor.setRequirement(new Requirement("Prefers quiet drying"));

        EditPetCommand expected = new EditPetCommand(new PetName("Mochi"), new Phone("91234567"), descriptor);
        assertEquals(expected, parser.parse("p/Mochi i/91234567 n/Mochi Bean s/Cat "
                + "b/British Shorthair r/Prefers quiet drying"));
    }

    /** Verifies an index target accepts the same full set of optional replacements as identifier targeting. */
    @Test
    public void parse_byIndexWithAllEditableFields_success() {
        // EP: the index-target partition supports each existing optional edit field.
        EditPetDescriptor descriptor = new EditPetDescriptor();
        descriptor.setName(new PetName("Mochi Bean"));
        descriptor.setSpecies(Species.CAT);
        descriptor.setBreed(new Breed("British Shorthair"));
        descriptor.setRequirement(new Requirement("Prefers quiet drying"));

        assertParseSuccess(parser, "1 n/Mochi Bean s/Cat b/British Shorthair r/Prefers quiet drying",
                new EditPetCommand(Index.fromOneBased(1), descriptor));
    }

    /** Verifies a breed can be explicitly cleared when an index identifies the pet. */
    @Test
    public void parse_byIndexEmptyBreed_success() {
        EditPetDescriptor descriptor = new EditPetDescriptor();
        descriptor.setBreed(null);

        assertParseSuccess(parser, "1 b/", new EditPetCommand(Index.fromOneBased(1), descriptor));
    }

    /** Verifies the lower valid index boundary parses and index zero is rejected. */
    @Test
    public void parse_indexAtLowerBoundaryAndBelow_failure() {
        // BVA: index 1 is the lowest valid one-based value, while 0 is just below the valid range.
        EditPetDescriptor descriptor = new EditPetDescriptor();
        descriptor.setRequirement(new Requirement("Needs a short walk"));
        assertParseSuccess(parser, "1 r/Needs a short walk", new EditPetCommand(Index.fromOneBased(1), descriptor));
        assertParseFailure(parser, "0 r/Needs a short walk", MESSAGE_INVALID_INDEX);
    }

    /** Verifies malformed index values are rejected rather than treated as identifiers. */
    @Test
    public void parse_nonNumericIndex_failure() {
        // EP: non-numeric preamble is outside the valid index partition.
        assertParseFailure(parser, "first r/Needs a short walk", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-1 r/Needs a short walk", MESSAGE_INVALID_INDEX);
    }

    /** Verifies index targeting cannot be combined with either identifier component. */
    @Test
    public void parse_indexWithIdentifiers_failure() {
        // EP: the index and name/owner identifier forms are mutually exclusive.
        assertParseFailure(parser, "1 p/Mochi r/Needs a short walk", EditPetCommand.MESSAGE_INDEX_AND_IDENTIFIERS);
        assertParseFailure(parser, "1 i/91234567 r/Needs a short walk", EditPetCommand.MESSAGE_INDEX_AND_IDENTIFIERS);
    }

    /** Verifies an index target still requires at least one replacement field. */
    @Test
    public void parse_indexWithoutChanges_failure() {
        assertParseFailure(parser, "1", EditPetCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_emptyBreed_clearsBreed() throws Exception {
        EditPetDescriptor descriptor = new EditPetDescriptor();
        descriptor.setBreed(null);

        EditPetCommand expected = new EditPetCommand(new PetName("Mochi"), new Phone("91234567"), descriptor);
        assertEquals(expected, parser.parse("p/Mochi i/91234567 b/"));
    }

    @Test
    public void parse_reorderedFields_createsSameCommand() throws Exception {
        EditPetDescriptor descriptor = new EditPetDescriptor();
        descriptor.setSpecies(Species.GUINEA_PIG);
        descriptor.setRequirement(new Requirement("Needs calm handling"));
        EditPetCommand expected = new EditPetCommand(new PetName("Mochi"), new Phone("91234567"), descriptor);

        assertEquals(expected, parser.parse(" r/Needs calm handling s/Guinea Pig i/91234567 p/Mochi "));
    }

    @Test
    public void parse_speciesAcceptsAllSupportedValuesIgnoringCase() throws Exception {
        for (Species species : Species.values()) {
            EditPetDescriptor descriptor = new EditPetDescriptor();
            descriptor.setSpecies(species);
            EditPetCommand expected = new EditPetCommand(new PetName("Mochi"), new Phone("91234567"), descriptor);

            assertEquals(expected, parser.parse("p/Mochi i/91234567 s/" + species.name().toLowerCase()));
        }

        EditPetDescriptor guineaPigDescriptor = new EditPetDescriptor();
        guineaPigDescriptor.setSpecies(Species.GUINEA_PIG);
        EditPetCommand guineaPig = new EditPetCommand(new PetName("Mochi"), new Phone("91234567"),
                guineaPigDescriptor);
        assertEquals(guineaPig, parser.parse("p/Mochi i/91234567 s/Guinea Pig"));
    }

    @Test
    public void parse_missingRequiredIdentifier_throwsParseException() {
        assertThrows(ParseException.class, "Missing required field: PET_NAME.", () ->
                parser.parse("i/91234567 n/Mochi Bean"));
        assertThrows(ParseException.class, "Missing required field: OWNER_IDENTIFIER.", () ->
                parser.parse("p/Mochi n/Mochi Bean"));
        assertThrows(ParseException.class, "Missing required field: PET_NAME.", () ->
                parser.parse("p/ i/91234567 n/Mochi Bean"));
        assertThrows(ParseException.class, "Missing required field: OWNER_IDENTIFIER.", () ->
                parser.parse("p/Mochi i/ n/Mochi Bean"));
    }

    @Test
    public void parse_noUpdatedFields_throwsParseException() {
        assertThrows(ParseException.class, EditPetCommand.MESSAGE_NOT_EDITED, () ->
                parser.parse("p/Mochi i/91234567"));
    }

    @Test
    public void parse_repeatedOrUnknownField_throwsParseException() {
        for (String repeatedField : List.of("p/Milo", "i/98765432", "n/Mochi Junior", "s/Dog",
                "b/Beagle", "r/Needs more care")) {
            assertThrows(ParseException.class, EditPetCommandParser.MESSAGE_DUPLICATE_FIELD, () ->
                    parser.parse("p/Mochi i/91234567 n/Mochi Bean s/Cat b/Bengal r/Needs calm handling "
                            + repeatedField));
        }
        assertThrows(ParseException.class, EditPetCommandParser.MESSAGE_INVALID_FORMAT, () ->
                parser.parse("p/Mochi i/91234567 x/value n/Mochi Bean"));
        assertThrows(ParseException.class, EditPetCommand.MESSAGE_INDEX_AND_IDENTIFIERS, () ->
                parser.parse("unexpected p/Mochi i/91234567 n/Mochi Bean"));
    }

    @Test
    public void parse_invalidUpdatedValue_throwsParseException() {
        assertThrows(ParseException.class, PetName.MESSAGE_CONSTRAINTS, () ->
                parser.parse("p/Bad@Name i/91234567 n/Mochi Bean"));
        assertThrows(ParseException.class, PetName.MESSAGE_CONSTRAINTS, () ->
                parser.parse("p/Mochi i/91234567 n/Bad@Name"));
        assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () ->
                parser.parse("p/Mochi i/71234567 n/Mochi Bean"));
        assertThrows(ParseException.class, EditPetCommandParser.MESSAGE_INVALID_SPECIES, () ->
                parser.parse("p/Mochi i/91234567 s/Horse"));
        assertThrows(ParseException.class, Breed.MESSAGE_CONSTRAINTS, () ->
                parser.parse("p/Mochi i/91234567 b/X"));
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
    //@@author

    // @@author xinyu-zxy
    @Test
    public void parse_requirementContainingSlash_createsCommand() throws Exception {
        // EP: valid updated requirements containing a slash
        EditPetDescriptor descriptor = new EditPetDescriptor();
        descriptor.setRequirement(new Requirement("Bath/brush carefully"));
        EditPetCommand expected = new EditPetCommand(
                new PetName("Mochi"), new Phone("91234567"), descriptor);

        assertEquals(expected, parser.parse("p/Mochi i/91234567 r/Bath/brush carefully"));
    }
    // @@author
}

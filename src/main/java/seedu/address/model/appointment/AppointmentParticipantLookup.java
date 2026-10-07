package seedu.address.model.appointment;

import seedu.address.model.person.Phone;

/**
 * Owner and pet lookup required by appointment scheduling.
 * The owner/pet features supply the implementation; scheduling does not duplicate their lookup rules.
 */
public interface AppointmentParticipantLookup {
    /**
     * Returns true if the phone number identifies exactly one registered owner.
     * Searches all owners, regardless of any displayed list filter.
     */
    boolean hasOwner(Phone ownerPhone);

    /**
     * Returns true if the named pet belongs to the specified owner.
     * The pet feature defines name matching; the supplied name has normalised whitespace.
     */
    boolean hasPet(Phone ownerPhone, String petName);
}

package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.model.pet.Species;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;
import seedu.address.testutil.TypicalClients;

public class AppointmentListTest {
    private static final Appointment FIRST = create("10:00", "11:00");
    private static final Appointment SECOND = create("11:00", "12:00");

    //@@author chunghean
    @Test
    public void equalsHashCodeAndToString_compareAppointmentContents() {
        AppointmentList appointments = new AppointmentList();
        appointments.setAppointments(List.of(FIRST, SECOND));
        AppointmentList copy = new AppointmentList();
        copy.setAppointments(List.of(FIRST, SECOND));

        assertTrue(appointments.equals(appointments));
        assertTrue(appointments.equals(copy));
        assertEquals(appointments.hashCode(), copy.hashCode());
        assertEquals(appointments.toString(), copy.toString());
        assertFalse(appointments.equals(null));
        assertFalse(appointments.equals("appointments"));
        assertNotEquals(appointments, new AppointmentList());
    }

    @Test
    public void updateOwner_updatesMatchingAppointmentsAndPreservesOthers() {
        AppointmentList appointments = new AppointmentList();
        Appointment currentOwnerAppointment = create("10:00", "11:00");
        Appointment otherOwnerAppointment = createAppointment(new Phone("81234567"), "Mochi",
                new AppointmentDate("01-01-2020"), new StartTime("11:00"), new EndTime("12:00"),
                Service.NAIL_TRIM);
        appointments.setAppointments(List.of(currentOwnerAppointment, otherOwnerAppointment));
        Client currentOwner = new ClientBuilder().withPhone("91234567").build();
        Client editedOwner = new ClientBuilder(currentOwner).withPhone("99999999").build();

        appointments.updateOwner(currentOwner, editedOwner);

        assertEquals(new Phone("99999999"), appointments.getAppointmentList().get(0).getOwnerPhone());
        assertEquals(new Phone("81234567"), appointments.getAppointmentList().get(1).getOwnerPhone());
    }

    //@@author chunghean
    @Test
    public void updateOwner_samePhoneWithChangedEmail_updatesPhoneAndPreservesHistory() {
        Client historicalOwner = new ClientBuilder().withName("Alice Tan").withPhone("91234567")
                .withEmail("alice@example.com").build();
        Pet historicalPet = new PetBuilder(historicalOwner).withName("Milo").build();
        Appointment historicalAppointment = new Appointment(historicalOwner, historicalPet,
                new AppointmentDate("01-01-2000"), new StartTime("10:00"), new EndTime("11:00"),
                Service.FULL_GROOM);
        Client currentOwner = new ClientBuilder(historicalOwner).withEmail("new@example.com").build();
        Client editedOwner = new ClientBuilder(currentOwner).withPhone("92345678").build();
        AppointmentList appointments = new AppointmentList();
        appointments.setAppointments(List.of(historicalAppointment));

        appointments.updateOwner(currentOwner, editedOwner);

        Appointment updated = appointments.getAppointmentList().get(0);
        assertEquals(editedOwner.getPhone(), updated.getOwnerPhone());
        assertEquals(historicalOwner.getEmail(), updated.getOwner().getEmail());
        assertEquals(historicalOwner.getName(), updated.getOwner().getName());
        assertEquals(historicalPet.getRequirement(), updated.getPet().getRequirement());
    }
    //@@author
    //@@author

    //@@author khangsian0412
    @Test
    public void updatePetName_updatesAllMatchingAppointmentsAndPreservesOthers() {
        AppointmentList appointments = new AppointmentList();
        Appointment firstMatch = create("10:00", "11:00");
        Appointment secondMatch = create("11:00", "12:00");
        Appointment otherPet = createAppointment(new Phone("81234567"), "Mochi",
                new AppointmentDate("01-01-2020"), new StartTime("12:00"), new EndTime("13:00"),
                Service.NAIL_TRIM);
        appointments.setAppointments(List.of(firstMatch, secondMatch, otherPet));
        Pet renamedPet = firstMatch.getPet().withName(new PetName("Rex"));

        appointments.updatePetName(firstMatch.getPet(), renamedPet.getName());

        assertEquals(new PetName("Rex"), appointments.getAppointmentList().get(0).getPetName());
        assertEquals(new PetName("Rex"), appointments.getAppointmentList().get(1).getPetName());
        assertTrue(appointments.getAppointmentList().get(0).isForPet(renamedPet));
        assertTrue(appointments.getAppointmentList().get(1).isForPet(renamedPet));
        assertEquals(new PetName("Mochi"), appointments.getAppointmentList().get(2).getPetName());
    }

    @Test
    public void updatePetName_nullArguments_throwNullPointerException() {
        AppointmentList appointments = new AppointmentList();
        Pet pet = FIRST.getPet();

        assertThrows(NullPointerException.class, () -> appointments.updatePetName(null, new PetName("Rex")));
        assertThrows(NullPointerException.class, () -> appointments.updatePetName(pet, null));
    }

    @Test
    public void updatePet_requirementOnlyChange_refreshesOngoingAppointment() {
        Pet originalPet = petWithCurrentDetails();
        Pet editedPet = new PetBuilder(originalPet).withRequirement("Use sensitive-skin shampoo").build();

        Appointment updated = updateSingleOngoingAppointment(originalPet, editedPet);

        assertEquals(editedPet.getRequirement(), updated.getPet().getRequirement());
    }

    @Test
    public void updatePet_speciesOnlyChange_refreshesOngoingAppointment() {
        Pet originalPet = petWithCurrentDetails();
        Pet editedPet = new PetBuilder(originalPet).withSpecies(Species.CAT).build();

        Appointment updated = updateSingleOngoingAppointment(originalPet, editedPet);

        assertEquals(editedPet.getSpecies(), updated.getPet().getSpecies());
    }

    @Test
    public void updatePet_breedOnlyChange_refreshesOngoingAppointment() {
        Pet originalPet = petWithCurrentDetails();
        Pet editedPet = new PetBuilder(originalPet).withBreed("Shih Tzu").build();

        Appointment updated = updateSingleOngoingAppointment(originalPet, editedPet);

        assertEquals(editedPet.getBreed(), updated.getPet().getBreed());
    }

    @Test
    public void updatePet_clearingBreed_refreshesOngoingAppointment() {
        Pet originalPet = petWithCurrentDetails();
        Pet editedPet = new Pet(originalPet.getName(), originalPet.getOwner(), originalPet.getSpecies(),
                originalPet.getRequirement());

        Appointment updated = updateSingleOngoingAppointment(originalPet, editedPet);

        assertNull(updated.getPet().getBreed());
    }

    @Test
    public void updatePet_nameChange_refreshesAllFieldsForOngoingAndFutureAppointments() {
        Pet originalPet = petWithCurrentDetails();
        Pet editedPet = new PetBuilder(originalPet).withName("Rex")
                .withSpecies(Species.CAT).withBreed("Shih Tzu")
                .withRequirement("Use sensitive-skin shampoo").build();
        Appointment ongoing = appointment(originalPet, "10:00", "10:30");
        Appointment future = appointment(originalPet, "11:00", "11:30");
        Client otherOwner = new ClientBuilder(TypicalClients.BENSON).build();
        Pet sameNameOtherOwner = new PetBuilder(otherOwner).withName(originalPet.getName().value).build();
        Appointment unrelated = appointment(sameNameOtherOwner, "12:00", "12:30");
        AppointmentList appointments = new AppointmentList(fixedClock("2027-01-01T10:15:00"));
        appointments.setAppointments(List.of(ongoing, future, unrelated));

        appointments.updatePet(originalPet, editedPet);

        for (Appointment refreshed : appointments.getAppointmentList().subList(0, 2)) {
            assertEquals(editedPet, refreshed.getPet());
            assertSame(refreshed.getOwner(), refreshed.getPet().getOwner());
        }
        assertSame(unrelated, appointments.getAppointmentList().get(2));
        assertEquals(new PetName("Milo"), appointments.getAppointmentList().get(2).getPetName());
    }

    @Test
    public void updatePet_completedAppointmentAtEndTimeBoundary_keepsHistoryButPropagatesRename() {
        Pet originalPet = petWithCurrentDetails();
        Pet editedPet = new PetBuilder(originalPet).withName("Rex")
                .withSpecies(Species.CAT).withBreed("Shih Tzu")
                .withRequirement("Use sensitive-skin shampoo").build();
        Appointment endedEarlierToday = appointment(originalPet, "09:00", "09:30");
        Appointment endsExactlyNow = appointment(originalPet, "10:00", "10:30");
        AppointmentList appointments = new AppointmentList(fixedClock("2027-01-01T10:30:00"));
        appointments.setAppointments(List.of(endedEarlierToday, endsExactlyNow));

        appointments.updatePet(originalPet, editedPet);

        for (Appointment historical : appointments.getAppointmentList()) {
            assertEquals(new PetName("Rex"), historical.getPetName());
            assertEquals(originalPet.getSpecies(), historical.getPet().getSpecies());
            assertEquals(originalPet.getBreed(), historical.getPet().getBreed());
            assertEquals(originalPet.getRequirement(), historical.getPet().getRequirement());
        }
    }

    @Test
    public void updatePet_completedAppointmentWithNonNameChange_preservesOriginalSnapshot() {
        Pet originalPet = petWithCurrentDetails();
        Pet editedPet = new PetBuilder(originalPet).withRequirement("Use sensitive-skin shampoo").build();
        Appointment completed = appointment(originalPet, "10:00", "10:30");
        AppointmentList appointments = new AppointmentList(fixedClock("2027-01-01T10:30:00"));
        appointments.setAppointments(List.of(completed));

        appointments.updatePet(originalPet, editedPet);

        assertSame(completed, appointments.getAppointmentList().get(0));
    }

    @Test
    public void updatePet_noFieldChange_doesNotReplaceAppointment() {
        Pet originalPet = petWithCurrentDetails();
        Pet unchangedPet = new PetBuilder(originalPet).build();
        Appointment existing = appointment(originalPet, "10:00", "10:30");
        AppointmentList appointments = new AppointmentList(fixedClock("2027-01-01T10:15:00"));
        appointments.setAppointments(List.of(existing));

        appointments.updatePet(originalPet, unchangedPet);

        assertSame(existing, appointments.getAppointmentList().get(0));
    }

    @Test
    public void updatePet_noLinkedAppointments_isNoOp() {
        Pet originalPet = petWithCurrentDetails();
        Pet editedPet = new PetBuilder(originalPet).withRequirement("Use sensitive-skin shampoo").build();
        AppointmentList appointments = new AppointmentList(fixedClock("2027-01-01T10:15:00"));

        appointments.updatePet(originalPet, editedPet);

        assertTrue(appointments.getAppointmentList().isEmpty());
    }

    @Test
    public void updatePet_nullArguments_throwNullPointerException() {
        Pet originalPet = petWithCurrentDetails();
        Pet editedPet = new PetBuilder(originalPet).withRequirement("Use sensitive-skin shampoo").build();

        assertThrows(NullPointerException.class, () -> new AppointmentList().updatePet(null, editedPet));
        assertThrows(NullPointerException.class, () -> new AppointmentList().updatePet(originalPet, null));
    }
    //@@author

    //@@author chunghean
    private static Appointment create(String start, String end) {
        return createAppointment(new Phone("91234567"), "BuBu", new AppointmentDate("01-01-2020"),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }

    private static Pet petWithCurrentDetails() {
        return new PetBuilder(TypicalClients.ALICE).withName("Milo").withBreed("Poodle")
                .withRequirement("Original care notes").build();
    }

    private static Appointment updateSingleOngoingAppointment(Pet originalPet, Pet editedPet) {
        Appointment existing = appointment(originalPet, "10:00", "10:30");
        AppointmentList appointments = new AppointmentList(fixedClock("2027-01-01T10:15:00"));
        appointments.setAppointments(List.of(existing));

        appointments.updatePet(originalPet, editedPet);

        Appointment updated = appointments.getAppointmentList().get(0);
        assertEquals(editedPet, updated.getPet());
        assertSame(updated.getOwner(), updated.getPet().getOwner());
        return updated;
    }

    private static Appointment appointment(Pet pet, String start, String end) {
        return new Appointment(pet.getOwner(), pet, new AppointmentDate("01-01-2027"),
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }

    private static Clock fixedClock(String singaporeDateTime) {
        ZoneId singapore = ZoneId.of("Asia/Singapore");
        return Clock.fixed(java.time.LocalDateTime.parse(singaporeDateTime).atZone(singapore).toInstant(), singapore);
    }
    //@@author
}

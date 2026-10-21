package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.AppointmentTestUtil.createAppointment;
import static seedu.address.testutil.TypicalClients.ALICE;
import static seedu.address.testutil.TypicalClients.BENSON;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;
import seedu.address.model.pet.PetName;
import seedu.address.testutil.ClientBuilder;
import seedu.address.testutil.PetBuilder;

public class AppointmentTest {
    private static final Phone OWNER_PHONE = new Phone("91234567");
    private static final Client OWNER = new ClientBuilder().withPhone(OWNER_PHONE.value).build();
    private static final Pet PET = new PetBuilder(OWNER).withName("BuBu").build();
    private static final AppointmentDate DATE = new AppointmentDate("18-09-2026");

    @Test
    public void constructor_validAppointment_preservesFields() {
        Appointment appointment = create("10:30", "12:00");
        assertSame(OWNER, appointment.getOwner());
        assertSame(PET, appointment.getPet());
        assertEquals(OWNER_PHONE, appointment.getOwnerPhone());
        assertEquals(new PetName("BuBu"), appointment.getPetName());
        assertEquals(DATE, appointment.getDate());
        assertEquals(new StartTime("10:30"), appointment.getStartTime());
        assertEquals(new EndTime("12:00"), appointment.getEndTime());
        assertEquals(Service.FULL_GROOM, appointment.getService());
        assertEquals(new EndTime("20:00"), create("19:30", "20:00").getEndTime());
    }

    @Test
    public void constructor_invalidDuration_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> create("10:30", "10:30"));
        assertThrows(IllegalArgumentException.class, () -> create("10:30", "10:00"));
    }

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        StartTime start = new StartTime("10:30");
        EndTime end = new EndTime("12:00");
        Service service = Service.FULL_GROOM;
        assertThrows(NullPointerException.class, () -> new Appointment(null, PET, DATE, start, end, service));
        assertThrows(NullPointerException.class, () -> new Appointment(OWNER, null, DATE, start, end, service));
        assertThrows(NullPointerException.class, () -> new Appointment(OWNER, PET, null, start, end, service));
        assertThrows(NullPointerException.class, () -> new Appointment(OWNER, PET, DATE, null, end, service));
        assertThrows(NullPointerException.class, () -> new Appointment(OWNER, PET, DATE, start, null, service));
        assertThrows(NullPointerException.class, () -> new Appointment(OWNER, PET, DATE, start, end, null));
    }

    @Test
    public void constructor_petBelongsToDifferentOwner_rejected() {
        Client otherOwner = new ClientBuilder().withPhone("81234567").build();
        assertThrows(IllegalArgumentException.class, () -> new Appointment(otherOwner, PET, DATE,
                new StartTime("10:30"), new EndTime("12:00"), Service.FULL_GROOM));
    }

    @Test
    public void equals_participantProfileDetailsParticipateInEquality() {
        Appointment original = create("10:30", "12:00");
        Client updatedOwner = new ClientBuilder(OWNER).withAddress("New address").build();
        Pet updatedPet = new PetBuilder(PET).withRequirement("Sensitive skin").withBreed("Poodle").build();
        Appointment changedOwner = new Appointment(updatedOwner, PET.withOwner(updatedOwner), DATE,
                original.getStartTime(), original.getEndTime(), original.getService());
        Appointment changedPet = new Appointment(OWNER, updatedPet, DATE,
                original.getStartTime(), original.getEndTime(), original.getService());
        assertNotEquals(original, changedOwner);
        assertNotEquals(original, changedPet);
        assertTrue(original.isSameAppointment(changedOwner));
        assertTrue(original.isSameAppointment(changedPet));
    }

    @Test
    public void overlaps_intersectionsDetectedAndAdjacentSlotsAllowed() {
        Appointment appointment = create("10:30", "12:00");
        for (Appointment other : new Appointment[]{create("10:30", "12:00"), create("10:00", "11:00"),
                create("11:30", "12:30"), create("10:00", "12:30"), create("11:00", "11:30")}) {
            assertTrue(appointment.overlaps(other));
            assertTrue(other.overlaps(appointment));
        }
        for (Appointment other : new Appointment[]{create("09:00", "10:30"), create("12:00", "13:30")}) {
            assertFalse(appointment.overlaps(other));
            assertFalse(other.overlaps(appointment));
        }
        Appointment differentOwner = createAppointment(new Phone("81234567"), "Mochi", DATE,
                new StartTime("11:00"), new EndTime("12:30"), Service.NAIL_TRIM);
        assertTrue(appointment.overlaps(differentOwner));
        Appointment nextDay = createAppointment(OWNER_PHONE, "BuBu", new AppointmentDate("19-09-2026"),
                new StartTime("10:30"), new EndTime("12:00"), Service.FULL_GROOM);
        assertFalse(appointment.overlaps(nextDay));
    }

    @Test
    public void matches_includesStartAndInteriorButExcludesEnd() {
        Appointment appointment = create("10:30", "12:00");
        assertTrue(appointment.matches(DATE, LocalTime.of(10, 30)));
        assertTrue(appointment.matches(DATE, LocalTime.of(10, 50)));
        assertFalse(appointment.matches(DATE, LocalTime.of(10, 29)));
        assertFalse(appointment.matches(DATE, LocalTime.of(12, 0)));
        assertFalse(appointment.matches(new AppointmentDate("19-09-2026"), LocalTime.of(10, 30)));
        assertTrue(create("12:00", "13:30").matches(DATE, LocalTime.of(12, 0)));
    }

    @Test
    public void isForPet_matchesOwnerAndNormalizedName() {
        Appointment appointment = new Appointment(ALICE, new PetBuilder(ALICE).withName("milo").build(), DATE,
                new StartTime("10:30"), new EndTime("12:00"), Service.FULL_GROOM);

        assertTrue(appointment.isForPet(new PetBuilder(ALICE).withName("  MiLo  ").build()));
        assertFalse(appointment.isForPet(new PetBuilder(BENSON).withName("Milo").build()));
        assertFalse(appointment.isForPet(new PetBuilder(ALICE).withName("Luna").build()));
        assertThrows(NullPointerException.class, () -> appointment.isForPet(null));
    }

    @Test
    public void withOwnerPhone_preservesSnapshotsAndUpdatesBothParticipants() {
        Appointment original = create("10:30", "12:00");
        Phone newPhone = new Phone("81234567");
        Client expectedOwner = new ClientBuilder(OWNER).withPhone(newPhone.value).build();

        Appointment updated = original.withOwnerPhone(newPhone);

        assertEquals(new Appointment(expectedOwner, PET.withOwner(expectedOwner), DATE,
                original.getStartTime(), original.getEndTime(), original.getService()), updated);
        assertSame(updated.getOwner(), updated.getPet().getOwner());
        assertSame(OWNER, original.getOwner());
        assertSame(PET, original.getPet());
        assertThrows(NullPointerException.class, () -> original.withOwnerPhone(null));
    }

    @Test
    public void withPetName_preservesAppointmentSnapshotDetails() {
        Appointment original = create("10:30", "12:00");

        Appointment renamed = original.withPetName(new PetName("Rex"));

        assertSame(OWNER, renamed.getOwner());
        assertEquals(new PetName("Rex"), renamed.getPetName());
        assertEquals(PET.getSpecies(), renamed.getPet().getSpecies());
        assertEquals(PET.getBreed(), renamed.getPet().getBreed());
        assertEquals(PET.getRequirement(), renamed.getPet().getRequirement());
        assertTrue(renamed.isForPet(PET.withName(new PetName("Rex"))));
        assertEquals(new PetName("BuBu"), original.getPetName());
        assertThrows(NullPointerException.class, () -> original.withPetName(null));
    }

    @Test
    public void equalsAndIdentity_distinguishSlotFromFullDetails() {
        Appointment appointment = create("10:30", "12:00");
        Appointment copy = create("10:30", "12:00");
        assertEquals(appointment, copy);
        assertEquals(appointment.hashCode(), copy.hashCode());
        assertTrue(appointment.isSameAppointment(create("10:30", "13:00")));
        assertNotEquals(appointment, create("10:30", "13:00"));
        assertFalse(appointment.isSameAppointment(create("11:00", "12:00")));
        assertFalse(appointment.isSameAppointment(null));
        assertNotEquals(null, appointment);
        Appointment otherService = createAppointment(OWNER_PHONE, "BuBu", DATE,
                new StartTime("10:30"), new EndTime("12:00"), Service.OTHER);
        assertTrue(appointment.isSameAppointment(otherService));
        assertNotEquals(appointment, otherService);
    }

    @Test
    public void constructor_petName_acceptsLengthBoundariesAndAllowedCharacters() {
        for (String name : new String[]{"A", "a".repeat(40), "Mr. O'Malley-2", "小白", "Café"}) {
            Appointment appointment = createAppointment(OWNER_PHONE, name, DATE,
                    new StartTime("10:30"), new EndTime("12:00"), Service.FULL_GROOM);
            assertEquals(new PetName(name), appointment.getPetName());
        }
    }

    @Test
    public void isSameAppointment_comparesDateAndStartRatherThanPet() {
        Appointment appointment = create("10:30", "12:00");
        assertTrue(appointment.isSameAppointment(appointment));
        Appointment nextDay = createAppointment(OWNER_PHONE, "BuBu", new AppointmentDate("19-09-2026"),
                new StartTime("10:30"), new EndTime("12:00"), Service.FULL_GROOM);
        assertFalse(appointment.isSameAppointment(nextDay));
        Appointment differentPet = createAppointment(new Phone("81234567"), "Mochi", DATE,
                new StartTime("10:30"), new EndTime("12:00"), Service.NAIL_TRIM);
        assertTrue(appointment.isSameAppointment(differentPet));
    }

    @Test
    public void equals_eachFieldParticipatesInEquality() {
        Appointment appointment = create("10:30", "12:00");
        assertTrue(appointment.equals(appointment));
        assertFalse(appointment.equals(null));
        assertFalse(appointment.equals("BuBu"));
        Appointment[] differentAppointments = {
            createAppointment(new Phone("81234567"), "BuBu", DATE,
                    new StartTime("10:30"), new EndTime("12:00"), Service.FULL_GROOM),
            createAppointment(OWNER_PHONE, "Mochi", DATE,
                    new StartTime("10:30"), new EndTime("12:00"), Service.FULL_GROOM),
            createAppointment(OWNER_PHONE, "BuBu", new AppointmentDate("19-09-2026"),
                    new StartTime("10:30"), new EndTime("12:00"), Service.FULL_GROOM),
            create("11:00", "12:00"),
            create("10:30", "12:30"),
            createAppointment(OWNER_PHONE, "BuBu", DATE,
                    new StartTime("10:30"), new EndTime("12:00"), Service.OTHER)
        };
        for (Appointment different : differentAppointments) {
            assertFalse(appointment.equals(different));
            assertFalse(different.equals(appointment));
        }
    }

    @Test
    public void overlapAndMatching_nullArguments_rejected() {
        Appointment appointment = create("10:30", "12:00");
        assertThrows(NullPointerException.class, () -> appointment.overlaps(null));
        assertThrows(NullPointerException.class, () -> appointment.matches(null, LocalTime.of(11, 0)));
        assertThrows(NullPointerException.class, () -> appointment.matches(DATE, null));
    }

    @Test
    public void toString_includesAppointmentDetails() {
        String expected = Appointment.class.getCanonicalName()
                + "{owner=" + OWNER + ", pet=" + PET + ", date=18-09-2026, "
                + "startTime=10:30, endTime=12:00, service=Full groom}";
        assertEquals(expected, create("10:30", "12:00").toString());
    }

    private Appointment create(String start, String end) {
        return new Appointment(OWNER, PET, DATE,
                new StartTime(start), new EndTime(end), Service.FULL_GROOM);
    }
}

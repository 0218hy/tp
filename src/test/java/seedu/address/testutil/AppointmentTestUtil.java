package seedu.address.testutil;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentDate;
import seedu.address.model.appointment.EndTime;
import seedu.address.model.appointment.Service;
import seedu.address.model.appointment.StartTime;
import seedu.address.model.client.Client;
import seedu.address.model.client.Phone;
import seedu.address.model.pet.Pet;

/** Creates complete participant profiles for appointment fixtures. */
public class AppointmentTestUtil {
    //@@author 0218hy
    /** Creates an appointment using default client and pet details with the supplied identifiers. */
    public static Appointment createAppointment(Phone ownerPhone, String petName, AppointmentDate date,
            StartTime startTime, EndTime endTime, Service service) {
        Client owner = new ClientBuilder().withPhone(ownerPhone.value).build();
        Pet pet = new PetBuilder(owner).withName(petName).build();
        return new Appointment(owner, pet, date, startTime, endTime, service);
    }
    //@@author

    //@@author chunghean
    /** Creates an appointment linked to the supplied client profile. */
    public static Appointment createAppointment(Client owner, String petName, AppointmentDate date,
            StartTime startTime, EndTime endTime, Service service) {
        Pet pet = new PetBuilder(owner).withName(petName).build();
        return new Appointment(owner, pet, date, startTime, endTime, service);
    }
    //@@author
}

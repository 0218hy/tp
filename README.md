# BuBu

[![Java CI](https://github.com/AY2627S1-CS2103T-W08-2/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-W08-2/tp/actions/workflows/gradle.yml)

**Client contacts, pet care details, and grooming appointments in one place.**

BuBu is designed for freelance mobile pet groomers. It helps groomers to keep track of their clients, remember each pet's grooming requirements, and plan upcoming appointments. A command-line interface supports quick keyboard input, while a graphical interface keeps records and schedules easy to read.

![BuBu UI mockup showing the command box, client and pet navigation, and upcoming grooming appointments](docs/images/Ui.png)

*Mockup of the intended product. BuBu is under development; the features below describe the planned MVP.*

## Planned features

* **Manage clients:** Add, list, search, and delete client records containing names, Singapore mobile numbers, email addresses, and service addresses.
* **Keep pet profiles:** Link pets to their owners and record species, optional breed information, and grooming or care requirements. View all pets or filter by owner.
* **Plan appointments:** Schedule grooming services for registered pets, view upcoming bookings or appointments on a specific date, and delete cancelled bookings. Overlapping appointments are rejected.
* **Protect linked records:** Confirm deletions and prevent removing clients with linked pets or appointments, or pets with future appointments.

## Planned commands

| Task | Commands |
| --- | --- |
| Manage client records | `add-client`, `list-clients`, `delete-client` |
| Manage pet profiles | `add-pet`, `list-pets`, `delete-pet` |
| Manage grooming appointments | `schedule`, `list-appointments`, `delete-appointment` |

For example, `list-clients q/amelia` searches client records, `list-pets o/Amelia Tan` shows that client's pets, and `list-appointments` displays upcoming appointments in chronological order.

## Documentation

* [User Guide](docs/UserGuide.md)
* [Developer Guide](docs/DeveloperGuide.md)
* [About Us](docs/AboutUs.md)
* [MVP Feature Specification](https://docs.google.com/document/d/1FkVnp8Y37VR8ZQ9TC2qKz3bctjJC6KtlW0KcylmdCfQ/edit?usp=sharing)

The guides are being adapted for BuBu and currently include inherited AddressBook documentation. The MVP feature specification describes the planned BuBu commands and behaviour.

## Development

BuBu is built with Java and JavaFX. With JDK 25 installed, run the current development version from the repository root:

```sh
./gradlew run
```

Run the automated checks with `./gradlew check`. On Windows, use `gradlew.bat` in place of `./gradlew`.

## Acknowledgements

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).


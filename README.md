# BuBu

[![Java CI](https://github.com/AY2627S1-CS2103T-W08-2/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-W08-2/tp/actions/workflows/gradle.yml)

**Client contacts, pet care details, and grooming appointments in one place.**

BuBu is designed for freelance mobile pet groomers. It helps groomers to keep track of their clients, remember each pet's grooming requirements, and plan upcoming appointments. A command-line interface supports quick keyboard input, while a graphical interface keeps records and schedules easy to read.

![BuBu UI showing the command box and record-list panels](docs/images/Ui.png)

BuBu provides one command-driven workflow for client records, pet profiles, and grooming appointments.

## Features

* **Manage clients:** Add, list, search, and delete client records containing names, Singapore mobile numbers, email addresses, and service addresses.
* **Keep pet profiles:** Add and edit owner-linked pet profiles with species, optional breed information, and grooming or care requirements. View all pets or filter by owner.
* **Plan appointments:** Schedule grooming services for registered pets, view ongoing or future bookings in chronological order, and edit or delete bookings. Overlapping appointments are rejected.
* **Switch record views:** The GUI displays one of the client, pet, or appointment lists at a time. List commands select the visible record type, while index-based deletions use the corresponding displayed list.

## Commands

| Task | Commands |
| --- | --- |
| Manage client records | `add-client`, `list-clients`, `delete-client` |
| Manage pet profiles | `add-pet`, `list-pets`, `edit-pet`, `delete-pet` |
| Manage grooming appointments | `add-appointment`, `list-appointments`, `edit-appointment`, `delete-appointment` |

For example, `list-clients q/amelia` searches client records, `list-pets i/87438807` shows the pets owned by the client with that phone number, and `list-appointments` displays upcoming appointments in chronological order.

In the default application startup, `add-appointment` remains unavailable until owner and pet lookup is connected;
appointment listing and deletion are available.

## Documentation

* [User Guide](docs/UserGuide.md)
* [Developer Guide](docs/DeveloperGuide.md)
* [About Us](docs/AboutUs.md)
* [MVP Feature Specification](https://docs.google.com/document/d/1FkVnp8Y37VR8ZQ9TC2qKz3bctjJC6KtlW0KcylmdCfQ/edit?usp=sharing)

The [User Guide](docs/UserGuide.md) documents command formats and user-visible behavior. The [Developer Guide](docs/DeveloperGuide.md) documents the current model, logic, UI, storage, and manual-testing workflows.

## Development

BuBu is built with Java and JavaFX. With JDK 25 installed, run the current development version from the repository root:

```sh
./gradlew run
```

The shared `BubuBook` contains clients, pets, and appointments, saved together in `data/bububook.json`. Pet and appointment display order is maintained by the model without changing the saved record order.
Feature-specific code lives in
`client`, `pet`, and `appointment` packages; see the [current code organization](docs/DeveloperGuide.md#current-code-organization)
for the package layout and storage format.

Run the automated checks with `./gradlew check`. On Windows, use `gradlew.bat` in place of `./gradlew`.

## Acknowledgements

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).


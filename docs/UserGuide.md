---
layout: page
title: User Guide
---

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

2. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

3. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

4. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

5. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add-client n/John Doe i/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the Address Book.

   * `delete-client 3` : Deletes the 3rd contact shown in the current list.

   * `delete-client i/91234567` : Deletes the person with phone number `91234567`.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

6. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add-client n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME i/PHONE_NUMBER`, `i/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a client: `add-client`

Creates a booking contact and mobile grooming address. All four fields are required:

* Name: 2-60 characters using letters, spaces, apostrophes, hyphens or full stops.
* Phone: exactly eight digits, starting with 8 or 9, without spaces or hyphens.
* Email: one `@`, no whitespace, and non-empty local and domain parts. The domain must contain a full stop separating non-empty sections.
* Address: 5-120 characters using letters, digits, spaces, commas, full stops, hyphens or `#`.

A phone number or email already used by another client is rejected. Email comparisons are case-sensitive. Clients may share names and addresses. Each required field may appear only once; blank values count as missing. Surrounding whitespace is trimmed.

Success displays `Client added: NAME (PHONE).` and shows the client in the list. A save failure displays `Unable to save client.`; the client remains in memory but may not survive a restart.

Format: `add-client n/NAME i/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​`

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A person can have any number of tags, including zero.
</div>

Examples:
* `add-client n/John Doe i/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add-client n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison i/91234567 t/criminal`

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [i/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 i/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a person: `delete-client`

Deletes a person from the address book using either their displayed index or phone number.

Formats:

```text
delete-client INDEX
delete-client i/PHONE_NUMBER
```

* `INDEX` must be a positive integer.
* The index refers to the displayed person list.
* `PHONE_NUMBER` must contain exactly 8 digits starting with 8 or 9.
* Provide either an index or a phone identifier, but not both.

Examples:
* `list` followed by `delete-client 2` deletes the 2nd person.
* `find Betsy` followed by `delete-client 1` deletes the 1st search result.
* `delete-client i/91234567` deletes the person with phone number `91234567`.

### Adding a pet to a person: `add-pet`

Creates a pet profile for a person already in the address book. Identify the owner by phone number, not by name.
The command reports the pet and owner's name when the profile is saved; it does not change the current person list.

Format: `add-pet p/PET_NAME i/OWNER_IDENTIFIER s/SPECIES [b/BREED] r/REQUIREMENTS`

* `PET_NAME`: 1–40 letters, digits, spaces, apostrophes, hyphens, or full stops.
* `OWNER_IDENTIFIER`: the owner's existing eight-digit phone number, starting with `8` or `9`. Do not include spaces or hyphens.
* `SPECIES`: `Dog`, `Cat`, `Rabbit`, `Guinea Pig`, or `Other`, ignoring letter case. `Guinea_Pig` is also accepted.
* `BREED`: optional; if supplied, 2–50 letters, digits, spaces, apostrophes, hyphens, or full stops.
* `REQUIREMENTS`: 5–240 characters describing grooming or care needs. Do not include `/` in a value.

Each field may be supplied only once. More than one pet can belong to the same owner, but pet names under that owner
must differ after ignoring letter case and extra whitespace. Different owners may have pets with the same name.

Example output:
* `add-pet p/Mochi i/87438807 s/Dog b/Golden retriever r/Nervous around dryers`<br>
  Expected: `Pet added: Mochi (Dog, Golden retriever) under Alex Yeoh.`
* `add-pet p/BuBu i/87438807 s/Cat r/Calm with baths`<br>
  Expected: `Pet added: BuBu (Cat) under Alex Yeoh.`

Invalid values produce these messages:

| Field | Message |
| --- | --- |
| `PET_NAME` | `Pet name must be 1-40 characters and use valid name characters.` |
| `OWNER_IDENTIFIER` | `Owner identifier must be a valid phone number belonging to the specified client.` |
| `SPECIES` | `Species must be Dog, Cat, Rabbit, Guinea Pig, or Other.` |
| `BREED` | `Breed must be 2-50 characters and use valid text characters.` |
| `REQUIREMENTS` | `Requirements must be 5-240 characters and cannot contain /.` |

A missing required field reports `Missing required field: [field].`; repeated fields report
`Each pet field may be specified only once.` An unknown prefix or extra text outside a field reports
`Invalid command format. Check the command syntax and try again.` A valid phone number with no registered owner reports
`The owner identifier does not belong to the specified client.` A duplicate under the same owner reports
`This client already has a pet named [PET_NAME].` If saving fails, the pet is not added and the app reports
`Unable to save a pet. Please try again.`

### Deleting a pet: `delete-pet`

Deletes the specified pet from the address book.

Format: `delete-pet p/PET_NAME i/OWNER_IDENTIFIER`

* Both the pet name and its owner's phone number are required to identify the pet.
* The owner phone number must contain at least three digits.

Example:
* `delete-pet p/Milo i/98765432` deletes the pet named `Milo` owned by the person with phone number `98765432`.

### Clearing all entries: `clear`

Clears all contacts from the address book. Any saved appointments are retained separately.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

Contact data is saved automatically after successful contact commands. Appointments have a separate save file,
`data/appointments.json`, which loads when the app starts. Scheduling in the app is pending owner/pet lookup integration;
once enabled, each successful booking is saved automatically. You do not need to save manually.
Successful `add-pet` commands also save the new pet in `data/addressbook.json` automatically.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until you run a command (AddressBook saves after every command). Still, we recommend backing up the file before editing it.<br>
If the address-book file cannot be loaded, `add-pet` reports `Unable to verify existing records. Please contact support.`
and does not overwrite the file.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add Client** | `add-client n/NAME i/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​` <br> e.g., `add-client n/James Ho i/82224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Add pet** | `add-pet p/PET_NAME i/OWNER_IDENTIFIER s/SPECIES [b/BREED] r/REQUIREMENTS`<br> e.g., `add-pet p/Mochi i/87438807 s/Dog r/Nervous around dryers`
**Clear** | `clear`
**Delete Client** | `delete-client INDEX` or `delete-client i/PHONE_NUMBER`<br> e.g., `delete-client 3`<br> e.g., `delete-client i/91234567`
**Edit Client** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Delete pet** | `delete-pet p/PET_NAME i/OWNER_IDENTIFIER`<br> e.g., `delete-pet p/Milo i/98765432`
**Find** | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List** | `list`
**Help** | `help`

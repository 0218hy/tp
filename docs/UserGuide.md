---
layout: page
title: User Guide
---

BuBu helps **freelance pet groomers** keep track of clients, their pets and grooming appointments. Before a visit, you can check where to go, which service was booked and whether the pet needs special care. Type a command in the box at the top of the window and press <kbd>Enter</kbd>. The box below it shows the result.

* [Quick start](#quick-start)
* [Features](#features)
  * [Reading command formats](#reading-command-formats)
  * [Using shared fields](#how-fields-are-shared-between-commands)
  * [General command rules](#general-command-rules)
  * [Accepted formats](#accepted-formats)
  * [Command history](#recalling-previously-entered-commands)
  * [Displayed record lists](#switching-the-displayed-record-list)
  * [If a command fails](#if-a-command-fails)
  * [View help](#viewing-help-help)
  * [Managing clients](#managing-clients)
    * [Add a client](#adding-a-client-add-client)
    * [List or search clients](#listing-and-searching-clients-list-clients)
    * [Edit a client](#editing-a-client-edit-client)
    * [Delete a client](#deleting-a-client-delete-client)
  * [Managing pet profiles](#managing-pet-profiles)
    * [Add a pet](#adding-a-pet-add-pet)
    * [List pet profiles](#listing-pet-profiles-list-pets)
    * [Edit a pet](#editing-a-pet-edit-pet)
    * [Delete a pet](#deleting-a-pet-delete-pet)
  * [Scheduling appointments](#scheduling-appointments)
    * [Schedule an appointment](#scheduling-an-appointment-add-appointment)
    * [List appointments](#listing-appointments-list-appointments)
    * [Edit an appointment](#editing-an-appointment-edit-appointment)
    * [Delete an appointment](#deleting-an-appointment-delete-appointment)
  * [Other commands](#other-commands)
    * [Clear all entries](#clearing-all-entries-clear)
    * [Exit BuBu](#exiting-the-program-exit)
    * [Saving the data](#saving-the-data)
* [FAQ](#faq)
* [Known issues](#known-issues)
* [Command summary](#command-summary)

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Install Java 25. On a Mac, follow the [macOS Java installation guide](https://se-education.org/guides/tutorials/javaInstallationMac.html).
2. Download `bubu.jar` from the [BuBu releases page](https://github.com/AY2627S1-CS2103T-W08-2/tp/releases) and put it in a folder where you can save files.
3. Open a terminal in that folder and run `java -jar bubu.jar`. Use the same folder each time so BuBu can find your records.
4. On a fresh launch, try `list-clients q/87438807` and press <kbd>Enter</kbd>. With the sample records, the feedback reads `Found 1 client(s).` and the list shows Alex Yeoh. Type `list-clients` to see everyone again.

![Alex Yeoh’s client card beneath the feedback “Found 1 client(s).”](images/Ui.png)

*Searching for 87438807 returns one matching client, Alex Yeoh.*

Use the [command summary](#command-summary) to look up a command, or read the examples below to see what BuBu displays.

--------------------------------------------------------------------------------------------------------------------

## Features

### Reading command formats

| In the guide | What to type |
| --- | --- |
| `i/PHONE_NUMBER` | Keep the short label (the *prefix*) and replace the uppercase words with your value, e.g. `i/87438807`. |
| `[b/BREED]` | The field is optional. Type `b/Toy Poodle` if known, or leave the whole field out. Omit the brackets. |
| `[t/TAG]...` | You can enter several tags, e.g. `t/regular t/mobile`. |
| `INDEX` | Use the number beside the record in the displayed list. After filtering, use the numbers you see now. |

Separate fields with spaces. When copying from a PDF, check that fields on different lines still have a space between them.

### How fields are shared between commands

The same information is used throughout BuBu. This table shows where a field is reused and, importantly, whether it **finds an existing record** or **supplies a new value**.

| Record | When adding | When editing or deleting |
| --- | --- | --- |
| **Client** | `n/`, `i/`, `e/` and `a/` supply the new client's details. | `i/` finds the current client. `n/`, `p/`, `e/`, `a/` and `t/` supply replacement details in `edit-client`. |
| **Pet** | `p/` supplies the new pet's name. `i/` identifies the owner. `s/`, `b/` and `r/` supply its details. | `p/` and `i/` together find the current pet. `n/` supplies a replacement name. `s/`, `b/` and `r/` change the same pet details used by `add-pet`. |
| **Appointment** | `i/` and `p/` identify the owner and pet. `d/`, `st/`, `et/` and `s/` supply the booking details. | `d/` and `st/` together find the current booking. `i/`, `p/`, `nd/`, `nst/`, `et/` and `s/` supply replacements. `nd/` and `nst/` mean **new** date and start time. |

For example, use `p/Mochi i/81112222` to find Mochi under Jamie's phone number when editing or deleting. Use `n/Mochi Bean` only when you want to rename Mochi.

### General command rules

* Type the command word first. Labelled fields can appear in any order.
* Use each single-valued prefix only once. `t/TAG` is repeatable when a command accepts tags.
* Leading and trailing spaces around a field value are ignored.
* A blank required field is rejected. In an edit command, `b/` removes the breed and `t/` removes all tags.

### Accepted formats

Use this table for rules shared by more than one command. Command sections below explain which fields are required and any additional rules for that task.

| Data | Prefix or field | What you can enter |
| --- | --- | --- |
| Client name | `n/NAME` | 2–60 Unicode letters, combining marks, spaces, apostrophes (`'`), hyphens (`-`) or full stops (`.`). For example, `José Tan` and `李小明` are accepted. |
| Pet name | `p/PET_NAME`, `n/NEW_PET_NAME` | 1–40 letters, digits, spaces, apostrophes (`'`), hyphens (`-`) or full stops (`.`). |
| Client or owner phone | `i/PHONE_NUMBER`, `i/OWNER_IDENTIFIER`, `i/OWNER_PHONE` | Exactly eight digits starting with `8` or `9`, without spaces or hyphens. |
| New client phone | `p/NEW_PHONE` | The same eight-digit phone format as a client or owner phone. |
| Email | `e/EMAIL` | At most 254 characters, exactly one `@`, no whitespace and non-empty local and domain parts. The domain includes a full stop, such as `example.com`. |
| Address | `a/ADDRESS` | 5–120 characters using English letters, digits, spaces, commas, full stops, apostrophes (`'`), hyphens (`-`) or `#`. |
| Tag | `t/TAG` | A non-empty alphanumeric label without spaces. Repeat `t/` for several tags. |
| Species | `s/SPECIES` | `Dog`, `Cat`, `Rabbit`, `Guinea Pig`, `Guinea_Pig` or `Other`, ignoring case. |
| Breed | `b/BREED` | Optional 2–50 character description using letters, digits, spaces, apostrophes (`'`), hyphens (`-`) or full stops (`.`). BuBu does not verify the breed or its species. |
| Care requirements | `r/REQUIREMENTS` | 5–240 characters. `/` is allowed; a space followed by a recognised prefix begins another field. |
| Appointment date | `d/`, `nd/` | A real calendar date in `DD-MM-YYYY` form, for example `18-09-2027`. |
| Appointment time | `st/`, `et/`, `nst/` | Singapore time in `HH:mm` form, on the hour or half-hour. Start from `08:00` to `19:30`; end from `08:30` to `20:00`, on the same day and at least 30 minutes later. |
| Service | `s/SERVICE` | `Bath and brush`, `Full groom`, `Nail trim` or `Other`, ignoring case and extra spaces. |
| Displayed position | `INDEX` | A positive, one-based number shown beside a record in the current matching list. |

For a field not listed above, follow the rule in its command section.

### Recalling previously entered commands

Use the <kbd>↑</kbd> and <kbd>↓</kbd> arrow keys while the command box is focused to recall commands that you have
entered during the current application session. This is useful for repeating a command or editing a similar command
without typing it again.

* <kbd>↑</kbd> starts with the most recently entered command and moves towards older commands.
* <kbd>↓</kbd> moves towards newer commands. After the newest command, it restores a blank command box.
* At either end of the history, further presses of the corresponding arrow key leave the displayed text unchanged.
* Every non-empty submitted command is remembered, including commands that produced an error. If you begin typing a
  command and press <kbd>↑</kbd>, that unfinished command is remembered too; press <kbd>↓</kbd> to restore it.
  Recalling an unchanged command does not duplicate it in the history.
* Command history is kept only while BuBu is running and is cleared when the application closes.


### Switching the displayed record list

BuBu shows one list at a time. A successful
`list-clients` command displays clients, `list-pets` displays pets, and `list-appointments` displays
ongoing and future appointments. The selected panel remains visible until another list command or a successful `edit-client` command changes it.

Index-based deletion commands use the corresponding displayed list. For example, run `list-pets` before
`delete-pet 1` and run `list-appointments` before `delete-appointment 1`. If the wrong record type is currently
displayed, the deletion is rejected rather than applying the index to another list.


### If a command fails

If BuBu rejects a command, your saved records stay as they were. Read the feedback below the command box, correct the input and press <kbd>Enter</kbd> to try again.

If the data file is invalid at startup, commands other than `help` are blocked with
`Unable to verify existing records. Please contact support.` Restore an unchanged, valid BuBu backup and restart,
or contact support before continuing.

BuBu checks client phone and email uniqueness when it loads saved records. For example, client emails that differ only in domain capitalisation make the data file invalid. BuBu does not merge or delete these records automatically; preserve the file and restore a valid backup before restarting.
An unrecognised command word reports an unknown-command error. Use the exact lowercase command words below.
When a save fails, previous records are restored in memory, but list filters or the visible selection may have changed.

Read-only commands (`help`, `list-clients`, `list-pets`, `list-appointments`, and `exit`) do not create or rewrite the records data file. Commands that change records save automatically; commands that leave the records unchanged skip saving. During shutdown, BuBu attempts to save window preferences separately in `preferences.json`.

The examples below use Jamie Tan and her dog Mochi. Each example describes the records it needs. Use a separate practice folder if you want to try deleting records. For a booking, choose a free slot that starts after the current Singapore time.

### Viewing help: `help`

Displays the command reference in the feedback box. It does not change records or depend on the currently displayed list.

#### Format

```text
help
```

#### Notes

* This command takes no parameters. Extra text after `help` is ignored.

#### Examples

- `help`<br>
  **First line of the command reference** — `AVAILABLE COMMANDS`

#### Success and other behavior

* On success, BuBu displays a scrollable command reference, with emphasised command names and monospace formats.
* The feedback box expands for help and returns to its usual size after the next command. The selected record panel stays unchanged.
* You can also open the reference using the **Help** menu or <kbd>F1</kbd>.

#### Common issues

* `help` does not save records and does not require write access to the data file.
* `help` remains available even when an invalid data file prevents record commands from running.

## Managing clients

### Adding a client: `add-client`

Adds a client contact and grooming address to the saved client records. The command works from any panel and checks all saved clients for duplicates.

#### Format

```text
add-client n/NAME i/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]...
```

#### Notes

* `n/NAME`, `i/PHONE_NUMBER`, `e/EMAIL` and `a/ADDRESS` are required. Their shared formats are in [Accepted formats](#accepted-formats).
* `t/TAG` is optional. Repeat it for multiple tags, or omit it for no tags. Duplicate tags are stored once.
* Phone numbers and emails must be unique across clients. Email domains are compared case-insensitively, while the local part (before `@`) remains case-sensitive: `alice@example.com` conflicts with `alice@EXAMPLE.COM`, but `Alice@example.com` is distinct. The entered spelling is preserved for display and storage. Names and addresses need not be unique.
* A phone number recorded for another owner in a past booking cannot be assigned to a new or existing client. This prevents client edits or deletions from changing another person's historical appointment.

If you see `Email (e/) cannot be blank.`, type the email address immediately after `e/`. If no email field was supplied, add the whole field.

#### Examples

Jamie is a new client. Her phone number and email are available.

- `add-client n/Jamie Tan i/81112222 e/jamie@example.com a/12 Lily Road t/regular`<br>
  **Feedback** — `Client added: Jamie Tan (81112222).`

#### Success and other behavior

* On success, BuBu displays `Client added: NAME (PHONE).`
* The active client filter is preserved. The new client appears only if it matches the filter. The command does not switch away from a pet or appointment panel; use `list-clients` to view all clients.
* Changes are saved automatically. A save failure restores the previous records in memory and reports that the new client was not saved.

#### Common issues

* Enter each required field once. If a field is blank or uses an unsupported format, correct that field and try again.
* Use a phone number and email that are not already assigned to another client. A phone number used by a different historical booking owner cannot be reused.
* If saving fails, BuBu restores the earlier records in the open window. Check that the data folder is available and writable before retrying.

### Listing and searching clients: `list-clients`

Shows all saved clients or searches their contact details. The search examines all clients, not just the currently filtered list.

#### Format

```text
list-clients [q/KEYWORD]
```

#### Notes

* `q/KEYWORD` - optional; omit it to show all clients. When supplied, use 1-60 characters after trimming. Any text or punctuation is accepted, including `+`, commas, underscores and slashes; blank queries and control characters (such as internal tabs or line breaks) are rejected.
* The keyword is one case-insensitive substring, including internal spaces, matched against name, phone, email or address; tags are not searched.
* Specify `q/` at most once.

#### Examples

Search with Jamie’s number, then clear the search to see all clients again.

- `list-clients q/81112222`<br>
  **Feedback** — `Found 1 client(s).`
- `list-clients`<br>
  **Feedback** — `Listed all clients.`

#### Success and other behavior

* On success, BuBu displays `Listed all clients.`, `Found N client(s).` or `No clients match "KEYWORD".`
* The client panel is selected and results are sorted alphabetically by name, ignoring case.
* No matches is a successful search with an empty list. Saved records and their stored order are unchanged.

#### Common issues

* Leave out `q/` to return to the full client list.
* Use one nonblank `q/KEYWORD` field. Text outside that field is not supported.

### Editing a client: `edit-client`

Edits a client by their index in the currently displayed client list or by their current phone number. The phone identifier searches all saved clients and works even when a filter hides the client or another panel is selected.

#### Format

```text
edit-client INDEX [n/NAME] [p/NEW_PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...
edit-client i/CURRENT_PHONE [n/NAME] [p/NEW_PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...
```

#### Notes

* Supply either `INDEX` or `i/CURRENT_PHONE`, not both. `INDEX` is a positive, one-based position in the displayed client list. Index-based editing requires the client panel; invalid or out-of-range indexes are rejected without changing records. The active filter remains applied after editing.
* `i/CURRENT_PHONE` — the client's existing eight-digit phone number starting with `8` or `9`.
* `n/NAME`, `p/NEW_PHONE`, `e/EMAIL`, `a/ADDRESS` — optional updates using the same value constraints as `add-client`. Omitted fields keep their values.
* `t/TAG` — optional alphanumeric tags replacing all existing tags. Repeat it to supply multiple tags; use `t/` alone to clear tags. Omit it to keep tags.
* Supply at least one update. Each of `i/`, `n/`, `p/`, `e/` and `a/` may appear only once.
* A changed phone or email must not belong to another saved client, including clients hidden by a search. Email domains are compared case-insensitively; the local part (before `@`) remains case-sensitive, as for `add-client`.
* The new number must also be different from a number recorded for another owner in a past booking.
* The old `edit` command is unsupported.

If BuBu reports a missing client, check which phone field you used. For example, `edit-client i/91234567 p/89998888` looks up `91234567` and replaces it with `89998888`.

#### Examples

Jamie Tan has moved from 12 Lily Road to 24 Lily Road. Use her current phone number to update her address.

- `edit-client i/81112222 a/24 Lily Road`<br>
  **Feedback** — `Edited client: Jamie Tan; Phone: 81112222; Email: jamie@example.com; Address: 24 Lily Road; Tags: [regular]`

#### Success and other behavior

* On success, BuBu displays `Edited client: ...` and selects the client panel while preserving its active filter. Updated clients appear only if they still match the filter; a client that stops matching remains saved but disappears from the view.
* Editing a client updates their saved profile, linked pet owner profiles, and owner details in ongoing and future appointments.
* Completed appointments retain their recorded name, email, address and tags, so later client profile changes do not rewrite those past appointment details. An appointment is completed when its end date and time are at or before the time of the edit, using Singapore time. Use `list-clients` to obtain the client's current contact details.
* Phone numbers are an exception: because they identify clients, a phone-number change also updates linked pets and all linked appointments, including completed appointments. Changing an email does not break these links.
* Client edits preserve appointment dates, times, services and other pet details. Completed appointments can still be affected by linked pet renames or deletion: deleting a client or pet also deletes their linked appointments, including completed ones.

#### Common issues

* Choose one way to find the client: the displayed `INDEX` or their current `i/PHONE_NUMBER`.
* Add at least one replacement field. The new phone number and email must not already belong to another client.
* When using an index, run `list-clients` first. If saving fails, BuBu restores the earlier client, pet and appointment records in the open window.

### Deleting a client: `delete-client`

Deletes a saved client and all pets and appointments linked to that client. Index-based deletion uses the displayed
client list; phone-based deletion searches all saved clients.

#### Format

```text
delete-client INDEX
delete-client i/PHONE_NUMBER
```

#### Notes

* `INDEX` — a positive integer referring to the client's one-based position in the currently displayed client list,
  including any search filter. The client panel must be displayed.
* `i/PHONE_NUMBER` — alternatively, a valid saved eight-digit phone number starting with `8` or `9`. This form can
  delete a client even when a filter or another panel hides them.
* Provide either an index or a phone identifier, not both. Specify `i/` only once.

#### Examples

This example assumes Jamie has moved to 24 Lily Road and has no linked pets or bookings.

- `delete-client i/81112222`<br>
  **Feedback** — `Deleted client: Jamie Tan; Phone: 81112222; Email: jamie@example.com; Address: 24 Lily Road; Tags: [regular]. Removed 0 pets and 0 appointments.`

#### Success and other behavior

* On success, BuBu displays `Deleted client: CLIENT_DETAILS. Removed PET_COUNT and APPOINTMENT_COUNT.`
  The counts use singular or plural wording, such as `1 pet` or `2 appointments`.
* All pets and appointments belonging to that client are also deleted, including completed appointments. Other clients' records are preserved.
* The selected panel does not change. There is no confirmation prompt.

#### Common issues

* Choose one deletion method: a positive displayed `INDEX` or one saved `i/PHONE_NUMBER`.
* Run `list-clients` before using an index. A phone number works even when a search hides the client.
* Deletion removes the client's linked pets and appointments too. Back up the data file first if you may need those records.

## Managing pet profiles

### Adding a pet: `add-pet`

Adds a pet under a registered client identified by phone number. It searches all clients and does not depend on the displayed client list.

#### Format

```text
add-pet p/PET_NAME i/OWNER_IDENTIFIER s/SPECIES [b/BREED] r/REQUIREMENTS
```

#### Notes

**Enter the new pet and owner**

* `p/PET_NAME` — required name for the **new** pet. See [Accepted formats](#accepted-formats).
* `i/OWNER_IDENTIFIER` — required registered owner [phone number](#accepted-formats). This links the new pet to that client.

**Enter the pet's details**

* `s/SPECIES` and `r/REQUIREMENTS` are required. `b/BREED` is optional. See [Accepted formats](#accepted-formats).
  Omit the breed when unknown; a supplied blank breed is invalid.
* Under one owner, pet names must be unique after ignoring case and extra whitespace. Different owners may have pets with the same name.
* Separate fields with ordinary spaces. Tabs inside the command are part of field values, not field separators.

#### Examples

Once Jamie is registered, you can add her dog Mochi.

- `add-pet p/Mochi i/81112222 s/Dog b/Toy Poodle r/Use low heat`<br>
  **Feedback** — `Pet added: Mochi (Dog, Toy Poodle) under Jamie Tan.`

#### Success and other behavior

* On success, BuBu displays `Pet added: PET_NAME (SPECIES, BREED) under OWNER.` The breed is omitted from the message when not supplied.
* The active pet filter is preserved. A new pet appears only if it matches the filter. The client filter and selected panel are unchanged; use `list-pets` to view all pets.
* Changes are saved automatically. A save failure restores the previous records in the open window and explains what to do next.

#### Common issues

* Add the owner with `add-client` before adding their pet. The supplied phone number must belong to that client.
* Enter each required field once and use one of the listed species. A pet name can be reused for another owner, but not for two pets under the same owner.
* If another BuBu window is open, wait for it to finish saving. If records changed outside this window, close and reopen BuBu before retrying.

### Listing pet profiles: `list-pets`

Shows all saved pets or only those belonging to one registered client. It selects the pet panel without changing saved records or the client filter.

#### Format

```text
list-pets [i/OWNER_IDENTIFIER]
```

#### Notes

* `i/OWNER_IDENTIFIER` — supply a registered client’s [phone number](#accepted-formats) to see only their pets. Omit it to list every owner’s pets.
* Specify `i/` at most once. Keyword or pet-name search is not supported.

#### Examples

In this example Mochi is Jamie’s only pet.

- `list-pets i/81112222`<br>
  **Feedback** — `Found 1 pet(s) under Jamie Tan.`
- `list-pets`<br>
  **Feedback** — `Listed all pets.`

#### Success and other behavior

* On success, BuBu displays `Listed all pets.`, `Found N pet(s) under OWNER.` or `OWNER has no saved pet profiles.`
* Pets are ordered by owner name, owner phone, then pet name. Names are compared ignoring case; owners with the same name remain separate groups.
* Each owner group has a heading. Record numbers continue across groups. Pet cards show name, species, breed and care requirements; an absent breed appears as `Not recorded`.
* An empty result is successful. This command does not save or change records.

#### Common issues

* `Jamie Tan has no saved pet profiles.` means BuBu found Jamie. Use `add-pet` to register her first pet.
* `No saved client has this phone number.` means BuBu could not find the owner. Run `list-clients` to check their number. If they are a new client, register them with `add-client` first.

### Editing a pet: `edit-pet`

Edits a saved pet identified by its current name and owner's phone number, or by its one-based index in the displayed pet list. Name-and-phone lookup searches all saved pets, regardless of the displayed list; index lookup requires the pet list to be displayed. The command never transfers ownership.

#### Format

```text
edit-pet INDEX [n/NEW_PET_NAME] [s/SPECIES] [b/BREED] [r/REQUIREMENTS]
edit-pet p/PET_NAME i/OWNER_IDENTIFIER [n/NEW_PET_NAME] [s/SPECIES] [b/BREED] [r/REQUIREMENTS]
```

#### Notes

**Find the pet to edit**

* `INDEX` — use this instead of the fields below to select the pet's one-based position in the displayed pet list, including any owner filter. The pet list must be the active display.
* `p/PET_NAME` — current pet name; required with `i/` when not using an index. It follows the same 1–40 character rules as `add-pet`. Lookup ignores case and extra whitespace.
* `i/OWNER_IDENTIFIER` — current owner's eight-digit phone number; required with `p/` when not using an index.

**Choose what to change**

* `n/NEW_PET_NAME` — optional replacement name. It follows the same rules as `add-pet` and must not match another pet under the owner after case and whitespace normalisation.
* `s/SPECIES` — optional replacement species. It accepts the same values as `add-pet`: `Dog`, `Cat`, `Rabbit`, `Guinea Pig`, `Other` or `Guinea_Pig`.
* `b/BREED` — optional replacement breed. A nonblank replacement follows the same format as `add-pet`. Use `b/` alone (or a whitespace-only value) to remove the breed; omit the field to keep it unchanged.
  For mixed breeds, use a description such as `b/Poodle-Labrador mix`; `/` and `&` are not accepted.
  BuBu checks the text format only; it does not verify that the breed exists or matches the selected species.
* `r/REQUIREMENTS` — optional replacement requirements. It follows the same 5–240 character rules as `add-pet` and may contain `/`, e.g. `shampoo/conditioner`.
* Supply either `INDEX` or both `p/PET_NAME` and `i/OWNER_IDENTIFIER`, not both targeting forms. Supply at least one update. Omitted fields are unchanged. Every prefix may appear only once.

#### Examples

Jamie asks you to keep the dryer on low for Mochi.

- `edit-pet p/Mochi i/81112222 r/Keep dryer on low`<br>
  **Feedback** — `Edited pet: Mochi`

#### Success and other behavior

* On success, BuBu displays `Edited pet: PET_NAME`.
* The active pet filter is preserved and matching pets show their updated details. Pets outside the filter remain hidden; the selected panel does not change.
* Linked ongoing and future appointments receive the updated pet name, species, breed and care requirements. Completed appointments keep their recorded pet details, except that a changed pet name still updates linked bookings. An appointment is completed when its end date and time are at or before the time of the edit, using Singapore time. Appointment dates, times, services and recorded owner details remain unchanged.

#### Common issues

* Find the pet using either a displayed `INDEX` or both its current `p/PET_NAME` and `i/OWNER_IDENTIFIER`. Do not combine these methods.
* Run `list-pets` before using an index. For name-and-phone lookup, check the pet's current name and the owner's registered phone number.
* Supply at least one change. A replacement name cannot duplicate another pet under the same owner.

### Deleting a pet: `delete-pet`

Deletes a saved pet and its linked appointments. Index-based deletion uses the displayed pet list; name-and-phone deletion searches all saved pets.

#### Format

```text
delete-pet INDEX
delete-pet p/PET_NAME i/OWNER_IDENTIFIER
```

#### Notes

* `INDEX` — a positive integer referring to the pet's one-based position in the current pet list, including any owner filter.
* `p/PET_NAME` — alternatively, the saved pet name; 1–40 letters, digits, spaces, apostrophes, hyphens or full stops. The name is matched case-insensitively; leading and trailing whitespace is ignored, and repeated internal whitespace is treated as a single space, consistent with edit lookup.
* `i/OWNER_IDENTIFIER` — required with `p/`; the owner's eight-digit phone number starting with `8` or `9`.
* Provide an index or both identifiers, not both formats.

#### Examples

Use Mochi’s name and Jamie’s phone number to remove the pet and any linked bookings.

- `delete-pet p/Mochi i/81112222`<br>
  **Feedback** — `Deleted pet: Mochi`

#### Success and other behavior

* On success, BuBu displays `Deleted pet: PET_NAME`.
* Appointments linked to that pet are also deleted. The client and other pets are preserved.
* The selected panel does not change. There is no confirmation prompt.

#### Common issues

* Choose one deletion method: a displayed `INDEX` or both `p/PET_NAME` and `i/OWNER_IDENTIFIER`.
* Run `list-pets` before using an index. If BuBu cannot find the pet by name and phone number, check the owner’s registered phone number.
* Deleting a pet also removes its linked appointments. The client and their other pets remain saved.

## Scheduling appointments

### Scheduling an appointment: `add-appointment`

Schedules a grooming appointment for a registered pet and client. It searches saved records rather than the currently displayed list and checks the slot against all appointments.

#### Format

```text
add-appointment i/OWNER_PHONE p/PET_NAME d/DD-MM-YYYY st/HH:mm et/HH:mm s/SERVICE
```

#### Notes

* `i/OWNER_PHONE` — enter the registered owner’s [phone number](#accepted-formats).
* `p/PET_NAME` — required; a pet registered under that client. Use 1–40 letters, digits, spaces, apostrophes, hyphens or full stops; lookup ignores case and repeated whitespace.
* `d/DD-MM-YYYY` — required; a real calendar date. The combined date and start time must be strictly in the future in Singapore time.
* `st/HH:mm` — required; a 24-hour start time from `08:00` to `19:30`, in 30-minute intervals.
* `et/HH:mm` — required; an end time from `08:30` to `20:00`, in 30-minute intervals and at least 30 minutes after the start on the same day.
* `s/SERVICE` — required; `Bath and brush`, `Full groom`, `Nail trim` or `Other`. Case and extra whitespace are ignored.
* Slots must not overlap any other appointment, including bookings for different pets; adjacent slots are allowed.

#### Examples

Jamie wants a full groom for Mochi. These examples assume the date is in the future and the first slot is free.

- `add-appointment i/81112222 p/Mochi d/18-09-2027 st/10:00 et/11:00 s/Full groom`<br>
  **Feedback** — `Appointment scheduled: Mochi; Owner phone: 81112222; Date: 18-09-2027; Time: 10:00-11:00; Service: Full groom`

If you try to book a second service at 10:30, BuBu identifies the booking that needs that time.

- `add-appointment i/81112222 p/Mochi d/18-09-2027 st/10:30 et/11:30 s/Nail trim`<br>
  **Feedback** — `This appointment overlaps an existing booking for Mochi on 18-09-2027, 10:00–11:00.`

#### Success and other behavior

* On success, BuBu displays `Appointment scheduled: ...` with the pet, owner phone, date, time and service.
* The booking records the participant details available at booking time. The selected panel does not change; use `list-appointments` to view the updated schedule.

#### Common issues

* Add the owner and pet first. The pet must be registered under the owner phone number you enter.
* You can book a slot later today or on a future date. Its start time must be after the current Singapore time. Choose a slot within business hours and one of the listed services.
* Choose a free slot. BuBu rejects overlaps even when the existing booking is for a different pet.

### Listing appointments: `list-appointments`

Shows ongoing and future appointments in the appointment panel, or all appointments on a specified date, ordered by date and start time. It reads all saved bookings rather than depending on the currently displayed list.

#### Format

```text
list-appointments [d/DD-MM-YYYY]
```

#### Notes

* Without a date filter, only ongoing and future appointments are shown, using Singapore time. Appointments that have already ended, including earlier today or exactly now, are excluded.
* `d/` — optional date in `DD-MM-YYYY` format. Shows every appointment on that date, including completed appointments. Past dates are allowed.
* Only one date may be supplied. Invalid dates, extra arguments, phone filters, and pet-name filters are rejected.

#### Examples

Mochi has the only booking on this date.

- `list-appointments d/18-09-2027`<br>
  **Feedback** — `Appointments on 18-09-2027 found: 1.`

If no bookings exist on that date, the same command returns `No appointments on 18-09-2027.`

#### Success and other behavior

* On success, BuBu displays `Ongoing or future appointments found: N.` or `No ongoing or future appointments.`
* With a date filter, BuBu displays `Appointments on DD-MM-YYYY found: N.` or `No appointments on DD-MM-YYYY.`
* Without a date filter, an appointment is included if its end time is after the time when the command runs, using Singapore time. Run the command again to refresh this time-based selection.
* Appointments have date headings and continuous record numbers. Cards show service and timing, pet and owner details, address and care notes saved with the booking.
* Completed appointments remain saved and can be viewed with the date filter. No matches is a successful result; this command does not save or change records.

#### Common issues

* Use at most one `d/DD-MM-YYYY` field. Omit it to see ongoing and future bookings.
* Use a real calendar date. Phone-number and pet-name filters are not available for this command.

### Editing an appointment: `edit-appointment`

Edits a saved appointment identified by its current date and start time, or by its displayed appointment-list index.

#### Format

```text
edit-appointment d/CURRENT_DATE st/CURRENT_START_TIME [i/NEW_OWNER_PHONE] [p/NEW_PET_NAME] [nd/NEW_DATE] [nst/NEW_START_TIME] [et/NEW_END_TIME] [s/NEW_SERVICE]
edit-appointment INDEX [i/NEW_OWNER_PHONE] [p/NEW_PET_NAME] [nd/NEW_DATE] [nst/NEW_START_TIME] [et/NEW_END_TIME] [s/NEW_SERVICE]
```

#### Notes

* `d/` and `st/` identify the existing appointment. Identifier-based edits search all saved appointments.
* `INDEX` refers to the one-based position in the currently displayed appointment list. Run `list-appointments`
  first when using this form; a date-filtered appointment list may also be used.
* Provide either the date/start-time identifiers or an index, but not both. Replacement fields can be combined with
  either identification form.
* `CURRENT_DATE` and `NEW_DATE` use `DD-MM-YYYY`. `CURRENT_START_TIME`, `NEW_START_TIME`, and `NEW_END_TIME` use
  `HH:mm`.
* When supplied, `i/NEW_OWNER_PHONE` must identify a registered client, and `p/NEW_PET_NAME` must identify a pet
  registered under that client. Pet-name matching ignores letter case and repeated whitespace.
* The [same booking hours and minimum duration](#accepted-formats) apply when moving or resizing a booking.
* Use `nd/` and `nst/` when changing the appointment's date or start time. At least one replacement field is required.
* The resulting appointment must start in the future in Singapore time and must not overlap another appointment.
  An appointment whose resulting start time is in the past cannot be edited.

#### Examples

Jamie wants a nail trim instead of a full groom. Identify the existing booking by its date and start time.

- `edit-appointment d/18-09-2027 st/10:00 s/Nail trim`<br>
  **Feedback** — `Edited appointment: Mochi; Owner phone: 81112222; Date: 18-09-2027; Time: 10:00-11:00; Service: Nail trim`

#### Success and other behavior

* On success, BuBu displays `Edited appointment: ...` with the updated booking details.
* Fields that are not supplied keep their existing values. The selected panel and active appointment filter are
  preserved; an updated appointment that no longer matches the filter disappears from the view but remains saved.

#### Common issues

* Find the booking using either a displayed `INDEX` or its current `d/` date and `st/` start time. Do not combine these methods.
* Run `list-appointments` before using an index. The date-and-start-time method needs the booking's exact start time.
* Supply at least one change. The resulting booking must be in the future, within business hours, at least 30 minutes long and free of overlaps.

### Deleting an appointment: `delete-appointment`

Deletes a booking by its displayed index or its date and start time. Date-and-start-time lookup searches all saved appointments, including completed bookings hidden from the list.

#### Format

```text
delete-appointment INDEX
delete-appointment d/DD-MM-YYYY st/HH:mm
```

#### Notes

* `INDEX` — a positive integer referring to the appointment's one-based position in the currently displayed appointment
  list. Run `list-appointments` first; a date-filtered appointment list may also be used.
* `d/DD-MM-YYYY` — alternatively, a real calendar date; it need not be in the future.
* `st/HH:mm` — required with `d/`; use the exact start time of the appointment to delete, not any time within its
  duration. It must be from `08:00` to `19:30` in 30-minute intervals.
* Provide an index or both date and time, not both formats.

#### Examples

To cancel Mochi’s nail trim, use the start time printed on the booking.

- `delete-appointment d/18-09-2027 st/10:00`<br>
  **Feedback** — `Deleted appointment: Mochi; Owner phone: 81112222; Date: 18-09-2027; Time: 10:00-11:00; Service: Nail trim`

#### Success and other behavior

* On success, BuBu displays `Deleted appointment: ...` with the booking details.
* Client and pet profiles and other appointments are preserved. The selected panel does not change.

#### Common issues

* Choose one deletion method: a displayed `INDEX` or the booking's exact `d/` date and `st/` start time.
* Run `list-appointments` before using an index. A date-and-start-time lookup works even for a completed booking hidden from the list.
* The start time must match the booking's beginning, not any time during the service.

## Other commands

### Clearing all entries: `clear`

Deletes all saved clients, pets and appointments, including completed bookings and records hidden by filters. It does not depend on the displayed list.

#### Format

```text
clear
```

#### Notes

* This command takes no parameters. Extra text is ignored.

#### Examples

- `clear`<br>
  **Feedback** — `Clients, pets, and appointments have been cleared!`

#### Success and other behavior

* On success, BuBu displays `Clients, pets, and appointments have been cleared!`
* The command takes effect immediately without confirmation. The selected panel remains selected but contains no records.

#### Common issues

* Storage error — the data file cannot be written, for example because of file or folder permissions.
* `Unable to verify existing records. Please contact support.` — commands are blocked because the data file was invalid at startup.

### Exiting the program: `exit`

Closes BuBu without deleting saved records. It does not depend on the displayed list.

#### Format

```text
exit
```

#### Notes

* This command takes no parameters. Extra text is ignored.

#### Examples

- `exit`<br>
  **Feedback** — `Exiting BuBu as requested ...`

#### Success and other behavior

* On success, BuBu displays `Exiting BuBu as requested ...` and closes.
* `exit` does not save record data. Record changes are saved when the commands that make them succeed. During shutdown, BuBu attempts to save window preferences separately in `preferences.json`. Session command history is not retained.

#### Common issues

* `Unable to verify existing records. Please contact support.` — commands, including `exit`, are blocked by the startup data-file error; close the window instead.

### Saving the data

Clients, pets, and appointments are saved together in `data/bububook.json` automatically after successful changes.
If saving fails, BuBu restores the previous records in the open window and displays an error. Read-only commands (`help`, `exit`,
`list-clients`, `list-pets` and `list-appointments`) do not create or rewrite this file. Other commands also skip
saving when the records are unchanged. Window preferences are saved separately in `preferences.json` during shutdown.

If another BuBu window changes the same data file, your next change is rejected with
`Records changed outside this BuBu window. Close and reopen BuBu, then retry your command.`
The other window's saved records are preserved, and your attempted change is undone in memory.
Reopen the affected window to load the latest records before entering the command again.
If another window is still saving, BuBu instead asks you to retry your command.

BuBu keeps a small `data/bububook.json.lock` file beside the records to coordinate saves between windows.
Leave it in place while BuBu is open. It is normal for this file to remain after closing the app;
the operating system releases the lock when the save finishes or the process exits.

If upgrading from a version that used `data/groombook.json`, close BuBu and copy that file to
`data/bububook.json` before launching the new version. Keep the original file as a backup.
The app loads this file at startup; you do not need to save manually. Window preferences are stored separately.
Bookings use the same data file as their registered clients and pets. Client edits refresh owner details in linked
ongoing and future bookings; pet edits refresh their pet details. A booking is ongoing or future if its end date
and time are strictly after the time of the edit in Singapore time. Bookings ending before or exactly at that time
are completed and retain their historical details, except that linked owner-phone and pet-name changes still apply.
Profile edits do not change appointment dates, times or services. Pet edits keep the recorded owner details;
client edits keep the recorded pet details other than the pet's owner information.
The owner's phone number links clients, pets, and appointments. Changing a client's email does not break these links;
completed appointments keep their recorded email. Changing the phone number updates the linked pets and appointments.
Deleting a client or pet also deletes their linked bookings. The recorded pet name is displayed with its capitalization.

Older bookings that saved only an owner phone and pet name can be loaded while both participants remain
registered. If either is missing, restore the missing participant from a backup before upgrading; those older
bookings do not contain a full participant profile.

### Data files and supported backups

By default, grooming records are saved in `data/bububook.json` relative to the terminal's working directory when
BuBu is launched, with `clients`, `pets`, and `appointments` lists. Following the Quick start instructions makes
this the folder containing the JAR. Always launch from the same folder to load the same records; launching from
another folder can make BuBu use a different data file.

Manage records using BuBu commands. BuBu does not support importing external JSON, merging data files, or manually
editing saved records. You may copy or restore an unchanged backup of a file saved by BuBu while the app is closed.
Every occurrence of an owner's phone number refers to the same client, including in completed appointments.
Manually constructing records that give different people the same phone number is unsupported and can cause their
records to be updated or deleted together; file validation does not detect every such inconsistency.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
Keep a backup of the data file. If BuBu detects that the file is invalid, it opens without displaying your records and blocks
commands with `Unable to verify existing records. Please contact support.` to avoid overwriting your records.
Preserve the affected file and restore an unchanged, valid BuBu backup before restarting, or contact support.
</div>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: With the default data-file location, follow these steps:

1. Close BuBu on both computers and keep a backup of `data/bububook.json` from your original launch folder.
2. Put the JAR in your intended launch folder on the other computer. Create a `data` subfolder there if it does not exist.
3. Copy your backup into that subfolder as `bububook.json`. If a destination file already exists, back it up before replacing it.
4. Open a terminal in that same launch folder and run `java -jar bubu.jar`. BuBu will load the copied records.

A fresh launch without a data file loads sample records into memory; it does not create `data/bububook.json`.
You do not need to add a dummy record before copying your backup. Keep the destination app closed while copying:
an already-open window retains its in-memory records and may reject later changes because the data file changed externally.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.

--------------------------------------------------------------------------------------------------------------------

## Command summary

The examples use Jamie and Mochi. To try them in sequence, add Jamie, add Mochi, then book the appointment. Try the list and edit commands while the records exist. Leave deletion until the end, starting with the appointment, then the pet, then the client.

Action | Format, Examples
--------|------------------
**Add Client** | `add-client n/NAME i/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]...`<br> e.g., `add-client n/Jamie Tan i/81112222 e/jamie@example.com a/12 Lily Road t/regular`
**List clients** | `list-clients [q/KEYWORD]`<br> e.g., `list-clients q/81112222`
**Edit client** | `edit-client INDEX [n/NAME] [p/NEW_PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...` or `edit-client i/CURRENT_PHONE [n/NAME] [p/NEW_PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `edit-client i/81112222 a/24 Lily Road`
**Delete Client** | `delete-client INDEX` or `delete-client i/PHONE_NUMBER`<br> e.g., `delete-client i/81112222`
**Add pet** | `add-pet p/PET_NAME i/OWNER_IDENTIFIER s/SPECIES [b/BREED] r/REQUIREMENTS`<br> e.g., `add-pet p/Mochi i/81112222 s/Dog r/Use low heat`
**List pets** | `list-pets [i/OWNER_IDENTIFIER]`<br> e.g., `list-pets i/81112222`
**Edit pet** | `edit-pet INDEX [n/NEW_PET_NAME] [s/SPECIES] [b/BREED] [r/REQUIREMENTS]` or `edit-pet p/PET_NAME i/OWNER_IDENTIFIER [n/NEW_PET_NAME] [s/SPECIES] [b/BREED] [r/REQUIREMENTS]`<br> e.g., `edit-pet p/Mochi i/81112222 r/Keep dryer on low`
**Delete pet** | `delete-pet INDEX` or `delete-pet p/PET_NAME i/OWNER_IDENTIFIER`<br> e.g., `delete-pet p/Mochi i/81112222`
**Add appointment** | `add-appointment i/OWNER_PHONE p/PET_NAME d/DD-MM-YYYY st/HH:mm et/HH:mm s/SERVICE`<br> e.g., `add-appointment i/81112222 p/Mochi d/18-09-2027 st/10:00 et/11:00 s/Full groom`
**List appointments** | `list-appointments [d/DD-MM-YYYY]`<br> e.g., `list-appointments d/18-09-2027`
**Edit appointment** | `edit-appointment d/CURRENT_DATE st/CURRENT_START_TIME [i/NEW_OWNER_PHONE] [p/NEW_PET_NAME] [nd/NEW_DATE] [nst/NEW_START_TIME] [et/NEW_END_TIME] [s/NEW_SERVICE]` or `edit-appointment INDEX [...]`<br> e.g., `edit-appointment d/18-09-2027 st/10:00 s/Nail trim`
**Delete appointment** | `delete-appointment INDEX` or `delete-appointment d/DD-MM-YYYY st/HH:mm`<br> e.g., `delete-appointment d/18-09-2027 st/10:00`
**Clear** | `clear`
**Help** | `help`
**Exit** | `exit`

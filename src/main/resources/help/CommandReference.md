AVAILABLE COMMANDS

General behavior
  Read-only commands (help, list-clients, list-pets, list-appointments, and exit) do not write record data. Record-changing commands save automatically.
  If records are corrupt at startup, help remains available; other commands are blocked. Exit through the window controls if the exit command is unavailable.

Help
  help
  Restrictions: Takes no parameters; extra text is ignored.
  Help menu and F1 run this same command. Help remains available when saved records are corrupt.
  If the bundled reference is missing or unreadable, BuBu displays: "Unable to load the command reference. Check that you are using a complete BuBu release JAR."

Add client
  add-client n/NAME i/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]...
  Restrictions:
    NAME: required; 2–60 Unicode code points after NFC normalization; Unicode letters/combining marks, spaces, apostrophes, hyphens, and full stops.
    PHONE_NUMBER: required; 8 digits, starting with 8 or 9; unique across clients.
    EMAIL: required; at most 254 characters, one @, no whitespace, non-empty local/domain parts, and a dotted domain; unique across clients. Domain matching is case-insensitive; local-part matching is case-sensitive.
    ADDRESS: required; 5–120 characters; English letters, digits, spaces, commas, full stops, apostrophes, hyphens, or #.
    TAG: optional; non-empty alphanumeric text without spaces. Repeat t/ for multiple tags. Duplicate tags are stored once.
    Required fields cannot be blank. Each of n/, i/, e/, and a/ may appear once; surrounding whitespace is trimmed.

List clients
  list-clients [q/KEYWORD]
  Restrictions: q/ is optional and may appear once. When supplied, the trimmed keyword must be 1–60 characters and contain no control characters. Any other text or punctuation is accepted. Matching is case-insensitive substring search over name, phone, email, and address; tags are not searched.

Edit client
  edit-client INDEX [n/NAME] [p/NEW_PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...
  edit-client i/CURRENT_PHONE [n/NAME] [p/NEW_PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...
  Restrictions:
    Choose one target: a positive, one-based INDEX in the displayed client list, or the client's current 8-digit phone starting with 8 or 9. Index editing requires the client panel; phone lookup searches all clients.
    Supply at least one update. n/, p/, e/, and a/ follow the add-client field rules. A changed phone or email must remain unique.
    Repeat t/ to replace tags; use t/ alone to clear them. Omit t/ to keep existing tags.
    Each single-valued prefix may appear once. A phone change updates linked pet owners and appointment owner identifiers. Ongoing and future appointments show updated owner details; completed appointments keep recorded details except for the phone identifier.

Delete client
  delete-client INDEX
  delete-client i/PHONE_NUMBER
  Restrictions: Choose one target: a positive, one-based INDEX in the displayed client list, or a valid 8-digit phone starting with 8 or 9. Index deletion requires the client panel. The phone prefix may appear once. Deleting a client also deletes their pets and appointments.

Add pet
  add-pet p/PET_NAME i/OWNER_IDENTIFIER s/SPECIES [b/BREED] r/REQUIREMENTS
  Restrictions:
    PET_NAME: required; 1–40 letters, digits, spaces, apostrophes, hyphens, or full stops. Names must be unique per owner after case and whitespace normalization.
    OWNER_IDENTIFIER: required; an existing client's 8-digit phone starting with 8 or 9.
    SPECIES: required; Dog, Cat, Rabbit, Guinea Pig, or Other; case-insensitive. Guinea_Pig is also accepted.
    BREED: optional; 2–50 letters, digits, spaces, apostrophes, hyphens, or full stops. Omit if unknown; a blank value is invalid.
    REQUIREMENTS: required; 5–240 characters; / is allowed. A space followed by another recognized field prefix starts that field.
    Each prefix may appear once. Pet names may repeat under different owners.

List pets
  list-pets [i/OWNER_IDENTIFIER]
  Restrictions: i/ is optional and may appear once; it must identify an existing client using an 8-digit phone starting with 8 or 9. No keyword or pet-name filter is supported.

Edit pet
  edit-pet p/PET_NAME i/OWNER_IDENTIFIER [n/NEW_PET_NAME] [s/SPECIES] [b/BREED] [r/REQUIREMENTS]
  Restrictions:
    p/ and i/ are required and identify the pet by its current name and owner's 8-digit phone starting with 8 or 9. Name lookup ignores case and repeated whitespace.
    Supply at least one update. NEW_PET_NAME follows the pet-name rules and must remain unique for that owner after normalization. SPECIES follows add-pet rules.
    BREED follows add-pet rules; use b/ with no value to remove the breed. REQUIREMENTS follows add-pet rules.
    Each prefix may appear once. Ownership cannot be changed. Renaming a pet updates its name in linked appointments; other appointment details remain unchanged.

Delete pet
  delete-pet INDEX
  delete-pet p/PET_NAME i/OWNER_IDENTIFIER
  Restrictions: Choose one target: a positive, one-based INDEX in the displayed pet list, or both the saved pet name and owner's valid 8-digit phone. Index deletion requires the pet panel. Name matching ignores case and repeated whitespace. The p/ and i/ prefixes may each appear once. Deleting a pet also deletes its linked appointments.

Add appointment
  add-appointment i/OWNER_IDENTIFIER p/PET_NAME d/DD-MM-YYYY st/HH:mm et/HH:mm s/SERVICE
  Restrictions:
    OWNER_IDENTIFIER: required; an existing client's 8-digit phone starting with 8 or 9.
    PET_NAME: required; a pet registered under that owner; 1–40 letters, digits, spaces, apostrophes, hyphens, or full stops. Lookup ignores case and repeated whitespace.
    DATE: required; a real DD-MM-YYYY date. The appointment must start in the future in Singapore time.
    START_TIME: required; HH:mm from 08:00 to 19:30, in 30-minute intervals.
    END_TIME: required; HH:mm from 08:30 to 20:00, in 30-minute intervals, at least 30 minutes after the start on the same day.
    SERVICE: required; Bath and brush, Full groom, Nail trim, or Other; case and extra whitespace are ignored.
    Each prefix may appear once. Appointments cannot overlap any booking, including bookings for other pets; adjacent slots are allowed.

List appointments
  list-appointments [d/DD-MM-YYYY]
  Restrictions: d/ is optional and may appear once; it must be a real date in DD-MM-YYYY format. With no date, only ongoing and future appointments are shown. With a date, all appointments on that date are shown, including completed and past bookings. Other filters are unsupported.

Edit appointment
  edit-appointment d/CURRENT_DATE st/CURRENT_START_TIME [i/NEW_OWNER_PHONE] [p/NEW_PET_NAME] [nd/NEW_DATE] [nst/NEW_START_TIME] [et/NEW_END_TIME] [s/NEW_SERVICE]
  edit-appointment INDEX [i/NEW_OWNER_PHONE] [p/NEW_PET_NAME] [nd/NEW_DATE] [nst/NEW_START_TIME] [et/NEW_END_TIME] [s/NEW_SERVICE]
  Restrictions:
    Choose one target: both d/ and st/ identifying the current appointment, or a positive, one-based INDEX in the displayed appointment list. Index editing requires the appointment panel.
    Supply at least one replacement field. Each prefix may appear once; do not combine the index target with d/ or st/.
    CURRENT_DATE and NEW_DATE must be real DD-MM-YYYY dates. CURRENT_START_TIME, NEW_START_TIME, and NEW_END_TIME use HH:mm.
    Start times must be 08:00–19:30 and end times 08:30–20:00, in 30-minute intervals; duration must be at least 30 minutes. The updated appointment must start in the future in Singapore time.
    NEW_OWNER_PHONE must be an existing client's 8-digit phone starting with 8 or 9. NEW_PET_NAME must identify a pet registered under that owner and follows add-appointment name rules. SERVICE follows add-appointment rules.
    The updated slot cannot overlap another appointment. Editing preserves the active appointment filter.

Delete appointment
  delete-appointment INDEX
  delete-appointment d/DD-MM-YYYY st/HH:mm
  Restrictions: Choose one target: a positive, one-based INDEX in the displayed appointment list, or both a real date and the appointment's exact start time. Index deletion requires the appointment panel. Dates need not be in the future. Start time must be HH:mm from 08:00 to 19:30 in 30-minute intervals. Each prefix may appear once.

Clear all clients, pets, and appointments
  clear
  Restrictions: Takes no parameters; extra text is ignored. Deletes all records immediately without confirmation.

Exit
  exit
  Restrictions: Takes no parameters; extra text is ignored. Does not write record data; window preferences may be saved separately.

Command input
  Press Up/Down to recall commands from the current session. Up stops at the oldest command; Down past the newest restores a blank prompt. A partially typed command is retained while you navigate.

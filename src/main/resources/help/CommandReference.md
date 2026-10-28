AVAILABLE COMMANDS

Help
  help

Add client
  add-client n/NAME i/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]...

List clients
  list-clients [q/KEYWORD]

Edit client
  edit-client i/CURRENT_PHONE [n/NAME] [p/NEW_PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...
  i/ identifies the client; p/ changes their phone. Use the new number in i/ after a phone change.

Delete client
  delete-client INDEX
  delete-client i/PHONE_NUMBER

Add pet
  add-pet p/PET_NAME i/OWNER_IDENTIFIER s/SPECIES [b/BREED] r/REQUIREMENTS

List pets
  list-pets [i/OWNER_IDENTIFIER]

Edit pet
  edit-pet p/PET_NAME i/OWNER_IDENTIFIER [n/NEW_PET_NAME] [s/SPECIES] [b/BREED] [r/REQUIREMENTS]

Delete pet
  delete-pet INDEX
  delete-pet p/PET_NAME i/OWNER_IDENTIFIER

Add appointment
  add-appointment i/OWNER_IDENTIFIER p/PET_NAME d/DD-MM-YYYY st/HH:mm et/HH:mm s/SERVICE

List appointments
  list-appointments [d/DD-MM-YYYY]
  Without a date: ongoing and future appointments only.
  With a date: all appointments on that date, including completed appointments.

Delete appointment
  delete-appointment INDEX
  delete-appointment d/DD-MM-YYYY st/HH:mm

Edit appointment
  edit-appointment d/CURRENT_DATE st/CURRENT_START_TIME [i/NEW_OWNER_PHONE] [p/NEW_PET_NAME] [nd/NEW_DATE] [nst/NEW_START_TIME] [et/NEW_END_TIME] [s/NEW_SERVICE]
  edit-appointment INDEX [i/NEW_OWNER_PHONE] [p/NEW_PET_NAME] [nd/NEW_DATE] [nst/NEW_START_TIME] [et/NEW_END_TIME] [s/NEW_SERVICE]

Clear all clients, pets, and appointments
  clear

Exit
  exit

AVAILABLE COMMANDS

Help
  help

Add client
  add-client n/NAME i/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]...

List clients
  list
  list-clients [q/KEYWORD]

Find clients
  find KEYWORD [MORE_KEYWORDS]

Delete client
  delete-client INDEX
  delete-client i/PHONE_NUMBER

Add pet
  add-pet p/PET_NAME i/OWNER_IDENTIFIER s/SPECIES [b/BREED] r/REQUIREMENTS

List pets
  list-pets [i/OWNER_IDENTIFIER]

Delete pet
  delete-pet INDEX
  delete-pet p/PET_NAME i/OWNER_IDENTIFIER

Add appointment
  add-appointment i/OWNER_IDENTIFIER p/PET_NAME d/DD-MM-YYYY st/HH:mm et/HH:mm s/SERVICE

List appointments
  list-appointments

Delete appointment
  delete-appointment INDEX
  delete-appointment d/DD-MM-YYYY t/HH:mm

Clear all clients, pets, and appointments
  clear

Exit
  exit

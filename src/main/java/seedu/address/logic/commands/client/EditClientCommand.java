package seedu.address.logic.commands.client;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_IDENTIFIER;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_NEW_PHONE;
import static seedu.address.logic.parser.client.ClientCliSyntax.PREFIX_TAG;

import java.util.Collections;
import java.util.HashSet;
//@@author anderschow
import java.util.List;
//@@author
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

//@@author anderschow
import seedu.address.commons.core.index.Index;
//@@author
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.client.Address;
import seedu.address.model.client.Client;
import seedu.address.model.client.ClientName;
import seedu.address.model.client.Email;
import seedu.address.model.client.Phone;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing client in the BuBu.
 */
public class EditClientCommand extends Command {

    //@@author anderschow
    public static final String COMMAND_WORD = "edit-client";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the client identified "
            + "by their displayed index or current phone number. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX or " + PREFIX_IDENTIFIER + "CURRENT_PHONE "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_NEW_PHONE + "NEW_PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_IDENTIFIER + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com";

    public static final String MESSAGE_EDIT_CLIENT_SUCCESS = "Edited client: %1$s";
    public static final String MESSAGE_CLIENT_NOT_FOUND = "No saved client has this phone number.";
    //@@author
    //@@author anderschow
    public static final String MESSAGE_NOT_EDITED = "No update supplied. Provide at least one of n/, p/, e/, a/ or t/.";
    //@@author
    //@@author anderschow
    public static final String MESSAGE_DUPLICATE_CLIENT =
            "The phone number and email already belong to saved clients.";
    public static final String MESSAGE_DUPLICATE_PHONE =
            "The phone number already belongs to another client.";
    public static final String MESSAGE_DUPLICATE_EMAIL =
            "The email already belongs to another client.";


    private final Index targetIndex;
    private final Phone identifier;
    //@@author
    private final EditClientDescriptor editClientDescriptor;

    //@@author anderschow
    /**
     * @param identifier current phone number of the client to edit
     * @param editClientDescriptor details to edit the client with
     */
    public EditClientCommand(Phone identifier, EditClientDescriptor editClientDescriptor) {
        requireNonNull(identifier);
        requireNonNull(editClientDescriptor);

        this.targetIndex = null;
        this.identifier = identifier;
        this.editClientDescriptor = new EditClientDescriptor(editClientDescriptor);
    }

    /** Creates a command to edit the client at the given displayed index. */
    public EditClientCommand(Index targetIndex, EditClientDescriptor editClientDescriptor) {
        this.targetIndex = requireNonNull(targetIndex);
        this.identifier = null;
        this.editClientDescriptor = new EditClientDescriptor(requireNonNull(editClientDescriptor));
    }

    @Override
    public Optional<CommandResult.DisplayType> getRequiredDisplayType() {
        return targetIndex == null ? Optional.empty() : Optional.of(CommandResult.DisplayType.CLIENTS);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Client clientToEdit;
        if (targetIndex != null) {
            List<Client> displayedClients = model.getFilteredClientList();
            if (targetIndex.getZeroBased() >= displayedClients.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_CLIENT_DISPLAYED_INDEX);
            }
            clientToEdit = displayedClients.get(targetIndex.getZeroBased());
        } else {
            clientToEdit = model.getBubuBook().getClientList().stream()
                    .filter(client -> client.getPhone().equals(identifier))
                    .findFirst()
                    .orElseThrow(() -> new CommandException(MESSAGE_CLIENT_NOT_FOUND));
        }
        Client editedClient = createEditedClient(clientToEdit, editClientDescriptor);

        ClientConflictCheck.validate(model.getBubuBook().getClientList(), editedClient, clientToEdit);

        model.setClient(clientToEdit, editedClient);
        return new CommandResult(String.format(MESSAGE_EDIT_CLIENT_SUCCESS, Messages.format(editedClient)),
                CommandResult.DisplayType.CLIENTS);
    }
    //@@author

    /**
     * Creates and returns a {@code Client} with the details of {@code clientToEdit}
     * edited with {@code editClientDescriptor}.
     */
    private static Client createEditedClient(Client clientToEdit, EditClientDescriptor editClientDescriptor) {
        //@@author anderschow
        assert clientToEdit != null : "Client lookup must succeed before building the edited client";
        //@@author

        ClientName updatedName = editClientDescriptor.getName().orElse(clientToEdit.getName());
        Phone updatedPhone = editClientDescriptor.getPhone().orElse(clientToEdit.getPhone());
        Email updatedEmail = editClientDescriptor.getEmail().orElse(clientToEdit.getEmail());
        Address updatedAddress = editClientDescriptor.getAddress().orElse(clientToEdit.getAddress());
        Set<Tag> updatedTags = editClientDescriptor.getTags().orElse(clientToEdit.getTags());

        return new Client(updatedName, updatedPhone, updatedEmail, updatedAddress, updatedTags);
    }

    //@@author anderschow
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditClientCommand otherEditCommand)) {
            return false;
        }

        return Objects.equals(targetIndex, otherEditCommand.targetIndex)
                && Objects.equals(identifier, otherEditCommand.identifier)
                && editClientDescriptor.equals(otherEditCommand.editClientDescriptor);
    }

    @Override
    public String toString() {
        if (targetIndex != null) {
            return new ToStringBuilder(this)
                    .add("targetIndex", targetIndex)
                    .add("editClientDescriptor", editClientDescriptor)
                    .toString();
        }
        return new ToStringBuilder(this)
                .add("identifier", identifier)
                .add("editClientDescriptor", editClientDescriptor)
                .toString();
    }
    //@@author

    /**
     * Stores the details to edit the client with. Each non-empty field value will replace the
     * corresponding field value of the client.
     */
    public static class EditClientDescriptor {
        private ClientName name;
        private Phone phone;
        private Email email;
        private Address address;
        private Set<Tag> tags;

        public EditClientDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditClientDescriptor(EditClientDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setAddress(toCopy.address);
            setTags(toCopy.tags);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, address, tags);
        }

        public void setName(ClientName name) {
            this.name = name;
        }

        public Optional<ClientName> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditClientDescriptor otherEditClientDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditClientDescriptor.name)
                    && Objects.equals(phone, otherEditClientDescriptor.phone)
                    && Objects.equals(email, otherEditClientDescriptor.email)
                    && Objects.equals(address, otherEditClientDescriptor.address)
                    && Objects.equals(tags, otherEditClientDescriptor.tags);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("address", address)
                    .add("tags", tags)
                    .toString();
        }
    }
}

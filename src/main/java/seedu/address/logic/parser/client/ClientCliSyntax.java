package seedu.address.logic.parser.client;

import seedu.address.logic.parser.Prefix;

/** CLI prefixes for client commands. */
public class ClientCliSyntax {
    //@@author anderschow
    public static final Prefix PREFIX_QUERY = new Prefix("q/");
    //@@author
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_IDENTIFIER = new Prefix("i/");
    //@@author anderschow
    public static final Prefix PREFIX_NEW_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_PHONE = new Prefix("i/");
    //@@author
    public static final Prefix PREFIX_EMAIL = new Prefix("e/");
    public static final Prefix PREFIX_ADDRESS = new Prefix("a/");
    public static final Prefix PREFIX_TAG = new Prefix("t/");
}

package seedu.address.logic.parser.appointment;

import seedu.address.logic.parser.Prefix;

/**
 * Command-line prefixes used by appointment commands.
 */
public class AppointmentCliSyntax {
    public static final Prefix PREFIX_OWNER_IDENTIFIER = new Prefix("i/");
    public static final Prefix PREFIX_PET_NAME = new Prefix("p/");
    public static final Prefix PREFIX_DATE = new Prefix("d/");
    public static final Prefix PREFIX_START_TIME = new Prefix("st/");
    public static final Prefix PREFIX_END_TIME = new Prefix("et/");
    public static final Prefix PREFIX_SERVICE = new Prefix("s/");
}

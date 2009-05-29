package org.glassfish.enterprise.admin.ncli;

import org.glassfish.api.admin.cli.OptionType;
import org.glassfish.cli.metadata.OptionDesc;

/** Builds the asadmin program options. It is an option for the asadmin program itself. The good
 *  thing is the metadata for program options is no different from that for option of a command.
 *  So, this class builds the OptionDesc for all the options that asadmin supports.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @see org.glassfish.cli.metadata.OptionDesc
 */
final class ProgramOptionBuilder {


    private static OptionDesc HOST_DESC;
    private static OptionDesc PORT_DESC;
    private static OptionDesc USER_DESC;
    private static OptionDesc PASSWORDFILE_DESC;
    private static OptionDesc SECURE_DESC;
    private static OptionDesc INTERACTIVE_DESC;
    private static OptionDesc ECHO_DESC;
    private static OptionDesc TERSE_DESC;

    private ProgramOptionBuilder() {} //disallow

    static synchronized OptionDesc buildHostDesc() {
        if (HOST_DESC != null)
            return HOST_DESC;
        HOST_DESC = new OptionDesc();
        HOST_DESC.setName("host");
        HOST_DESC.setSymbol("h");
        HOST_DESC.setDefaultValue("localhost");
        HOST_DESC.setRepeats("FALSE");
        HOST_DESC.setRequired("FALSE");
        HOST_DESC.setType(OptionType.STRING.name());

        return HOST_DESC;
    }
    static Option buildHost(String host) {
        return new Option(buildHostDesc(), host);
    }
    static synchronized OptionDesc buildPortDesc() {
        if (PORT_DESC != null)
            return PORT_DESC;
        PORT_DESC = new OptionDesc();
        PORT_DESC.setName("port");
        PORT_DESC.setSymbol("p");
        PORT_DESC.setDefaultValue("4848");
        PORT_DESC.setRepeats("FALSE");
        PORT_DESC.setRequired("FALSE");
        PORT_DESC.setType(OptionType.STRING.name());

        return PORT_DESC;
    }
    static Option buildPort(String port) {
        return new Option(buildPortDesc(), port);
    }
    static synchronized OptionDesc buildUserDesc() {
        if (USER_DESC != null)
            return USER_DESC;
        USER_DESC = new OptionDesc();
        USER_DESC.setName("user");
        USER_DESC.setSymbol("u");
        USER_DESC.setDefaultValue("anonymous");
        USER_DESC.setRepeats("FALSE");
        USER_DESC.setRequired("FALSE");
        USER_DESC.setType(OptionType.STRING.name());

        return USER_DESC;
    }
    static Option buildUser(String user) {
        return new Option(buildHostDesc(), user);
    }
    static synchronized OptionDesc buildPasswordfileDesc() {
        if (PASSWORDFILE_DESC != null)
            return PASSWORDFILE_DESC;
        PASSWORDFILE_DESC = new OptionDesc();
        PASSWORDFILE_DESC.setName("passwordfile");
        PASSWORDFILE_DESC.setSymbol("W");
        PASSWORDFILE_DESC.setRepeats("FALSE");
        PASSWORDFILE_DESC.setRequired("FALSE");
        PASSWORDFILE_DESC.setType(OptionType.FILE_PATH.name());

        return PASSWORDFILE_DESC;
    }
    static Option buildPasswordFile(String path) {
        return new Option(buildPasswordfileDesc(), path);
    }
    static synchronized OptionDesc buildSecureDesc() {
        if (SECURE_DESC != null)
            return SECURE_DESC;
        SECURE_DESC = new OptionDesc();
        SECURE_DESC.setName("secure");
        SECURE_DESC.setSymbol("s");
        SECURE_DESC.setDefaultValue("FALSE");
        SECURE_DESC.setRepeats("FALSE");
        SECURE_DESC.setRequired("FALSE");
        SECURE_DESC.setType(OptionType.BOOLEAN.name());

        return SECURE_DESC;
    }
    static Option buildSecure(String secure) {
        return new Option(buildSecureDesc(), secure);
    }
    static synchronized OptionDesc buildInteractiveDesc() {
        if (INTERACTIVE_DESC != null)
            return INTERACTIVE_DESC;
        INTERACTIVE_DESC = new OptionDesc();
        INTERACTIVE_DESC.setName("interactive");
        INTERACTIVE_DESC.setSymbol("i");
        INTERACTIVE_DESC.setDefaultValue("FALSE");
        INTERACTIVE_DESC.setRepeats("FALSE");
        INTERACTIVE_DESC.setRequired("FALSE");
        INTERACTIVE_DESC.setType(OptionType.BOOLEAN.name());

        return INTERACTIVE_DESC;
    }
    static Option buildInteractive(String interactive) {
        return new Option(buildHostDesc(), interactive);
    }
    static synchronized OptionDesc buildEchoDesc() {
        if (ECHO_DESC != null)
            return ECHO_DESC;
        ECHO_DESC = new OptionDesc();
        ECHO_DESC.setName("echo");
        ECHO_DESC.setSymbol("e");
        ECHO_DESC.setDefaultValue("FALSE");
        ECHO_DESC.setRepeats("FALSE");
        ECHO_DESC.setRequired("FALSE");
        ECHO_DESC.setType(OptionType.BOOLEAN.name());

        return ECHO_DESC;
    }
    static Option buildEcho(String echo) {
        return new Option(buildEchoDesc(), echo);
    }
    static synchronized OptionDesc buildTerseDesc() {
        if (TERSE_DESC != null)
            return TERSE_DESC;
        TERSE_DESC = new OptionDesc();
        TERSE_DESC.setName("terse");
        TERSE_DESC.setSymbol("t");
        TERSE_DESC.setDefaultValue("FALSE");
        TERSE_DESC.setRepeats("FALSE");
        TERSE_DESC.setRequired("FALSE");
        TERSE_DESC.setType(OptionType.BOOLEAN.name());

        return TERSE_DESC;
    }
    static Option buildTerse(String terse) {
        return new Option(buildTerseDesc(), terse);
    }
}

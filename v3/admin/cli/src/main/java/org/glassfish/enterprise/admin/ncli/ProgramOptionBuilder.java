package org.glassfish.enterprise.admin.ncli;

import org.glassfish.api.admin.cli.OptionType;
import org.glassfish.enterprise.admin.ncli.metadata.OptionDesc;
import static org.glassfish.enterprise.admin.ncli.Constants.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Builds the asadmin program options. A program option is an option for the asadmin program itself. The good
 *  thing is the metadata for program options is no different from that for options of a command.
 *  So, this class builds the OptionDesc for all the options that asadmin supports.
 *
 *  This is modeled as a Singleton class.  That instance of this class is immutable.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @see org.glassfish.enterprise.admin.ncli.metadata.OptionDesc
 */
public final class ProgramOptionBuilder {

    private final Set<OptionDesc> som; //set of metadata

    private final OptionDesc hostDesc;
    private final OptionDesc portDesc;
    private final OptionDesc userDesc;
    private final OptionDesc passwordDesc;
    private final OptionDesc pwfileDesc;
    private final OptionDesc secureDesc;
    private final OptionDesc interactiveDesc;
    private final OptionDesc echoDesc;
    private final OptionDesc terseDesc;
    
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String PASSWORDFILE = "passwordfile";
    public static final String SECURE = "secure";
    public static final String INTERACTIVE = "interactive";
    public static final String ECHO = "echo";
    public static final String TERSE = "terse";

    public static final char HOST_SYMBOL = 'H';
    public static final char PORT_SYMBOL = 'p';
    public static final char USER_SYMBOL = 'u';
    public static final char PASSWORD_SYMBOL = 'w';
    public static final char PASSWORDFILE_SYMBOL = 'W';
    public static final char SECURE_SYMBOL = 's';
    public static final char INTERACTIVE_SYMBOL = 'I';
    public static final char ECHO_SYMBOL = 'e';
    public static final char TERSE_SYMBOL = 't';

    private final static ProgramOptionBuilder INSTANCE = new ProgramOptionBuilder();

    private ProgramOptionBuilder() {
        Set<OptionDesc> t = new HashSet<OptionDesc>();
        t.add(hostDesc = buildHostDesc());
        t.add(portDesc = buildPortDesc());
        t.add(userDesc = buildUserDesc());
        t.add(passwordDesc = buildPasswordDesc());
        t.add(pwfileDesc = buildPasswordfileDesc());
        t.add(secureDesc = buildSecureDesc());
        t.add(interactiveDesc = buildInteractiveDesc());
        t.add(echoDesc = buildEchoDesc());
        t.add(terseDesc = buildTerseDesc());
        som = Collections.unmodifiableSet(t);
    }

    public static ProgramOptionBuilder getInstance() {
        return INSTANCE;
    }

    public Set<OptionDesc> getAllOptionMetadata() {
        return som; //this is unmodifiable
    }

    public OptionDesc getHostDesc() {
        return hostDesc;
    }
    public OptionDesc getPortDesc() {
        return portDesc;
    }

    public OptionDesc getUserDesc() {
        return userDesc;
    }

    public OptionDesc getPasswordDesc() {
        return passwordDesc;
    }

    public OptionDesc getPwfileDesc() {
        return pwfileDesc;
    }

    public OptionDesc getSecureDesc() {
        return secureDesc;
    }

    public OptionDesc getInteractiveDesc() {
        return interactiveDesc;
    }

    public OptionDesc getEchoDesc() {
        return echoDesc;
    }

    public OptionDesc getTerseDesc() {
        return terseDesc;
    }


    // ALL PRIVATE ....

    private static OptionDesc buildHostDesc() {
        OptionDesc h = new OptionDesc();
        h.setName(HOST);
        h.setSymbol(Character.toString(HOST_SYMBOL));
        h.setDefaultValue(Constants.DEFAULT_HOST);
        h.setRepeats("FALSE");
        h.setRequired("FALSE");
        h.setType(OptionType.STRING.name());

        return h;
    }

    private static OptionDesc buildPortDesc() {
        OptionDesc p = new OptionDesc();
        p.setName(PORT);
        p.setSymbol(Character.toString(PORT_SYMBOL));
        p.setDefaultValue(Constants.DEFAULT_PORT + "");
        p.setRepeats("FALSE");
        p.setRequired("FALSE");
        p.setType(OptionType.STRING.name());

        return p;
    }

    private static OptionDesc buildUserDesc() {
        OptionDesc u = new OptionDesc();
        u.setName(USER);
        u.setSymbol(Character.toString(USER_SYMBOL));
        u.setDefaultValue(Constants.DEFAULT_USER);
        u.setRepeats("FALSE");
        u.setRequired("FALSE");
        u.setType(OptionType.STRING.name());

        return u;
    }

    private static OptionDesc buildPasswordDesc() {
        OptionDesc pwd = new OptionDesc();
        pwd.setName(PASSWORD);
        pwd.setSymbol(Character.toString(PASSWORD_SYMBOL));
        pwd.setRepeats("FALSE");
        pwd.setRequired("FALSE");
        pwd.setType(OptionType.PASSWORD.name());

        return pwd;
    }

    private OptionDesc buildPasswordfileDesc() {
        OptionDesc pf = new OptionDesc();
        pf.setName(PASSWORDFILE);
        pf.setSymbol(Character.toString(PASSWORDFILE_SYMBOL));
        pf.setRepeats("FALSE");
        pf.setRequired("FALSE");
        pf.setType(OptionType.FILE_PATH.name());

        return pf;
    }

    private OptionDesc buildSecureDesc() {
        OptionDesc s = new OptionDesc();
        s.setName(SECURE);
        s.setSymbol(Character.toString(SECURE_SYMBOL));
        s.setDefaultValue(DEFAULT_SECURE);
        s.setRepeats("FALSE");
        s.setRequired("FALSE");
        s.setType(OptionType.BOOLEAN.name());

        return s;
    }

    private OptionDesc buildInteractiveDesc() {
        OptionDesc i = new OptionDesc();
        i.setName(INTERACTIVE);
        i.setSymbol(Character.toString(INTERACTIVE_SYMBOL));
        i.setDefaultValue(DEFAULT_INTERACTIVE);
        i.setRepeats("FALSE");
        i.setRequired("FALSE");
        i.setType(OptionType.BOOLEAN.name());

        return i;
    }

    private OptionDesc buildEchoDesc() {
        OptionDesc e = new OptionDesc();
        e.setName(ECHO);
        e.setSymbol(Character.toString(ECHO_SYMBOL));
        e.setDefaultValue(DEFAULT_ECHO);
        e.setRepeats("FALSE");
        e.setRequired("FALSE");
        e.setType(OptionType.BOOLEAN.name());

        return e;
    }

    private OptionDesc buildTerseDesc() {
        OptionDesc t = new OptionDesc();
        t.setName(TERSE);
        t.setSymbol(Character.toString(TERSE_SYMBOL));
        t.setDefaultValue(DEFAULT_TERSE);
        t.setRepeats("FALSE");
        t.setRequired("FALSE");
        t.setType(OptionType.BOOLEAN.name());

        return t;
    }
}
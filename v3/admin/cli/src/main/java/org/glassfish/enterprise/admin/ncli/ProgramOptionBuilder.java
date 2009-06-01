package org.glassfish.enterprise.admin.ncli;

import org.glassfish.api.admin.cli.OptionType;
import org.glassfish.cli.metadata.OptionDesc;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/** Builds the asadmin program options. A program option is an option for the asadmin program itself. The good
 *  thing is the metadata for program options is no different from that for options of a command.
 *  So, this class builds the OptionDesc for all the options that asadmin supports.
 *
 *  This is modeled as a Singleton class.  Instances of this class are immutable.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @see org.glassfish.cli.metadata.OptionDesc
 */
public final class ProgramOptionBuilder {

    private final static ProgramOptionBuilder INSTANCE = new ProgramOptionBuilder();
    private final Set<OptionDesc> som; //set of metadata

    private final OptionDesc hostDesc;
    private final OptionDesc portDesc;
    private final OptionDesc userDesc;
    private final OptionDesc pwfileDesc;
    private final OptionDesc secureDesc;
    private final OptionDesc interDesc;
    private final OptionDesc echoDesc;
    private final OptionDesc terseDesc;

    private ProgramOptionBuilder() {
        Set<OptionDesc> t = new HashSet<OptionDesc>();
        t.add(hostDesc = buildHostDesc());
        t.add(portDesc = buildPortDesc());
        t.add(userDesc = buildUserDesc());
        t.add(pwfileDesc = buildPasswordfileDesc());
        t.add(secureDesc = buildSecureDesc());
        t.add(interDesc = buildInteractiveDesc());
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

    public OptionDesc getPwfileDesc() {
        return pwfileDesc;
    }

    public OptionDesc getSecureDesc() {
        return secureDesc;
    }

    public OptionDesc getInterDesc() {
        return interDesc;
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
        h.setName("host");
        h.setSymbol("h");
        h.setDefaultValue("localhost");
        h.setRepeats("FALSE");
        h.setRequired("FALSE");
        h.setType(OptionType.STRING.name());

        return h;
    }

    private static OptionDesc buildPortDesc() {
        OptionDesc p = new OptionDesc();
        p.setName("port");
        p.setSymbol("p");
        p.setDefaultValue("4848");
        p.setRepeats("FALSE");
        p.setRequired("FALSE");
        p.setType(OptionType.STRING.name());

        return p;
    }

    private static OptionDesc buildUserDesc() {
        OptionDesc u = new OptionDesc();
        u.setName("user");
        u.setSymbol("u");
        u.setDefaultValue("anonymous");
        u.setRepeats("FALSE");
        u.setRequired("FALSE");
        u.setType(OptionType.STRING.name());

        return u;
    }

    private OptionDesc buildPasswordfileDesc() {
        OptionDesc pf = new OptionDesc();
        pf.setName("passwordfile");
        pf.setSymbol("W");
        pf.setRepeats("FALSE");
        pf.setRequired("FALSE");
        pf.setType(OptionType.FILE_PATH.name());

        return pf;
    }

    private OptionDesc buildSecureDesc() {
        OptionDesc s = new OptionDesc();
        s.setName("secure");
        s.setSymbol("s");
        s.setDefaultValue("FALSE");
        s.setRepeats("FALSE");
        s.setRequired("FALSE");
        s.setType(OptionType.BOOLEAN.name());

        return s;
    }

    private OptionDesc buildInteractiveDesc() {
        OptionDesc i = new OptionDesc();
        i.setName("interactive");
        i.setSymbol("i");
        i.setDefaultValue("FALSE");
        i.setRepeats("FALSE");
        i.setRequired("FALSE");
        i.setType(OptionType.BOOLEAN.name());

        return i;
    }

    private OptionDesc buildEchoDesc() {
        OptionDesc e = new OptionDesc();
        e.setName("echo");
        e.setSymbol("e");
        e.setDefaultValue("FALSE");
        e.setRepeats("FALSE");
        e.setRequired("FALSE");
        e.setType(OptionType.BOOLEAN.name());

        return e;
    }

    private OptionDesc buildTerseDesc() {
        OptionDesc t = new OptionDesc();
        t.setName("terse");
        t.setSymbol("t");
        t.setDefaultValue("FALSE");
        t.setRepeats("FALSE");
        t.setRequired("FALSE");
        t.setType(OptionType.BOOLEAN.name());

        return t;
    }
}

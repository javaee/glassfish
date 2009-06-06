package org.glassfish.enterprise.admin.ncli;

import org.glassfish.enterprise.admin.ncli.comm.TargetServer;
import org.glassfish.cli.metadata.OptionDesc;
import static org.glassfish.enterprise.admin.ncli.ProgramOptionBuilder.*;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/** A class to hold the result of first pass of the command line parsing. At the end of this <i> first pass </i>,
 *  parser should either detect a parsing error based on client-only knowledge or gather all the information relevant
 *  to second pass. A successful instance of this class indicates that there are no first pass syntax/parsing
 *  errors. Essentially, at the end of a successful (error-free) first pass, system knows about the command name,
 *  the target server, asadmin program options used by a particular invocation and commands arguments
 *  (option names, their values and operands, if any). This class capatures
 *  that context, crucial for later execution. Note that the heavy lifting is done in the parser.
 *  <p>
 *  Instances of this class are immutable. It is modeled as a <i> Value Class </i>.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
final class FirstPassResult {

    private final ProgramOptionBuilder POB = ProgramOptionBuilder.getInstance();    
    private final String cmdName;
    private final TargetServer ts;
    private final Set<Option> programOptions;
    private final String[] cmdArgs;

    FirstPassResult(String cmdName, Map<String, String> poPair, String[] cmdArgs) {
        this.cmdName = cmdName;
        this.programOptions = initializeAllProgramOptions(poPair);
        this.ts             = initializeTargetServer();
        this.cmdArgs        = new String[cmdArgs.length];
        System.arraycopy(cmdArgs, 0, this.cmdArgs, 0, cmdArgs.length);
    }

    String getCommandName() {
        return cmdName;
    }

    TargetServer getTargetServer() {
        return ts;
    }

    String[] getCommandArguments() {
        return cmdArgs;
    }

    public Set<Option> getProgramOptions() {
        return programOptions;
    }
    
    // ALL Private ...

    private Set<Option> initializeAllProgramOptions(Map<String, String> explicitOpts) {
        Set<OptionDesc> validOnes   = POB.getAllOptionMetadata();
        Set<Option> options         = new HashSet<Option>();
        Set<String> names = explicitOpts.keySet();
        for (String name : names) {
            //this MUST be a valid program option; so assert it
            assert ParseUtilities.getOptionDescForName(name, validOnes) != null : "Programming Error: uncaught invalid program option: " + name;
            OptionDesc metadata = ParseUtilities.getMetadataFor(name, validOnes);
            assert metadata != null : "Programming Error: Program option metadata should never be null for: " + name;
            boolean added = options.add(new Option(metadata, explicitOpts.get(name)));
            assert added : "Programming Error: This option could not be added to the set: " + name;
        }
        //add the rest as defaults
        Set<OptionDesc> defaultThem = ParseUtilities.getAllOptionMetadataExcluding(validOnes, names);
        for (OptionDesc od : defaultThem) {
            Option defOpt = new Option(od, null); // make the value null for those where default prevails
            options.add(defOpt);
        }
        return Collections.unmodifiableSet(options);
    }

    private TargetServer initializeTargetServer() {
        //this method assumes that basic validation is already done.
        String host = null;
        int port = -1;
        String user = null;
        String password = null;
        boolean secure = false;
        for (Option po : programOptions) {
            String name = po.getName();
            if (HOST.equals(name))
                host = po.getEffectiveValue();
            if (PORT.equals(name))
                port = Integer.parseInt(po.getEffectiveValue()); // no check
            if (USER.equals(name))
                user = po.getEffectiveValue();
            if (PASSWORD.equals(name))
                password = po.getEffectiveValue();
            if (SECURE.equals(name))
                secure = Boolean.valueOf(po.getEffectiveValue());
        }
        return new TargetServer(host, port, user, password, secure);
    }
}

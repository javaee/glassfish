package org.glassfish.enterprise.admin.ncli;

import org.glassfish.enterprise.admin.ncli.comm.TargetServer;

/** A class to hold the result of first pass of the command line parsing. At the end of this <i> first pass </i>,
 *  parser should either detect a parsing error based on client-only knowledge or gather all the information relevant
 *  to second pass. A successful instance of this class indicates that there are no first pass syntax/parsing
 *  errors. Essentially, at the end of a successful (error-free) first pass, system knows about the command name,
 *  the target server and commands arguments (option names, their values and operands, if any). This class capatures
 *  that context, crucial for later execution. Note that the heavy lifting is done in the parser.
 *  <p>
 *  Instances of this class are immutable. It is modeled as a <i> Value Class </i>.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
final class FirstPassResult {

    private final String cmdName;
    private final TargetServer ts;
    private final String[] cmdArgs;

    FirstPassResult(String cmdName, TargetServer ts, String[] cmdArgs) {
        if (cmdName == null || ts == null)
            throw new IllegalArgumentException("null cmd or target server");

        this.cmdName = cmdName;
        this.ts      = ts;
        this.cmdArgs = cmdArgs;
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
}

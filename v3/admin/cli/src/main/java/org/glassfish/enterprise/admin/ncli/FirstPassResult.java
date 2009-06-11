/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *   Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 *   The contents of this file are subject to the terms of either the GNU
 *   General Public License Version 2 only ("GPL") or the Common Development
 *   and Distribution License("CDDL") (collectively, the "License").  You
 *   may not use this file except in compliance with the License. You can obtain
 *   a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *   or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *   language governing permissions and limitations under the License.
 *
 *   When distributing the software, include this License Header Notice in each
 *   file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *   Sun designates this particular file as subject to the "Classpath" exception
 *   as provided by Sun in the GPL Version 2 section of the License file that
 *   accompanied this code.  If applicable, add the following below the License
 *   Header, with the fields enclosed by brackets [] replaced by your own
 *   identifying information: "Portions Copyrighted [year]
 *   [name of copyright owner]"
 *
 *   Contributor(s):
 *
 *   If you wish your version of this file to be governed by only the CDDL or
 *   only the GPL Version 2, indicate your decision by adding "[Contributor]
 *   elects to include this software in this distribution under the [CDDL or GPL
 *   Version 2] license."  If you don't indicate a single choice of license, a
 *   recipient has the option to distribute your version of this file under
 *   either the CDDL, the GPL Version 2 or to extend the choice of license to
 *   its licensees as provided above.  However, if you add GPL Version 2 code
 *   and therefore, elected the GPL Version 2 license, then the option applies
 *   only if the new code is made subject to such option by the copyright
 *   holder.
 */

package org.glassfish.enterprise.admin.ncli;

import org.glassfish.enterprise.admin.ncli.metadata.OptionDesc;
import static org.glassfish.enterprise.admin.ncli.ProgramOptionBuilder.*;
import org.glassfish.enterprise.admin.ncli.comm.TargetServer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private final boolean usesDeprecatedSyntax;

    /** Creates an instance of this class. This instance indicates a parsing result for a command
     *  that follows the new syntax. See the new syntax <a href="https://glassfish.dev.java.net/nonav/v3/admin/planning/j109/admin-cli.html#asadminoptions">
     *  here </a>. Call to this constructor should generated a <b> deprecation warning </b>.
     *
     * @param cmdName String representing command name, may not be null
     * @param poPair A Map<String, String> between option names and their values
     * @param cmdArgs Unresolved command options and operands as string array
     */
    FirstPassResult(String cmdName, Map<String, String> poPair, String[] cmdArgs) {
        this(cmdName, poPair, cmdArgs, false);
    }

    /** Creates an instance of this class. This instance indicates a parsing result for a command
     *  that might follow the old syntax. See the new syntax <a href="https://glassfish.dev.java.net/nonav/v3/admin/planning/j109/admin-cli.html#asadminoptions">
     *  here </a>.
     * @param cmdName String representing command name, may not be null
     * @param poPair A Map<String, String> between option names and their values
     * @param cmdArgs Unresolved command options and operands as string array
     * @param usesDeprecatedSyntax indicates whether this parsing results in command following the old syntax
     */
    public FirstPassResult(String cmdName, Map<String, String> poPair, String[] cmdArgs, boolean usesDeprecatedSyntax) {
        if (cmdName == null || poPair == null || cmdArgs == null)
            throw new IllegalArgumentException("null_arg");
        this.cmdName = cmdName;
        this.programOptions = initializeAllProgramOptions(poPair);
        this.ts             = initializeTargetServer();
        this.cmdArgs        = new String[cmdArgs.length];
        System.arraycopy(cmdArgs, 0, this.cmdArgs, 0, cmdArgs.length);
        this.usesDeprecatedSyntax = usesDeprecatedSyntax;
    }

    /** Returns the name of the command to be run.
     *
     * @return String representing the name. Never returns null
     */
    String getCommandName() {
        return cmdName;
    }

    /** Returns an instance of TargetServer representing the target server.
     *
     * @return TargetServer instance. The returned instance is immutable.
     */
    TargetServer getTargetServer() {
        return ts;
    }

    /** Returns a copy of command arguments. These may contain command options (resolved as name-value pairs to an extent possible)
     *  like "name=value" and operands. Returned array is a copy and any changes to the copy made by the caller won't
     *  affect the state of this instance. The copy ensures the immutability of this instance.
     *
     * @return a String array. Never returns a null
     */
    String[] getCommandArguments() {
        int length = cmdArgs.length;
        String[] copy = new String[length];
        System.arraycopy(cmdArgs, 0, copy, 0, length);
        return copy;
    }

    /** Returns an <i> unmodifiable view </i> of the program options as a set. This class makes sure that asadmin program
     *  options are initialized to values given on the command line, defaulting the rest of them based on metadata.
     *  This is one of the responsibilities of this class and parser's first pass.
     *  <p>
     *  Since instances of the Option class are themselves immutable, this class is made immutable.
     * 
     * @return an unmodifiable set of asadmin program options.
     * @see ProgramOptionBuilder
     * @see Parser#firstPass()
     */
    Set<Option> getProgramOptions() {
        return programOptions;
    }

    /** Returns whether this result implies the command using the deprecated syntax.
     *
     * @return true if command line uses deprecated syntax, false otherwise
     */
    boolean usesDeprecatedSyntax() {
        return usesDeprecatedSyntax;
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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.admin.ncli;

import org.glassfish.admin.ncli.metadata.CommandDesc;
import org.glassfish.admin.ncli.comm.TargetServer;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/** <h3> This class is not yet final. Please do not spend time. </h3>
 *  A thread-safe cache of command metadata. The idea is that each server that asadmin client knows about can
 *  be implementing a different set of commands at any given point in time. It is also possible that a given server
 *  implements different commands are different points in time. This class asynchronously builds a cache of
 *  command metadata for all servers it knows about.
 * <p>
 *  This class is designed to be thread-safe. It also provides an explicit API to modify the cache synchronously. But
 *  its main use is in terms of asynchronously building cache of command metadata.
 *  <p>
 *  This class is package private.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 September 2009
 */
final class CommandMetadataCache {
    /** A mapping betweeen a server and descriptions of commands available on that server. */
    private final Map<TargetServer, Set<CommandDesc>> s2d;

    /** The default constructor that initializes the concurrent data structures used internally.
     *
     */
    CommandMetadataCache() {
        //Set<CommandDesc> descs = new ConcurrentSkipListSet<CommandDesc>();
        s2d = new ConcurrentHashMap<TargetServer, Set<CommandDesc>>();
    }


    /** Returns the metadata for a given command from given target server from the cache.
     *
     * @param cmdName String representing the command name possibly implemented on a given server
     * @param ts a TargetServer instance that represents the server where the command might be implemented
     * @return an instance of CommandDesc if cache has it, null otherwise. A returned null should be interpreted
     * as some kind of problem or a cache-miss. The calling code should then communicate with the target server
     * explicitly to get the metadata for the given command.
     * <p>
     * This method also acts as a trigger to build the cache in the background.
     * @see #put(String,TargetServer, CommandDesc)
     */
    CommandDesc get(String cmdName, TargetServer ts) {
        Set<CommandDesc> scd = s2d.get(ts);
        if (scd == null)
            return null;
        for (CommandDesc aScd : scd) {
            CommandDesc cd = aScd;
            if (cd.getName().equals(cmdName)) {
                buildAsync(ts);
                return cd;
            }
        }
        buildAsync(ts);
        return null;
    }

    /** Explicitly puts the given metadata for the given command and target server. This is a hook for an external
     *  effort to get the metadata and put into the cache. This method should be used sparingly and its use should
     *  reduce as the cache builds up.
     * 
     * @param cmdName String representing name of the command, may not be null
     * @param ts  TargetServer instance indicating the target server
     * @param desc Metadata of the given command
     */
    void put(String cmdName, TargetServer ts, CommandDesc desc) {
        Set<CommandDesc> scd = s2d.get(ts);
        if (scd == null) {
            // XXX - can't use following because OptionDesc isn't Comparable
            // XXX - need synchronization in this class
            //scd = new ConcurrentSkipListSet<CommandDesc>();
            scd = new HashSet<CommandDesc>();
            s2d.put(ts, scd);
        }
        scd.add(desc);
    }

    // ALL Private ...

    /** This method is the essence of this class. It builds the command desc for the target server asynchronously.
     * @param ts identifies the target server that implements the commands
     */
    private void buildAsync(TargetServer ts) {
        //mutates s2d
    }

}

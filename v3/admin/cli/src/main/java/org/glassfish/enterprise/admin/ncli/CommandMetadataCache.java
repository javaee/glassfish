package org.glassfish.enterprise.admin.ncli;

import org.glassfish.enterprise.admin.ncli.metadata.CommandDesc;
import org.glassfish.enterprise.admin.ncli.comm.TargetServer;

import java.util.Map;
import java.util.Set;
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
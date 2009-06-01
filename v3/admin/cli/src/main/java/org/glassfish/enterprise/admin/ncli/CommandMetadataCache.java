package org.glassfish.enterprise.admin.ncli;

import org.glassfish.cli.metadata.CommandDesc;
import org.glassfish.enterprise.admin.ncli.comm.TargetServer;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ConcurrentHashMap;

/** A thread-safe cache of command metadata. The idea is that each server that asadmin client knows about can
 *  be implementing a different set of commands at any given point in time. It is also possible that a given server
 *  implements different commands are different points in time. This class asynchronously builds a cache of
 *  command metadata for all servers it knows about.
 * <p>
 *  This class is designed to be thread-safe.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 September 2009
 */
final class CommandMetadataCache {
    /** A mapping betweeen a server and descriptions of commands available on that server. */
    private final Map<TargetServer, Set<CommandDesc>> s2d;

    CommandMetadataCache() {
        Set<CommandDesc> descs = new ConcurrentSkipListSet<CommandDesc>();
        s2d = new ConcurrentHashMap<TargetServer, Set<CommandDesc>>();
    }


    CommandDesc get(String cmdName, TargetServer ts) {
        Set<CommandDesc> scd = s2d.get(ts);
        Iterator<CommandDesc> it = scd.iterator();
        while(it.hasNext()) {
            CommandDesc cd = it.next();
            if (cd.getName().equals(cmdName)) {
                return cd;
            }
        }
        buildAsync(ts);
        return null;
    }

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
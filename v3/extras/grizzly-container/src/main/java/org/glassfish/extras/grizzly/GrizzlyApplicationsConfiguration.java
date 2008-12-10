package org.glassfish.extras.grizzly;

import org.glassfish.api.deployment.archive.ReadableArchive;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Dec 2, 2008
 * Time: 4:42:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrizzlyApplicationsConfiguration {


    GrizzlyApplicationsConfiguration(ReadableArchive source) {

    }

    public Map<String, String> getTuples() {
        // we need to read this from grizzly.xml from the source readable archive passed above
        Map entries = new HashMap<String, String>();
        entries.put("/cometd", "com.sun.grizzly.cometd.standalone.CometdAdapter");
        return entries;
    }
}

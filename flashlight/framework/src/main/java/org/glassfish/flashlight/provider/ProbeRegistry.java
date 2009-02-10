package org.glassfish.flashlight.provider;

import org.glassfish.flashlight.provider.Probe;
import org.glassfish.flashlight.impl.core.*;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mahesh Kannan
 *         Date: Jul 20, 2008
 */
@Service
@Scoped(Singleton.class)
public class ProbeRegistry {

    private static ProbeRegistry _me = new ProbeRegistry();

    private ConcurrentHashMap<Integer, Probe> probeMap =
                new ConcurrentHashMap<Integer, Probe>();
    private ConcurrentHashMap<String, Probe> probeDesc2ProbeMap =
                new ConcurrentHashMap<String, Probe>();

    public static ProbeRegistry getInstance() {
        return _me;
    }

    public void registerProbe(Probe probe) {
        probeMap.put(probe.getId(), probe);
        probeDesc2ProbeMap.put(probe.getProbeStr(), probe);
        //System.out.println("[FL]Registered probe : " + probe.getProbeStr());
    }

    public void unregisterProbe(Probe probe) {
        probeMap.remove(probe.getId());
    }

    public void unregisterProbe(int id) {
        probeMap.remove(id);
    }

    public Probe getProbe(int id) {
        return probeMap.get(id);
    }

    public Probe getProbe(String probeStr) {
        //System.out.println("[FL]Get probe : " + probeStr);
        return probeDesc2ProbeMap.get(probeStr);
    }

    public static Probe getProbeById(int id) {
        return _me.getProbe(id);
    }
}

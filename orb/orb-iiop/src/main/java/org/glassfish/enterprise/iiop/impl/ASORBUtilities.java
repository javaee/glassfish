package org.glassfish.enterprise.iiop.impl;

import com.sun.logging.LogDomains;
import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.SocketInfo;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orbutil.ORBConstants;
import com.sun.corba.ee.spi.orbutil.misc.ORBClassLoader;
import com.sun.enterprise.config.serverbeans.Cluster;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.io.PrintStream;

import org.omg.CORBA.ORBPackage.InvalidName;

/**
 * @author Harold Carr
 */
public class ASORBUtilities {
    private static Logger _logger = null;

    static {
        _logger = LogDomains.getLogger(ASORBUtilities.class, LogDomains.CORBA_LOGGER);
    }

    private static final String GMS_CLASS =
            "com.sun.enterprise.ee.ejb.iiop.IiopFolbGmsClient";

    private static boolean gisInitialized = false;

    public static synchronized void initGIS(org.omg.CORBA.ORB orb) {
        GroupInfoService gis = null;
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "initGIS->: " + gisInitialized);
            }
            if (gisInitialized) {
                return;
            }
            if (isGMSAvailableAndClusterHeartbeatEnabled()) {
                gis = initGISUsesGMS();
            } else {
                gis = initGISUsesAdmin();
            }
            if (gis == null) {
                return;
            }
            try {
                ((ORB) orb).register_initial_reference(
                        ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE,
                        (org.omg.CORBA.Object) gis);
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE,
                            ".initGIS: naming registration complete: "
                                    + gis);
                }
                gisInitialized = true;

                if (_logger.isLoggable(Level.FINE)) {
                    gis = (GroupInfoService)
                            ((ORB) orb).resolve_initial_references(
                                    ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE);
                    List<ClusterInstanceInfo> lcii =
                            gis.getClusterInstanceInfo(null);
                    _logger.log(Level.FINE,
                            "Results from getClusterInstanceInfo:");
                    if (lcii != null) {
                        for (ClusterInstanceInfo cii : lcii) {
                            _logger.log(Level.INFO, toString(cii));
                        }
                    }
                }

            } catch (InvalidName e) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.SEVERE,
                            ".initGIS: registering GIS failed: " + e);
                }
            }
        } finally {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                        "initGIS<-: " + gisInitialized + " " + gis);
            }
        }
    }

    private static GroupInfoService initGISUsesAdmin() {
        GroupInfoService result = null;
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "initGISUsesAdmin->:");
            }
            result = new GroupInfoServiceImplForJNLP();
            return result;
        } finally {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "initGISUsesAdmin<-: " + result);
            }
        }
    }

    private static GroupInfoService initGISUsesGMS() {
        GroupInfoService result = null;
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "initGISUsesGMS->:");
            }

            Class clazz = loadClass(GMS_CLASS);

            if (clazz == null) {
                _logger.log(Level.SEVERE,
                        ".initGISUsesGMS: GMS initialization failure: class not found: " + GMS_CLASS);
            }

            result = (GroupInfoService) newInstance(clazz);

            if (result == null) {
                _logger.log(Level.SEVERE, ".initGISUsesGMS: GMS initialization failure: cannot instantiate: " + GMS_CLASS);
            }

            return result;

        } finally {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "initGISUsesGMS<-: " + result);
            }
        }
    }

    public static boolean isGMSAvailableAndClusterHeartbeatEnabled() {
        Cluster cluster = null;
        boolean result =
                (loadClass("com.sun.enterprise.ee.cms.core.GMSFactory") != null)
                        && (cluster = getCluster()) != null
                        && isClusterHeartbeatEnabled(cluster);
        return result;
    }

    public static Class loadClass(String classname) {
        Class result = null;
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ".loadClass->: " + classname);
            }
            result = ORBClassLoader.loadClass(classname);
        } catch (ClassNotFoundException e) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ".loadClass: " + classname + " " + e);
            }
        } finally {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                        ".loadClass<-: " + classname + " " + result);
            }
        }
        return result;
    }

    public static <T> T  newInstance(Class<T> clazz) {
        T result = null;
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ".newInstance->: " + clazz);
            }
            result = clazz.newInstance();
        } catch (InstantiationException e) {
            _logger.log(Level.WARNING, ".newInstance: " + clazz + " " + e);
        } catch (IllegalAccessException e) {
            _logger.log(Level.WARNING, ".newInstance: " + clazz + " " + e);
        } finally {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ".newInstance<-: " + clazz + " " + result);
            }
        }
        return result;
    }

    public static Cluster getCluster() {

        Cluster result = null;
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ".getCluster->:");
            }

            //TODO FIXME: result = ClusterHelper.getClusterForInstance(configCtx, instanceName);
        } finally {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, ".getCluster<-: " + result);
            }
        }
        return result;
    }

    public static boolean isClusterHeartbeatEnabled(Cluster cluster) {
        boolean result = false;
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                        ".isClusterHeartbeatEnabled->: " + cluster);
            }
            result = Boolean.valueOf(cluster.getHeartbeatEnabled());
        } finally {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                        ".isClusterHeartbeatEnabled<-: " + cluster
                                + " " + result);
            }
        }
        return result;
    }

    public static boolean member(ClusterInstanceInfo item,
                                 List<ClusterInstanceInfo> list) {
        for (ClusterInstanceInfo element : list) {
            if (equals(item, element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equals(ClusterInstanceInfo c1,
                                 ClusterInstanceInfo c2) {
        if (c1 == c2) {
            return true;
        }
        if (c1.weight != c2.weight) {
            return false;
        }
        if (c1.endpoints.length != c2.endpoints.length) {
            return false;
        }
        if (!c1.name.equals(c2.name)) {
            return false;
        }
        for (int i = 0; i < c1.endpoints.length; ++i) {
            if (c1.endpoints[i].port != c2.endpoints[i].port) {
                return false;
            }
            if (!c1.endpoints[i].type.equals(c2.endpoints[i].type)) {
                return false;
            }
            if (!c1.endpoints[i].host.equals(c2.endpoints[i].host)) {
                return false;
            }
        }
        return true;
    }

    public static String toString(ClusterInstanceInfo cii) {
        return
                "[ClusterInstanceInfo "
                        + cii.name
                        + " "
                        + cii.weight
                        + " "
                        + toString(cii.endpoints)
                        + "]";

    }

    public static String toString(SocketInfo[] socketInfo) {
        String result = "";
        for (int i = 0; i < socketInfo.length; ++i) {
            result += toString(socketInfo[i]) + " ";
        }
        return result;
    }

    public static String toString(SocketInfo socketInfo) {
        return
                "[SocketInfo "
                        + socketInfo.type
                        + " "
                        + socketInfo.host
                        + " "
                        + socketInfo.port
                        + "]";
    }

    public static void forceStackTrace(String msg) {
        forceStackTrace(msg, System.out);
    }

    public static void forceStackTrace(String msg, PrintStream out) {
        try {
            _logger.log(Level.INFO, msg + "->:");
            throw new Exception("FORCED STACKTRACE");
        } catch (Exception e) {
            e.printStackTrace(out);
        } finally {
            _logger.log(Level.INFO, msg + "<-:");
        }
    }
}

class GroupInfoServiceImplForJNLP
        extends org.omg.CORBA.LocalObject
        implements GroupInfoService {
    public boolean addObserver(GroupInfoServiceObserver x) {
        throw new RuntimeException("SHOULD NOT BE CALLED");
    }

    public void notifyObservers() {
        throw new RuntimeException("SHOULD NOT BE CALLED");
    }

    public List<ClusterInstanceInfo> getClusterInstanceInfo(
            String[] adapterName) {
        if (adapterName != null) {
            throw new RuntimeException("Argument should be null");
        }
        return IIOPEndpointsInfo.getClusterInstanceInfo();
    }

    public boolean shouldAddAddressesToNonReferenceFactory(String[] x) {
        throw new RuntimeException("SHOULD NOT BE CALLED");
    }

    public boolean shouldAddMembershipLabel(String[] adapterName) {
        throw new RuntimeException("SHOULD NOT BE CALLED");
    }
}
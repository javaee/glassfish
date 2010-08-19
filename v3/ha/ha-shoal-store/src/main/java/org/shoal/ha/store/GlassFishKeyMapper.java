package org.shoal.ha.store;

import org.glassfish.ha.common.HACookieManager;
import org.jvnet.hk2.annotations.Service;
import org.shoal.ha.mapper.DefaultKeyMapper;

import org.glassfish.ha.common.GlassFishHAReplicaPredictor;
import org.glassfish.ha.common.HACookieInfo;

/**
 * @author Mahesh Kannan
 *
 */
public class GlassFishKeyMapper
    extends DefaultKeyMapper
    implements GlassFishHAReplicaPredictor {

    private static final String[] _EMPTY_TARGETS = new String[] {null, null};

    public GlassFishKeyMapper(String instanceName, String groupName) {
        super(instanceName, groupName);
    }


    @Override
    public HACookieInfo makeCookie(String groupName, Object key, String version) {

        System.out.println("ENTERED makeCookie(" + key + ", " + version + ")");
        String cookieStr = null;
        if (version == null) {
            version = "-1";
        }

        if (key == null) {
            key = Thread.currentThread().getName() + "-";
        }
        String str = super.getMappedInstance(groupName, key);
        cookieStr = (str == null) ? (":" + version) : (str + ":" + version);

        System.out.println("makeCookie calling new HACookieInfo(" + cookieStr + ")");
        return new HACookieInfo(cookieStr);
    }

    /*
    @Override
    public String getMappedInstance(String groupName, Object key1) {
        HACookieInfo cookieInfo = HACookieManager.getCurrent();
        if (cookieInfo.isInitialized()) {
            return cookieInfo.getReplica();
        } else {
            return super.getMappedInstance(groupName, key1);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }


    @Override
    public String[] findReplicaInstance(String groupName, Object key1, String keyMappingInfo) {
        HACookieInfo cookieInfo = HACookieManager.getCurrent();
        if (cookieInfo.isInitialized()) {
            return cookieInfo.getReplica();
        } else {
            return super.getMappedInstance(groupName, key1);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }
    */
}
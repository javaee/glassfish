package org.glassfish.enterprise.iiop.impl;

import org.glassfish.enterprise.iiop.api.GlassFishORBFactory;
import org.glassfish.enterprise.iiop.impl.GlassFishORBManager;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.omg.CORBA.ORB;

import java.util.Properties;

/**
 * @author Mahesh Kannan
 *         Date: Jan 15, 2009
 */
@Service
public class GlassFishORBFactoryImpl
        implements GlassFishORBFactory {

    @Inject
    Habitat habitat;

    private volatile ORB gfORB;

    public ORB createORB(Properties props) {
        if (gfORB == null) {
            synchronized (this) {
                if (gfORB == null) {
                    gfORB = GlassFishORBManager.getORB(props);
                }
            }
        }

        return gfORB;
    }
}
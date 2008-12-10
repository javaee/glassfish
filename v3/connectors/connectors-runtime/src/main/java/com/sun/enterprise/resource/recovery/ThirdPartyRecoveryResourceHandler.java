package com.sun.enterprise.resource.recovery;

import javax.transaction.xa.XAResource;
import java.util.Set;
import java.util.List;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import com.sun.enterprise.transaction.spi.RecoveryResourceListener;
import com.sun.enterprise.transaction.spi.RecoveryResourceHandler;
import com.sun.enterprise.transaction.api.RecoveryResourceRegistry;


/**
 * RecoveryResourceHandler for third party resources
 *
 * @author Jagadish Ramu
 */
@Service
public class ThirdPartyRecoveryResourceHandler implements RecoveryResourceHandler {

    @Inject
    private RecoveryResourceRegistry rrr;
    /**
     * {@inheritDoc}
     */
    public void loadXAResourcesAndItsConnections(List xaresList, List connList) {
        Set<RecoveryResourceListener> listeners =
                rrr.getListeners();

        for (RecoveryResourceListener rrl : listeners) {
            XAResource[] xars = rrl.getXAResources();
            for (XAResource xar : xars) {
                xaresList.add(xar);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void closeConnections(List connList) {
        //do nothing
    }
}

package org.jvnet.hk2.config.provider;

import org.jvnet.hk2.annotations.Contract;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;

/**
 * 
 * @author Jeff Trent
 */
@Contract
public interface ServerService extends PostConstruct, PreDestroy {

}

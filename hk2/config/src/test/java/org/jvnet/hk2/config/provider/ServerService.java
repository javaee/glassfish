package org.jvnet.hk2.config.provider;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

/**
 * 
 * @author Jeff Trent
 */
@Contract
public interface ServerService extends PostConstruct, PreDestroy {

}

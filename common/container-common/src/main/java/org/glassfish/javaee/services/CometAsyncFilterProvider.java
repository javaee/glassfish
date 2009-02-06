package org.glassfish.javaee.services;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.ContractProvided;
import com.sun.grizzly.arp.AsyncFilter;
import com.sun.grizzly.comet.CometAsyncFilter;

/**
 * I really wished Grizzly would start using hk2 so I don't have to exist
 *
 * @Author Jerome Dochez
 * Date: Apr 16, 2008
 * Time: 1:38:51 PM
 */
@Service(name="comet")
@ContractProvided(AsyncFilter.class)
public class CometAsyncFilterProvider extends CometAsyncFilter{

}

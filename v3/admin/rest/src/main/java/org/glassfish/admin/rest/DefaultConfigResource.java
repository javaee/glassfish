/**

 * Ludovic Champenois ludo@dev.java.net
 *
 **/
package org.glassfish.admin.rest;

import javax.ws.rs.Path;
import com.sun.enterprise.config.serverbeans.Config;
import org.glassfish.admin.rest.resources.ConfigResource;

@Path("/server-config/")
public class DefaultConfigResource extends ConfigResource {

    @Override
    public Config getEntity() {
        return RestService.theDomain.getConfigs().getConfig().get(0);
    }
}

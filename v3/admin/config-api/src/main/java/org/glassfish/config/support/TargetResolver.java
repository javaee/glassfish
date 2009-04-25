package org.glassfish.config.support;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import com.sun.enterprise.config.serverbeans.Domain;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Apr 13, 2009
 * Time: 10:07:04 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class TargetResolver implements ConfigResolver {

    @Inject
    Domain domain;

    @Param(optional=true)
    String target;
    
    public ConfigBeanProxy resolve(AdminCommandContext context) {
        if (target==null) {
            return domain;
        }
        return null;
    }
}

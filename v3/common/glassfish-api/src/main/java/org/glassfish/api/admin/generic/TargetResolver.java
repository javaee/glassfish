package org.glassfish.api.admin.generic;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Apr 13, 2009
 * Time: 10:07:04 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class TargetResolver implements ConfigResolver {

    @Param
    String target;
    
    public Object resolve(AdminCommandContext context) {
        return null;
    }
}

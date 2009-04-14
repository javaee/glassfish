package org.glassfish.api.admin.generic;

import org.jvnet.hk2.annotations.Contract;
import org.glassfish.api.admin.AdminCommandContext;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Apr 13, 2009
 * Time: 10:07:28 PM
 * To change this template use File | Settings | File Templates.
 */
@Contract
public interface ConfigResolver {

    Object resolve(AdminCommandContext context); 
}

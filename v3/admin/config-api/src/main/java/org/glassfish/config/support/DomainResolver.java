package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.Domain;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Dom;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Mar 15, 2010
 * Time: 3:26:39 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class DomainResolver implements ConfigResolver {

    @Inject
    Domain domain;

    @Inject
    Habitat habitat;

    @Override
    public ConfigBeanProxy resolve(AdminCommandContext context, String elementName, Class<? extends ConfigBeanProxy> type) {
        if (elementName==null)
            return domain;


        return habitat.getComponent(type);
    }
}

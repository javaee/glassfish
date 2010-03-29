package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.*;

import java.beans.PropertyVetoException;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Mar 15, 2010
 * Time: 3:26:39 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class TypeResolver implements ConfigResolver {

    @Inject
    Habitat habitat;

    @Inject
    Domain domain;

    final protected static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(TypeResolver.class);

    @Override
    public ConfigBeanProxy resolve(AdminCommandContext context, final Class<? extends ConfigBeanProxy> type) {
        ConfigBeanProxy proxy = habitat.getComponent(type);
        if (proxy==null) {
            try {
                proxy = (ConfigBeanProxy) ConfigSupport.apply(new SingleConfigCode<Domain>() {
                    @Override
                    public Object run(Domain writeableDomain) throws PropertyVetoException, TransactionFailure {
                        ConfigBeanProxy child = writeableDomain.createChild(type);
                        Dom domDomain = Dom.unwrap(writeableDomain);
                        ConfigModel model = domDomain.model;
                        final String elementName;
                        try {
                            elementName = GenericCrudCommand.elementName(domDomain.document, Domain.class, type);
                        } catch (ClassNotFoundException e) {
                            throw new TransactionFailure(e.toString());
                        }
                        domDomain.setNodeElements(elementName, Dom.unwrap(child));

                        // add to the habitat
                        habitat.addIndex(new ExistingSingletonInhabitant<ConfigBeanProxy>(child), type.getName(), null);

                        return child;
                    }
                }, domain);
            } catch(TransactionFailure e) {
                throw new RuntimeException(e);
            }
            if (proxy==null) {
                String msg = localStrings.getLocalString(TypeAndNameResolver.class,
                        "TypeResolver.target_object_not_found",
                        "Cannot find a single component instance of type {0}", type.getSimpleName());
                throw new RuntimeException(msg);
            }
        }
        return proxy;

    }
}

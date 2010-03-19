package org.glassfish.config.support;

import com.sun.hk2.component.InjectionResolver;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandModelProvider;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.DomDocument;

import java.util.List;

/**
 * services pertinent to generic CRUD command implementations
 *
 * @author Jerome Dochez
 *
 */
public abstract class GenericCrudCommand implements CommandModelProvider {
    private InjectionResolver<Param> injector;

    @Inject
    DomDocument document;

    // todo : would be lovely to replace this with some smart injection...
    public void setInjectionResolver(InjectionResolver<Param> injector) {
        this.injector = injector;
    }

    public InjectionResolver<Param> getInjectionResolver() {
        return injector;
    }

    /**
     * Returns the element name used by the parent to store instances of the child
     *
     * @param parent type of the parent
     * @param child type of the child
     * @return the element name holding child's instances in the parent
     */
    protected String elementName(Class<ConfigBeanProxy> parent, Class<ConfigBeanProxy> child)
        throws ClassNotFoundException {

        ConfigModel cm = document.buildModel(parent);
        for (String elementName : cm.getElementNames()) {
            ConfigModel.Property prop = cm.getElement(elementName);
            if (prop instanceof ConfigModel.Node) {
                ConfigModel childCM = ((ConfigModel.Node) prop).getModel();
                String childTypeName = childCM.targetTypeName;
                if (childTypeName.equals(child.getName())) {
                    return elementName;
                }
                // check the inheritance hierarchy
                List<ConfigModel> subChildrenModels = document.getAllModelsImplementing(
                        childCM.classLoaderHolder.get().loadClass(childTypeName));
                for (ConfigModel subChildModel : subChildrenModels) {
                    if (subChildModel.targetTypeName.equals(child.getName())) {
                        return elementName;
                    }
                }

            }
        }
        return null;
    }
 
}

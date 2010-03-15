package org.glassfish.config.support;

import com.sun.hk2.component.InjectionResolver;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandModel;

/**
 * services pertinent to generic CRUD command implementations
 *
 * @author Jerome Dochez
 *
 */
public abstract class GenericCrudCommand {
    private InjectionResolver<Param> injector;

    // todo : would be lovely to replace this with some smart injection...
    public void setInjectionResolver(InjectionResolver<Param> injector) {
        this.injector = injector;
    }

    public InjectionResolver<Param> getInjectionResolver() {
        return injector;
    }


    public abstract CommandModel getCommandModel();
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jvnet.testing.hk2mockito.fixture.assisted;

import java.lang.reflect.Type;
import javax.inject.Inject;
import javax.inject.Named;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import static org.glassfish.hk2.api.InjectionResolver.SYSTEM_RESOLVER_NAME;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceHandle;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author saden
 */
@Rank(50)
@Service
public class CustomAssistedResolver implements InjectionResolver<Inject> {

    private final InjectionResolver<Inject> systemResolver;

    @Inject
    CustomAssistedResolver(@Named(SYSTEM_RESOLVER_NAME) InjectionResolver systemResolver) {
        this.systemResolver = systemResolver;
    }

    @Override
    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        Type type = injectee.getRequiredType();

        if (type instanceof Class && CustomService.class.isAssignableFrom((Class) type)) {
            return new CustomService();
        }

        return systemResolver.resolve(injectee, root);
    }

    @Override
    public boolean isConstructorParameterIndicator() {
        return false;
    }

    @Override
    public boolean isMethodParameterIndicator() {
        return false;
    }

}

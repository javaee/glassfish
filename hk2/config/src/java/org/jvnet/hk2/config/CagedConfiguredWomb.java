package org.jvnet.hk2.config;

import org.jvnet.hk2.component.Womb;
import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.ComponentException;
import com.sun.hk2.component.ExistingSingletonInhabitant;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Feb 15, 2008
 * Time: 4:38:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class CagedConfiguredWomb<T> extends ConfiguredWomb<T> {

    final CageBuilder builder;


    public CagedConfiguredWomb(Womb core, Dom dom, CageBuilder builder) {
        super(core, dom);
        this.builder = builder;
    }

    public void initialize(T t, Inhabitant onBehalfOf) throws ComponentException {
        super.initialize(t, onBehalfOf);
        builder.onEntered(new ExistingSingletonInhabitant(t));
    }

}

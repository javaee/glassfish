package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantRequested;
import org.jvnet.hk2.test.contracts.Simple;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: May 17, 2010
 * Time: 12:53:20 PM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="one")
public class OneSimple implements Simple, InhabitantRequested {
    public Inhabitant<?> self;
    
    public String get() {
        return "one";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setInhabitant(Inhabitant inhabitant) {
      this.self = inhabitant;
    }
}

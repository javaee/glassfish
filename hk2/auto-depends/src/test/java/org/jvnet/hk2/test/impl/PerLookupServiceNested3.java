package org.jvnet.hk2.test.impl;

import org.glassfish.hk2.scopes.PerLookup;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantRequested;
import org.glassfish.hk2.PostConstruct;
import org.glassfish.hk2.PreDestroy;

import com.sun.hk2.component.Holder;

@Service
@Scoped(PerLookup.class)
public class PerLookupServiceNested3 implements PostConstruct, PreDestroy, InhabitantRequested {
    public static int constructs;
    public static int destroys;
    
    public Inhabitant<?> self;
    
    @Inject
    Holder<PerLookupService> perLookupServiceHolder;

    @Override
    public void postConstruct() {
      constructs++;
      if (null == self) {
        throw new IllegalStateException();
      }
      if (null == perLookupServiceHolder) {
        throw new IllegalStateException();
      }
    }

    @Override
    public void preDestroy() {
      destroys++;
      if (null == self) {
        throw new IllegalStateException();
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setInhabitant(Inhabitant inhabitant) {
      self = inhabitant;
    }
}

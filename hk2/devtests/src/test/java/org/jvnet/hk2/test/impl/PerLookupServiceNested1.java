package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantRequested;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

@Service
@Scoped(PerLookup.class)
public class PerLookupServiceNested1 implements PostConstruct, PreDestroy, InhabitantRequested {
    public static int constructs;
    public static int destroys;
    
    public Inhabitant<?> self;
    
    PerLookupServiceNested2 perLookupServiceNested2;

    @SuppressWarnings("unused")
    @Inject
    private void setPerLookupServiceNested2(PerLookupServiceNested2 service) {
      perLookupServiceNested2 = service;
    }
    
    @Override
    public void postConstruct() {
      constructs++;
      if (null == self) {
        throw new IllegalStateException();
      }
      if (null == perLookupServiceNested2) {
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

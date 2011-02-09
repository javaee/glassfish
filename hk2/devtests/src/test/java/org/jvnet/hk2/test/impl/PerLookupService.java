package org.jvnet.hk2.test.impl;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

@Service
@Scoped(PerLookup.class)
public class PerLookupService implements PostConstruct, PreDestroy {

  public static int constructs;
  public static int destroys;

  @Inject
  PerLookupServiceNested1 perLookupServiceNested1;
  
  @Override
  public void postConstruct() {
    constructs++;
    if (null == perLookupServiceNested1) {
      throw new IllegalStateException();
    }
  }

  @Override
  public void preDestroy() {
    destroys++;
  }

}

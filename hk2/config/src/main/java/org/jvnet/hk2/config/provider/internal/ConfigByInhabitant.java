package org.jvnet.hk2.config.provider.internal;

import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

import com.sun.hk2.component.EventPublishingInhabitant;
import com.sun.hk2.component.InhabitantStore;

/**
 * Represents a managed instance of a {@link ConfigByMetaInhabitant}.
 * 
 * @author Jeff Trent
 */
@SuppressWarnings("unchecked")
/*public*/ class ConfigByInhabitant extends EventPublishingInhabitant<Object> {

  private final ConfigByMetaInhabitant lead;
  private final InhabitantStore store;
  
  private final ConfigByCreator managedServiceCreator;
  private final MultiMap<String, String> metadata; 

  private volatile Object managedService;
  
  
  ConfigByInhabitant(InhabitantStore store,
        ConfigByMetaInhabitant lead,
        ConfigByCreator managedServiceCreator,
        MultiMap<String, String> md) {
    this.store = store;
    this.lead = lead;
    this.managedServiceCreator = managedServiceCreator;
    this.metadata = (null == md) ? new MultiMap<String, String>() : md;
  }
  
  @Override
  public ConfigByMetaInhabitant lead() {
      return lead;
  }
  
  @Override
  public Object get(Inhabitant onBehalfOf) {
    if (null == managedService) {
      synchronized (this) {
        if (null == managedService) {
          managedService = managedServiceCreator.get(onBehalfOf);
          assert(null != managedService);
        }        
      }
    }
    
    return managedService;
  }
  
  @Override
  public synchronized void release() {
    if (null != managedService) {
      lead().manageRelease(this, store);

      dispose(managedService);
      managedService = null;
      
      super.release();
    }
  }
  
  @Override
  public boolean isInstantiated() {
    return (null != managedService);
  }

  @Override
  public MultiMap<String, String> metadata() {
    return metadata;
  }

  @Override
  public synchronized Class type() {
    return (null == managedService) ? null : managedService.getClass();
  }

  @Override
  public String typeName() {
    // TODO: this might be the wrong decision
    return lead().typeName();
  }

}

package org.jvnet.hk2.config.provider.internal;

import org.glassfish.hk2.api.Descriptor;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

import com.sun.hk2.component.EventPublishingInhabitant;

import java.util.List;
import java.util.Map;

/**
 * Represents a managed instance of a {@link ConfigByMetaInhabitant}.
 * 
 * @author Jeff Trent
 */
@SuppressWarnings("unchecked")
/*public*/ class ConfigByInhabitant extends EventPublishingInhabitant<Object> {

  private final ConfigByMetaInhabitant lead;
  
  private final ConfigByCreator managedServiceCreator;

  private volatile Object managedService;
  
  
  ConfigByInhabitant(
        ConfigByMetaInhabitant lead,
        ConfigByCreator managedServiceCreator,
        MultiMap<String, String> md) {
    super(lead.getServiceLocator(), (Descriptor) null); // TODO: handle descriptor
    this.lead = lead;
    this.managedServiceCreator = managedServiceCreator;
  }
  
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

      dispose(managedService);
      managedService = null;
      
      super.release();
    }
  }
  
  @Override
  public boolean isActive() {
    return (null != managedService);
  }

  @Override
  public synchronized Class type() {
    return (null == managedService) ? null : managedService.getClass();
  }

}

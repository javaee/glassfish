package rls.test.model;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

@Service
public class ServiceZ implements PostConstruct {

  public static int ctorCount = 0;
  public static int postConstructCount = 0;
  
  public ServiceZ() {
    ctorCount++;
  }
  
  @Override
  public void postConstruct() {
    postConstructCount++;
  }
}

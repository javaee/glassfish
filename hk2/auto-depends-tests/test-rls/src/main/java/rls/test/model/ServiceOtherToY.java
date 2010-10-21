package rls.test.model;

import org.jvnet.hk2.annotations.Inject;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.internal.runlevel.DefaultRunLevelService;

import com.sun.hk2.component.Holder;

@RunLevel(DefaultRunLevelService.KERNEL_RUNLEVEL)
@Service(name="other")
public class ServiceOtherToY implements PostConstruct {

  public static int ctorCount = 0;
  public static int postConstructCount = 0;
  
  @Inject public static ContractY y;
  
  @Inject public static ContractY[] allY;

  @Inject public static Holder<ServiceZ> zHolder;
  
  
  public ServiceOtherToY() {
    ctorCount++;
  }
  
  @Override
  public void postConstruct() {
    postConstructCount++;
  }
 
}

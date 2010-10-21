package rls.test.model;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.internal.runlevel.DefaultRunLevelService;

@RunLevel(DefaultRunLevelService.KERNEL_RUNLEVEL)
@Service(name="base")
public class ServiceBaseX implements ContractX, PostConstruct {

  public static int ctorCount = 0;
  public static int postConstructCount = 0;
  
  @Inject public static ContractY y;
  
  @Inject(name="other") public static ServiceOtherToY other;
  
  public ServiceBaseX() {
    ctorCount++;
  }
  
  @Override
  public void postConstruct() {
    postConstructCount++;
  }
 
}

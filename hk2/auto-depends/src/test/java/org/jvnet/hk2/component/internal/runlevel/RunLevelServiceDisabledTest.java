package org.jvnet.hk2.component.internal.runlevel;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.junit.Hk2Runner;
import org.jvnet.hk2.junit.Hk2RunnerOptions;

/**
 * Custom Hk2RunnerOptions to disable default RunLevelService.
 * 
 * @author Jeff Trent
 */
@RunWith(Hk2Runner.class)
@Hk2RunnerOptions(enableDefaultRunLevelService=false)
public class RunLevelServiceDisabledTest {

  @Inject(name="default")
  RunLevelService<?> rls;
  
  @Test
  public void testProceedTo() throws Exception {
    assertNotNull(rls);
    assertNull("looks like RLS ran, and it shouldn't have", rls.getState().getCurrentRunLevel());

    try {
      rls.proceedTo(5);
      fail("expected to see an exception when RLS is disabled");
    } catch (IllegalStateException e) {
      //expected
    }
  }

}

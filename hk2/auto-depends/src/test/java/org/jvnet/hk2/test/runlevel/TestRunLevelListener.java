package org.jvnet.hk2.test.runlevel;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.RunLevelListener;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.RunLevelState;
import org.jvnet.hk2.component.ServiceContext;

/**
 * Records calls to a RunLevelListener.
 * 
 * @author Jeff Trent
 */
@Service
@Ignore
public class TestRunLevelListener implements RunLevelListener {

  public final List<Call> calls = new ArrayList<Call>();
  
  public Integer proceedToWaitFor;
  public Integer proceedToGoTo;
  public RunLevelService<?> proceedToRls;
  
  
  @Override
  public void onCancelled(RunLevelState<?> state, int previousProceedTo) {
    calls.add(Call.onCancelled(state, previousProceedTo));
  }

  @Override
  public void onError(RunLevelState<?> state, ServiceContext context,
      Throwable error, boolean willContinue) {
    calls.add(Call.onError(state, context, error, willContinue));
  }

  @Override
  public void onProgress(RunLevelState<?> state) {
    calls.add(Call.onProgress(state));
    if (null != proceedToWaitFor && 
        state.getCurrentRunLevel() == proceedToWaitFor) {
      proceedToWaitFor = null;
      proceedToRls.proceedTo(proceedToGoTo);
    }
  }

  public void setProgressProceedTo(int i, int j, RunLevelService<?> rls) {
    proceedToWaitFor = i;
    proceedToGoTo = j;
    proceedToRls = rls;
  }

  
  public static class Call {
    public final String type;
    public final Integer current;
    public final Integer planned;
    public final Class<?> env;
    public final Integer previousProceedTo;
    public final ServiceContext context;
    public final Throwable error;
    public final Boolean willContinue;
    
    public Call(String type, RunLevelState<?> rls, Integer prv) {
      this(type, rls, prv, null, null, null);
    }
    
    public Call(String type, RunLevelState<?> rls, Integer prv, ServiceContext ctx,
        Throwable error, Boolean willCont) {
      this.type = type;
      this.current = rls.getCurrentRunLevel();
      this.planned = rls.getPlannedRunLevel();
      this.env = rls.getEnvironment();
      this.previousProceedTo = prv;
      this.context = ctx;
      this.error = error;
      this.willContinue = willCont;
    }

    @Override
    public String toString() {
      return type + ";ctx=" + context + ";cl=" + current;
    }
    
    public static Call onCancelled(RunLevelState<?> rls, int previousProceedTo) {
      return new Call("cancelled", rls, previousProceedTo);
    }

    public static Call onError(RunLevelState<?> rls, ServiceContext ctx,
        Throwable error, boolean willContinue) {
      return new Call("error", rls, null, ctx, error, willContinue);
    }

    public static Call onProgress(RunLevelState<?> rls) {
      return new Call("progress", rls, null);
    }
  }

}

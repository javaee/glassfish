package org.jvnet.hk2.test.runlevel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
  
  public Integer progress_proceedToWaitFor;
  public Integer progress_proceedToGoTo;
  public RunLevelService<?> progress_proceedToRls;
  
  public Integer error_proceedToGoTo;
  public RunLevelService<?> error_proceedToRls;
  
  public Integer cancel_proceedToGoTo;
  public RunLevelService<?> cancel_proceedToRls;
  
  @Override
  public void onCancelled(RunLevelState<?> state, int previousProceedTo) {
    calls.add(Call.onCancelled(state, previousProceedTo));
    if (null != cancel_proceedToGoTo &&
        null != cancel_proceedToRls) {
      int pto = cancel_proceedToGoTo;
      cancel_proceedToGoTo = null;
      cancel_proceedToRls.proceedTo(pto);
    }
  }

  @Override
  public void onError(RunLevelState<?> state, ServiceContext context,
      Throwable error, boolean willContinue) {
    calls.add(Call.onError(state, context, error, willContinue));
    if (null != error_proceedToGoTo &&
        null != error_proceedToRls) {
      int pto = error_proceedToGoTo;
      error_proceedToGoTo = null;
      error_proceedToRls.proceedTo(pto);
    }
  }

  @Override
  public void onProgress(RunLevelState<?> state) {
    Logger.getAnonymousLogger().log(Level.FINE, state.toString());
    calls.add(Call.onProgress(state));
    if (null != progress_proceedToWaitFor && 
        state.getCurrentRunLevel() == progress_proceedToWaitFor) {
      int pto = progress_proceedToGoTo;
      progress_proceedToGoTo = null;
      progress_proceedToRls.proceedTo(pto);
    }
  }

  public void setProgressProceedTo(int i, int j, RunLevelService<?> rls) {
    progress_proceedToWaitFor = i;
    progress_proceedToGoTo = j;
    progress_proceedToRls = rls;
  }

  public void setErrorProceedTo(int j, RunLevelService<?> rls) {
    error_proceedToGoTo = j;
    error_proceedToRls = rls;
  }

  public void setCancelProceedTo(int j, RunLevelService<?> rls) {
    cancel_proceedToGoTo = j;
    cancel_proceedToRls = rls;
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

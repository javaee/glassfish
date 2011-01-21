/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
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
  private Runnable progress_runnable;
  
  public Integer error_proceedToGoTo;
  public RunLevelService<?> error_proceedToRls;
  
  public Integer cancel_proceedToGoTo;
  public RunLevelService<?> cancel_proceedToRls;

  @Override
  public void onCancelled(RunLevelState<?> state,
      ServiceContext context,
      int previousProceedTo,
      boolean isHard) {
    calls.add(Call.onCancelled(state, context, previousProceedTo, isHard));
    if (null != cancel_proceedToGoTo &&
        null != cancel_proceedToRls) {
      int pto = cancel_proceedToGoTo;
      cancel_proceedToGoTo = null;
      cancel_proceedToRls.proceedTo(pto);
    }
  }

  @Override
  public void onError(RunLevelState<?> state,
      ServiceContext context,
      Throwable error,
      boolean willContinue) {
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
      if (null != progress_proceedToGoTo) {
        int pto = progress_proceedToGoTo;
        progress_proceedToGoTo = null;
        progress_proceedToRls.proceedTo(pto);
      }
      if (null != progress_runnable) {
        Runnable runnable = progress_runnable;
        progress_runnable = null;
        runnable.run();
      }
    }
  }

  public void setProgressProceedTo(int i, int j, RunLevelService<?> rls) {
    progress_proceedToWaitFor = i;
    progress_proceedToGoTo = j;
    progress_proceedToRls = rls;
  }

  public void setProgressRunnable(int i, Runnable run) {
    progress_proceedToWaitFor = i;
    progress_runnable = run;
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
    public final Boolean isHardInterrupt;
    
    public Call(String type, RunLevelState<?> rls, Integer prv) {
      this(type, rls, prv, null, null, null, null);
    }
    
    public Call(String type, RunLevelState<?> rls, ServiceContext ctx,
        Integer prv, boolean isHardInterrupt) {
      this(type, rls, prv, ctx, null, null, isHardInterrupt);
    }
    
    public Call(String type, RunLevelState<?> rls, Integer prv, ServiceContext ctx,
        Throwable error, Boolean willCont, Boolean isHardInterrupt) {
      this.type = type;
      this.current = rls.getCurrentRunLevel();
      this.planned = rls.getPlannedRunLevel();
      this.env = rls.getEnvironment();
      this.previousProceedTo = prv;
      this.context = ctx;
      this.error = error;
      this.willContinue = willCont;
      this.isHardInterrupt = isHardInterrupt;
    }

    @Override
    public String toString() {
      return type + ";ctx=" + context + ";cl=" + current + ";hrdI=" + isHardInterrupt + " ";
    }
    
    public static Call onCancelled(RunLevelState<?> rls, ServiceContext ctx,
        int previousProceedTo, boolean isHard) {
      return new Call("cancelled", rls, ctx, previousProceedTo, isHard);
    }

    public static Call onError(RunLevelState<?> rls, ServiceContext ctx,
        Throwable error, boolean willContinue) {
      return new Call("error", rls, null, ctx, error, willContinue, null);
    }

    public static Call onProgress(RunLevelState<?> rls) {
      return new Call("progress", rls, null);
    }
  }

}

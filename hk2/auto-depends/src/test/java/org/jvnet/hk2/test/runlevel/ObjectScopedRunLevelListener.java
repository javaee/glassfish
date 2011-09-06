package org.jvnet.hk2.test.runlevel;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.RunLevelListener;
import org.jvnet.hk2.component.RunLevelState;
import org.jvnet.hk2.component.ServiceContext;

@Service(metadata = RunLevel.META_SCOPE_TAG + "=java.lang.Object")
public class ObjectScopedRunLevelListener implements RunLevelListener {

    public static boolean called;
    
    @Override
    public void onCancelled(RunLevelState<?> state, ServiceContext ctx,
            int previousProceedTo, boolean isInterrupt) {
        ObjectScopedRunLevelListener.called = true;
    }

    @Override
    public void onError(RunLevelState<?> state, ServiceContext context,
            Throwable error, boolean willContinue) {
        ObjectScopedRunLevelListener.called = true;
    }

    @Override
    public void onProgress(RunLevelState<?> state) {
        ObjectScopedRunLevelListener.called = true;
    }
    
}

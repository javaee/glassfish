/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight;

import com.sun.enterprise.config.serverbeans.MonitoringService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.glassfish.api.monitoring.DTraceContract;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.jvnet.hk2.component.Habitat;

/**
 * Note that you MUST call an initilizer for this class for DTrace!
 * Why? We do not want the overhead of being a Service that implements a Contract
 * which is what you need to get a habitat object!
 *
 * @author Byron Nevins
 */

public class FlashlightUtils {
    private FlashlightUtils() {
        // All static.  No instances llowed.
    }

    public static void initialize(Habitat h, MonitoringService mc) {
        // only do once -- ignore multiple calls...
        synchronized(LOCK) {
            if(habitat == null) {
                habitat = h;
                monConfig = mc;
                setDTraceAvailabilty();
            }
        }
    }

    public static boolean isDtraceEnabled() {
        ok();

        if(dt == null)
            return false;

        if(!Boolean.parseBoolean(monConfig.getMonitoringEnabled()))
            return false;

        if(!Boolean.parseBoolean(monConfig.getDtraceEnabled()))
            return false;

        return true;
    }

    static public DTraceContract getDtraceEngine() {
        return isDtraceEnabled() ? dt : null;
    }

    private static void setDTraceAvailabilty() {
        // the code below fails fast -- it marches through returning immediately
        // when something is amiss instead of having complicated hard-to-read nested
        // blocks of code.

        dt = null;

        // AS_DTRACE check is temporary...
        if(!Boolean.parseBoolean(System.getenv("AS_DTRACE"))) {
            return;
        }

        dt = habitat.getByContract(DTraceContract.class);

        if(dt == null)
            return;

        if(!dt.isSupported())
            dt = null;
        // else dt is available!!
    }

    /** bnevins -- I see 2 exact copies of this big chunk of code -- so I moved it here!
     *
     * @param method
     * @return
     */
    public static String[] getParamNames(Method method) {
        String[] paramNames = new String[method.getParameterTypes().length];
        Annotation[][] allAnns = method.getParameterAnnotations();
        int index = 0;

        for (Annotation[] paramAnns : allAnns) {
            for (Annotation ann : paramAnns) {
                if(ann instanceof ProbeParam) {
                    paramNames[index++] = ((ProbeParam)ann).value();
                        break;
                }
            }
        }

        if(index != paramNames.length)
            throw new RuntimeException("All params have to be  named with a ProbeParam Annotation.  This method ("  + method + ") did not have them.");

        return paramNames;
    }

    private static void ok() {
        if(habitat == null || monConfig == null)
            throw new RuntimeException("Internal Error: habitat was not set in " + FlashlightUtils.class);
    }

    private static              Habitat             habitat;
    private static              MonitoringService   monConfig;
    private static              DTraceContract      dt;
    private final static        Object              LOCK                = new Object();
}

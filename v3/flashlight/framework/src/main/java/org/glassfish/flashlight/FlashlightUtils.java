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
import org.glassfish.flashlight.provider.FlashlightProbe;
import org.jvnet.hk2.component.Habitat;

/**
 * Note that you MUST call an initilizer for this class for DTrace!
 * Why? We do not want the overhead of being a Service that implements a Contract
 * which is what you need to get a habitat object!
 *
 * // TODO -- just make this a Service and inject the habitat and simplify the code a bit!
 * @author Byron Nevins
 */

public class FlashlightUtils {
    private FlashlightUtils() {
        // All static.  No instances allowed.
    }

    public static void initialize(Habitat h, MonitoringService mc) {
        // only do once -- ignore multiple calls...
        synchronized(LOCK) {
            if(habitat == null) {
                habitat = h;
                monConfig = mc;
                setDTraceAvailabilty();
                setDTraceEnabled(Boolean.parseBoolean(monConfig.getDtraceEnabled()));
                setMonitoringEnabled(Boolean.parseBoolean(monConfig.getMonitoringEnabled()));
            }
        }
    }

    public static void setDTraceEnabled(boolean b) {
        ok();
        dtraceEnabled = b;
    }

    public static void setMonitoringEnabled(boolean b) {
        ok();
        monitoringEnabled = b;
    }

    public static boolean isMonitoringEnabled() {
        ok();
        return monitoringEnabled;
    }

    public static boolean isDtraceAvailable() {
        ok();

        if(dt == null)
            return false;

        if(!dtraceEnabled)
            return false;

        if(!monitoringEnabled)
            return false;

        return true;
    }

    static public DTraceContract getDtraceEngine() {
        return isDtraceAvailable() ? dt : null;
    }

    private static void setDTraceAvailabilty() {
        // the code below fails fast -- it marches through returning immediately
        // when something is amiss instead of having complicated hard-to-read nested
        // blocks of code.

        ok();
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

    public static boolean isLegalDtraceParam(Class clazz) {
        return isIntegral(clazz) || String.class.equals(clazz);
    }
    
    public static boolean isIntegral(Class clazz) {
        for(Class c : INTEGRAL_CLASSES) {
            if(c.equals(clazz))
                return true;
        }
        return false;
    }

    /**
     * return true if they are the same -- ignoring boxing/unboxing
     * AND if they are "integrals"
     *
     * @param c1
     * @param c2
     * @return
     */
    public static boolean compareIntegral(Class c1, Class c2) {
        // first make sure they are both in the 12 element array of legal classes
        if(!isIntegral(c1) || !isIntegral(c2))
            return false;

        // next a sanity check -- they ought to be different classes but let's check anyways!
        if(c1.equals(c2))
            return true;
        
        if(c1.equals(short.class))  { return c2.equals(Short.class); }
        if(c1.equals(long.class))  { return c2.equals(Long.class); }
        if(c1.equals(int.class))  { return c2.equals(Integer.class); }
        if(c1.equals(byte.class))  { return c2.equals(Byte.class); }
        if(c1.equals(char.class))  { return c2.equals(Character.class); }
        if(c1.equals(boolean.class))  { return c2.equals(Boolean.class); }
        if(c2.equals(short.class))  { return c1.equals(Short.class); }
        if(c2.equals(long.class))  { return c1.equals(Long.class); }
        if(c2.equals(int.class))  { return c1.equals(Integer.class); }
        if(c2.equals(byte.class))  { return c1.equals(Byte.class); }
        if(c2.equals(char.class))  { return c1.equals(Character.class); }
        if(c2.equals(boolean.class))  { return c1.equals(Boolean.class); }
        
        // can't get here!!!
        return false;
    }


    private static void ok() {
        if(habitat == null || monConfig == null)
            throw new RuntimeException("Internal Error: habitat was not set in " + FlashlightUtils.class);
    }

    private static              Habitat             habitat;
    private static              MonitoringService   monConfig;
    private static              DTraceContract      dt;
    private static              boolean             dtraceEnabled;
    private static              boolean             monitoringEnabled;
    private final static        Object              LOCK                = new Object();
    private final static        Class[]             INTEGRAL_CLASSES    = new Class[] {
        int.class, long.class, short.class, boolean.class, char.class, byte.class,
        Integer.class, Long.class, Short.class, Boolean.class, Character.class, Byte.class,
    };
}

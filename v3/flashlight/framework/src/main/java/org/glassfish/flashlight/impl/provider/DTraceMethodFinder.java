/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.impl.provider;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import java.lang.reflect.Method;
import org.glassfish.flashlight.provider.FlashlightProbe;

/**
 *
 * @author bnevins
 */
class DTraceMethodFinder {
    DTraceMethodFinder(FlashlightProbe p, Object t) {
        probe               = p;
        targetObject        = t;
        targetClass         = targetObject.getClass();
        probeParamTypes     = probe.getParamTypes();

        if(probeParamTypes == null)
            probeParamTypes = new Class[0];

        numProbeParams = probeParamTypes.length;
    }

    Method matchMethod() {
        String metname = probe.getProviderJavaMethodName();

        for(Method m : targetClass.getMethods()) {
            if(!m.getName().equals(metname))
                continue;

            // we have a name match!!
            Class[] paramTypes = m.getParameterTypes(); // guaranteed non null

            if(paramTypes.length != numProbeParams)
                continue; // overloaded method

            if(!compareParams(probeParamTypes, paramTypes))
                continue; // overloaded method

            // we have a match!!!
            return  m;
        }
        throw new RuntimeException(strings.get("dtrace_cantfind", metname));
    }

    private boolean compareParams(Class[] probep, Class[] dtracep) {
        // the lengths are guaranteed to be the same!
        for(int i = 0; i < probep.length; i++) {
            Class probeClass = probep[i];
            Class dtraceClass = dtracep[i];

            if(probeClass.equals(dtraceClass))
                continue;

            // something that can be coverted to String...
            else if(dtraceClass.equals(String.class) && !isIntegralPrimitive(probeClass))
                continue;
            else
                return false;
        }
        return true;
    }

    private boolean isIntegralPrimitive(Class clazz) {
        // all primitives except double and float
        if(!clazz.isPrimitive())
            return false;

        if(clazz.equals(double.class) || clazz.equals(float.class))
            return false;

        return true;
    }
    
    private final   FlashlightProbe probe;
    private final   Object          targetObject;
    private final   Class           targetClass;
    private final   int             numProbeParams;
    private         Method          method;
    private         Class[]         probeParamTypes;
    //private final static boolean debug = Boolean.parseBoolean(System.getenv("AS_DEBUG"));
    private static final LocalStringsImpl strings = new LocalStringsImpl(DTraceMethodFinder.class);
}

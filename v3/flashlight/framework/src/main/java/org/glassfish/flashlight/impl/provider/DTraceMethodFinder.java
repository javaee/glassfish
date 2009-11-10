/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.impl.provider;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.glassfish.flashlight.FlashlightUtils;
import org.glassfish.flashlight.provider.FlashlightProbe;

/**
 *
 * @author bnevins
 */
class DTraceMethodFinder {
    private static final Logger logger =
        LogDomains.getLogger(DTraceMethodFinder.class, LogDomains.MONITORING_LOGGER);
    public final static LocalStringManagerImpl localStrings =
                            new LocalStringManagerImpl(DTraceMethodFinder.class);

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

        // bnevins - the contract is that you do not call me with a hidden probe
        // If you do anyway -- I'll throw an unchecked exception as punishment.
        if(probe.isHidden())
            throw new RuntimeException(localStrings.getLocalString("dtrace_cantfind",
                            "The probe is  hidden.  DTrace will ignore it.", metname));

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
        String errStr = localStrings.getLocalString("dtrace_cantfind",
                            "Can not match the Probe method ({0}) with any method in the DTrace object.", metname);
        throw new RuntimeException(errStr);
    }

    private boolean compareParams(Class[] probep, Class[] dtracep) {
        // the lengths are guaranteed to be the same!
        for(int i = 0; i < probep.length; i++) {
            Class probeClass = probep[i];
            Class dtraceClass = dtracep[i];

            if(probeClass.equals(dtraceClass))
                continue;

            // something that can be coverted to String...
            else if(dtraceClass.equals(String.class) && !FlashlightUtils.isIntegral(probeClass))
                continue;

            // check for something like Short.class versus short.class
            // JDK will handle the boxing/unboxing
            else if(FlashlightUtils.compareIntegral(dtraceClass, probeClass))
                continue;
            
            else
                return false;
        }
        
        return true;
    }

    private final   FlashlightProbe probe;
    private final   Object          targetObject;
    private final   Class           targetClass;
    private final   int             numProbeParams;
    private         Method          method;
    private         Class[]         probeParamTypes;
}

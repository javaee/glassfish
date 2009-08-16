/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.impl.client;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import java.lang.reflect.Method;
import java.util.logging.*;
import org.glassfish.flashlight.provider.FlashlightProbe;

/**
 * bnevins Aug 15, 2009
 * DTraceClientInvoker is only public because an internal class is using it from a
 * different package.  If this were C++ we would have a "friend" relationship between
 * the 2 classes.  In Java we are stuck with making it public.
 * Notes:
 * DTrace has a fairly serious limitation.  It only allows parameters that are
 * integral primitives (all primitives except float and double) and java.lang.String
 * So what we do is automagically convert other class objects to String via Object.toString()
 * This brings in a new rub with overloaded methods.  E.g. we can't tell apart these 2 methods:
 * foo(Date) and foo(String)
 * TODO:I believe we should disallow such overloads when the DTrace object is being produced rather than
 * worrying about it in this class.
 * TODO: handle float and double
 * @author bnevins
 */
public class DTraceClientInvoker{
    public DTraceClientInvoker(FlashlightProbe p, Object t, Object[] ps) {
        probe = p;
        targetObject = t;
        args = (ps == null) ? new Object[0] : ps;
        targetClass = targetObject.getClass();
        numParams = args.length;
        fixArgs = new Object[numParams];
    }

    public void invoke() {
        try {
            matchMethod();
            method.invoke(targetObject, fixArgs);
        }
        catch(Exception e) {
            Logger.getAnonymousLogger().warning(e.getMessage());
        }
    }

    private void matchMethod() {
        String metname = probe.getProviderJavaMethodName();
        Class[] probeParamTypes = probe.getParamTypes();
        int numProbeParams = (probeParamTypes == null) ? 0 : probeParamTypes.length;

        if(numProbeParams != numParams)
            throw new RuntimeException(strings.get("dtraceinvoker_numparams", numParams, numProbeParams));

        for(Method m : targetClass.getMethods()) {
            if(!m.getName().equals(metname))
                continue;
            // name match!!
            Class[] paramTypes = m.getParameterTypes(); // gyuaranteed non null

            if(paramTypes.length != numParams)
                continue; // overloaded method

            if(!compareParams(probeParamTypes, paramTypes))
                continue; // overloaded method

            // we have a match!!!
            method = m;
            fixTheArgs(probeParamTypes, paramTypes);
            return;
        }
        throw new RuntimeException(strings.get("dtraceinvoker_cantfind", metname));
    }

    private boolean compareParams(Class[] probep, Class[] dtracep) {
        // the lengths are guaranteed to be the same!
        for(int i = 0; i < probep.length; i++) {
            Class probeClass = probep[i];
            Class dtraceClass = dtracep[i];

            if(probeClass.equals(dtraceClass))
                continue;
            else if(dtraceClass.equals(String.class) && !probeClass.isPrimitive())
                continue;
            else
                return false;
        }
        return true;
    }


    private void fixTheArgs(Class[] probep, Class[] dtracep) {
        // the lengths are guaranteed to be the same!
        // if probep is a class other than String -- convert
        for(int i = 0; i < probep.length; i++) {
            Class probeClass = probep[i];
            Class dtraceClass = dtracep[i];

            if(probeClass.equals(dtraceClass))
                fixArgs[i] = args[i];
            else
                fixArgs[i] = args[i].toString();
        }
    }

    private final   FlashlightProbe probe;
    private final   Object          targetObject;
    private final   Object[]        args;
    private final   Object[]        fixArgs;
    private final   Class           targetClass;
    private final   int             numParams;
    private         Method          method;

    private final static boolean debug = Boolean.parseBoolean(System.getenv("AS_DEBUG"));
    private static final LocalStringsImpl strings = new LocalStringsImpl(DTraceClientInvoker.class);
}

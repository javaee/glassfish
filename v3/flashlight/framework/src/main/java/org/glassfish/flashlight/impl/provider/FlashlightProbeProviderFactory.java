/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package org.glassfish.flashlight.impl.provider;

import com.sun.enterprise.config.serverbeans.MonitoringService;
import com.sun.enterprise.util.ObjectAnalyzer;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import org.glassfish.api.monitoring.DTraceContract;
import org.glassfish.flashlight.FlashlightUtils;
import org.glassfish.flashlight.impl.client.FlashlightProbeClientMediator;
import org.glassfish.flashlight.provider.*;
import org.glassfish.flashlight.impl.core.*;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.external.probe.provider.annotations.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

/**
 * @author Mahesh Kannan
 * @author Byron Nevins
 */
@Service
public class FlashlightProbeProviderFactory
        implements ProbeProviderFactory, PostConstruct {
    @Inject
    MonitoringService monitoringServiceConfig;

    @Inject
    ProbeProviderEventManager ppem;
    
    @Inject
    Habitat habitat;

    private final static Set<FlashlightProbeProvider> allProbeProviders = new HashSet<FlashlightProbeProvider>();
    private boolean debug = false;
    private final static Logger logger = Logger.getLogger(FlashlightProbeProviderFactory.class.getName());
    private final HashMap<String, Class> primTypes = new HashMap<String, Class>() {
        {
            put("int",Integer.TYPE);
            put("byte",Byte.TYPE);
            put("char",Character.TYPE);
            put("short",Short.TYPE);
            put("long",Long.TYPE);
            put("float",Float.TYPE);
            put("double",Double.TYPE);
            put("boolean",Boolean.TYPE);
            put("void",Void.TYPE);
        }
    };

    public void postConstruct() {
        FlashlightUtils.initialize(habitat, monitoringServiceConfig);
    }

    public void dtraceEnabledChanged(boolean newValue) {
        FlashlightUtils.setDTraceEnabled(newValue);

        // if true->false we just need to set the enabled flag
        // DTrace invoker will simply not call the already-built
        // DTrace objects

        if(newValue == false)
            return;
        
        // if it still is not available -- return.  E.g. they set the flag
        // but they don't have the native libs
        
        if(!FlashlightUtils.isDtraceAvailable())
            return;
        
        // if false-> true we might need to create all the DTrace classes.  It 
        // depends on whether the server started up with it true, etc.
        // loop through all the Providers -- if any don't have DTrace then
        // instrument with DTrace
        Collection<FlashlightProbeProvider> pps = ProbeProviderRegistry.getInstance().getAllProbeProviders();

        for(FlashlightProbeProvider pp : pps) {
            if(!pp.isDTraceInstrumented()) {
                handleDTrace(pp);
            }
        }
    }

    public void monitoringEnabledChanged(boolean newValue) {
        FlashlightUtils.setMonitoringEnabled(newValue);

        // if monitoring-enabled is going false -> true AND
        // dtrace-enabled is true -- then we need to do something
        // o/w we don't have to do anything

        if(newValue == true && FlashlightUtils.isDtraceEnabled())
            dtraceEnabledChanged(true);
    }
    
    public <T> T getProbeProvider(Class<T> providerClazz)
            throws InstantiationException, IllegalAccessException {
        //TODO: check for null and generate default names
        ProbeProvider provAnn = providerClazz.getAnnotation(ProbeProvider.class);

        return getProbeProvider(provAnn.moduleProviderName(), provAnn.moduleName(),
                                provAnn.probeProviderName(),
                                providerClazz);
    }

    public <T> T getProbeProvider(String moduleProviderName, String moduleName,
    		String probeProviderName,
    		Class<T> providerClazz)
            throws InstantiationException, IllegalAccessException {

        FlashlightProbeProvider provider = null;
        try {
            provider = new FlashlightProbeProvider(
            		moduleProviderName, moduleName, probeProviderName, providerClazz);
            //System.out.println("ModuleProviderName= " + moduleProviderName + " \tModule= " + moduleName
            //		+ "\tProbeProviderName= " + probeProviderName + "\tProviderClazz= " + providerClazz.toString());
            for (Method m : providerClazz.getDeclaredMethods()) {
                int sz = m.getParameterTypes().length;
                Probe pnameAnn = m.getAnnotation(Probe.class);
                String probeName = (pnameAnn != null)
                        ? pnameAnn.name() : m.getName();
                boolean self = (pnameAnn != null) ? pnameAnn.self() : false;
                boolean hidden = (pnameAnn != null) ? pnameAnn.hidden() : false;
                String[] probeParamNames = FlashlightUtils.getParamNames(m);
                FlashlightProbe probe = ProbeFactory.createProbe(
                        providerClazz, moduleProviderName, moduleName, probeProviderName, probeName,
                        probeParamNames, m.getParameterTypes(), self, hidden);
                probe.setProviderJavaMethodName(m.getName());
                provider.addProbe(probe);
            }

            handleDTrace(provider);
        
            Class<T> tClazz = providerClazz;

            int mod = providerClazz.getModifiers();
            if (Modifier.isAbstract(mod)) {

                String generatedClassName = provider.getModuleProviderName() +
                        "_Flashlight_" + provider.getModuleName() + "_" + "Probe_" +
                        ((provider.getProbeProviderName() == null) ? providerClazz.getName() : provider.getProbeProviderName());
                generatedClassName = providerClazz.getName() + "_" + generatedClassName;

                try {
                    tClazz = (Class<T>) (providerClazz.getClassLoader()).loadClass(generatedClassName);
                    //System.out.println ("Reusing the Generated class");
                    return (T) tClazz.newInstance();
                } catch (ClassNotFoundException cnfEx) {
                    //Ignore
                }

                ProviderImplGenerator gen = new ProviderImplGenerator();
                generatedClassName = gen.defineClass(provider, providerClazz);

                try {
                    tClazz = (Class<T>) providerClazz.getClassLoader().loadClass(generatedClassName);
                } catch (ClassNotFoundException cnfEx) {
                    throw new RuntimeException(cnfEx);
                }
            }


            ProbeProviderRegistry.getInstance().registerProbeProvider(
                    provider, tClazz);
            T inst = (T) tClazz.newInstance();
            //System.out.println("Created provider successfully....: " + inst.getClass().getName());
            // Notify listeners that a new provider is registered
            //System.out.println("Notify listeners that a new provider is registered");
            
            //Defensive check so that we can unit test this outside of HK2
            if (ppem != null) {
            	//TODO: FIXME: ppem.notifyListenersOnRegister(moduleName, providerName, appName);
            }

        return inst;

        } catch (Exception e) {
            e.printStackTrace();
            return (T) Proxy.newProxyInstance(providerClazz.getClassLoader(),
                new Class[]{providerClazz},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method m, Object[] args) {
                        return null;
                    }
                });
        }

        finally {
            //if(provider != null)
                //allProbeProviders.add(provider);
        }
    }

    public void processXMLProbeProviders(ClassLoader cl, String xml, boolean inBundle) {
        mprint("processProbeProviderXML for " + xml);
        try {
            InputStream is = cl.getResourceAsStream(xml);
            if (inBundle) {
                is = cl.getResourceAsStream(xml);
            } else {
                is = new FileInputStream(xml);
            }
            mprint("InputStream = " + is);
            ProbeProviderXMLParser providerXMLParser = new ProbeProviderXMLParser(is);
            List<ProbeProviderXMLParser.Provider> providers = providerXMLParser.getProviders();
            for (ProbeProviderXMLParser.Provider provider : providers) {
                mprint(provider.toString());
                registerProvider(cl, provider);
}
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }

    }

    @Override
	public String toString() {
		return ObjectAnalyzer.toString(this);
	}


    private void handleDTrace(FlashlightProbeProvider provider) {
        // bnevins:  The way this works above (getProbeProvider()) is that every
        //**method** winds up added to the probe registry with an official ID.
        // I.e. a given provider-class generates possibly many probe objects
        // DTrace has a 1:1 correspondence between provider class and dtrace class imp
        // So we loop through all the probes and add the same DTrace impl object to
        // each probe.
        // We set the DTrace Method object inside the probe just this once to avoid
        // having to discover it anew over and over and over again at runtime...

        DTraceContract dt = FlashlightUtils.getDtraceEngine();

        // is DTrace available and enabled?
        if(dt == null)
            return;

        // here is a way to do the same thing but you get the intermediate interface class
        //Class dtraceProviderInterface = dt.getInterface(provider);
        //Object dtraceProviderImpl = dt.getProvider(dtraceProviderInterface);

        Object dtraceProviderImpl = dt.getProvider(provider);

        // something is wrong with the provider class
        if(dtraceProviderImpl == null) {
            provider.setDTraceInstrumented(false);
            return;
        }

        provider.setDTraceInstrumented(true);
 
         Collection<FlashlightProbe> probes = provider.getProbes();

         for(FlashlightProbe probe : probes) {
             // mf will either find a method or throw an Exception
             DTraceMethodFinder mf = new DTraceMethodFinder(probe, dtraceProviderImpl);
             probe.setDTraceMethod(mf.matchMethod());
             probe.setDTraceProviderImpl(dtraceProviderImpl);
         }

         FlashlightProbeClientMediator.getInstance().registerDTraceListener(provider);
    }

    private void registerProvider(ClassLoader cl, ProbeProviderXMLParser.Provider provider) {

        String moduleProviderName = provider.getModuleProviderName();
        String moduleName = provider.getModuleName();
        String probeProviderName = provider.getProbeProviderName();
        String providerClass = provider.getProbeProviderClass();
        List<ProbeProviderXMLParser.Probe> probes = provider.getProbes();
        Class<?> providerClazz = null;

        try {
            providerClazz = cl.loadClass(providerClass);
            mprint("providerClazz = " + providerClazz);
        } catch (Exception e) {
            mprint( " Could not load the class ( " + providerClazz +
                        " ) for the provider " + providerClass);
            e.printStackTrace();
        }
        mprint("moduleProviderName = " + moduleProviderName);
        mprint("moduleName = " + moduleName);
        mprint("probeProviderName = " + probeProviderName);
        mprint("probeProviderClass = " + providerClass);

        FlashlightProbeProvider flProvider = new FlashlightProbeProvider(
            		moduleProviderName, moduleName, probeProviderName, providerClazz);

        for (ProbeProviderXMLParser.Probe probe : probes) {
            String probeName = probe.getProbeName();
            String probeMethod = probe.getProbeMethod();
            boolean hasSelf = probe.hasSelf();
            boolean isHidden = probe.isHidden();

            boolean errorParsingProbe = false;
            String[] probeParams = new String[probe.getProbeParams().size()];
            Class<?>[] paramTypes = new Class[probe.getProbeParams().size()];

            int i = 0;
            for (ProbeProviderXMLParser.ProbeParam param : probe.getProbeParams()) {
                probeParams[i] = param.getName();
                mprint("          probeParam[" + i + "] = " + probeParams[i]);
                paramTypes[i] = getParamType(cl, param.getType());

                if (paramTypes[i] == null) {
                    // Lets not create a probe if we see a problem with the
                    // paramType resolution
                    errorParsingProbe = true;
                    logger.log(Level.SEVERE,
                            "Cannot resolve the paramTypes, unable to create this probe - " + probeName);
                    // stop parsing anymore probe params
                    break;
                }

                i++;
            }
            if (errorParsingProbe) {
                //reset
                errorParsingProbe = false;
                // continue for the next probe
                continue;
            }
            FlashlightProbe flProbe = ProbeFactory.createProbe( providerClazz,
                    moduleProviderName, moduleName, probeProviderName, probeName,
                    probeParams, paramTypes, hasSelf, isHidden);
            flProbe.setProviderJavaMethodName(probeMethod);
            mprint(" Constructed probe === " + flProbe.toString());
            flProvider.addProbe(flProbe);
        }
        if (flProvider.getProbes().size() == 0)
            return;

        handleDTrace(flProvider);
        allProbeProviders.add(flProvider);
        ProbeProviderRegistry.getInstance().registerProbeProvider(
                flProvider, providerClazz);
        mprint (" Provider registered successfully - " + probeProviderName);
   }


    private Class<?> getParamType(ClassLoader cl, String paramTypeStr) {
        Class<?> paramType = null;

        try {
            // Lets see if this is a primitive type
            Class primType = primTypes.get(paramTypeStr);
            if (primType != null) {
                return primType;
            }
            //Not a primitive type, lets try to load it as is
            paramType = cl.loadClass(paramTypeStr);

        } catch (ClassNotFoundException ex) {
            try {
                // Not a primitive or the actual type, maybe its one of the java.lang.* types
                // try to prepend java.lang. to the given class
                paramType = cl.loadClass("java.lang." + paramTypeStr);
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "Cannot resolve the paramTypes of the probe, tried " + paramTypeStr +
                        " and now the following - Try giving a fully qualified name for the type", e);
            }
        }

        mprint("          paramType = " + paramType);

        return paramType;

    }

    private void mprint(String str) {
        if (debug) {
            System.out.println("APK: " + str);
        }
    }
}

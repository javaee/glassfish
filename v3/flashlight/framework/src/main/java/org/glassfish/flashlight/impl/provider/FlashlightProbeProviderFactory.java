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

import org.glassfish.flashlight.provider.*;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.impl.client.FlashlightProbeClientMediator;
import org.glassfish.flashlight.impl.core.*;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

import org.glassfish.probe.provider.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;

/**
 * @author Mahesh Kannan
 */
@Service
public class FlashlightProbeProviderFactory
        implements ProbeProviderFactory {

    @Inject
    ProbeProviderEventManager ppem;
    
    private ConcurrentHashMap<String, Object> providerInfo = new ConcurrentHashMap<String, Object>();

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

        try {
            FlashlightProbeProvider provider = new FlashlightProbeProvider(
            		moduleProviderName, moduleName, probeProviderName, providerClazz);
            //System.out.println("ModuleProviderName= " + moduleProviderName + " \tModule= " + moduleName
            //		+ "\tProbeProviderName= " + probeProviderName + "\tProviderClazz= " + providerClazz.toString());
            for (Method m : providerClazz.getDeclaredMethods()) {
                int sz = m.getParameterTypes().length;
                Probe pnameAnn = m.getAnnotation(Probe.class);
                String probeName = (pnameAnn != null) 
                        ? pnameAnn.name() : m.getName();
                String[] probeParamNames = new String[sz];
                int index = 0;
                Annotation[][] anns2 = m.getParameterAnnotations();
                for (Annotation[] ann1 : anns2) {
                    for (Annotation ann : ann1) {
                        if (ann instanceof ProbeParam) {
                            ProbeParam pParam = (ProbeParam) ann;
                            probeParamNames[index++] = pParam.value();
                            break;
                        }
                    }
                }

                FlashlightProbe probe = ProbeFactory.createProbe(
                        providerClazz, moduleProviderName, moduleName, probeProviderName, probeName,
                        probeParamNames, m.getParameterTypes());
                probe.setProviderJavaMethodName(m.getName());
                //System.out.println("\tProbe: " + probe);
                provider.addProbe(probe);
            }

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
    }

}

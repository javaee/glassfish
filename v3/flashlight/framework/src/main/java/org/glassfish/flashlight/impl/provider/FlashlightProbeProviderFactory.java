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

import org.glassfish.flashlight.client.EjbContainerListener;
import org.glassfish.flashlight.client.EjbContainerProvider;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.impl.client.FlashlightProbeClientMediator;
import org.glassfish.flashlight.impl.core.*;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.glassfish.flashlight.provider.annotations.ProbeName;
import org.glassfish.flashlight.provider.annotations.ProbeParam;
import org.jvnet.hk2.annotations.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mahesh Kannan
 */
@Service
public class FlashlightProbeProviderFactory
        implements ProbeProviderFactory {

    private ConcurrentHashMap<String, Object> providerInfo = new ConcurrentHashMap<String, Object>();

    public <T> T getProbeProvider(String moduleName, String providerName, String appName, Class<T> providerClazz)
            throws InstantiationException, IllegalAccessException {

        ProbeProvider provider = new ProbeProvider(moduleName, providerName, appName);

        for (Method m : providerClazz.getDeclaredMethods()) {
            int sz = m.getParameterTypes().length;
            ProbeName pnameAnn = m.getAnnotation(ProbeName.class);
            if (pnameAnn == null) {
                continue;
            }
            String probeName = pnameAnn.value();
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

            Probe probe = ProbeFactory.createProbe(
                    moduleName, providerName, appName, probeName,
                    probeParamNames, m.getParameterTypes());
            probe.setProviderJavaMethodName(m.getName());
            System.out.println("\tProbe: " + probe);
            provider.addProbe(probe);
        }


        ProviderImplGenerator gen = new ProviderImplGenerator();
        String genClassName = gen.defineClass(provider, providerClazz);

        Class<T> tClazz = null;
        try {
            tClazz = (Class<T>) Class.forName(genClassName);
        } catch (ClassNotFoundException cnfEx) {
            throw new RuntimeException(cnfEx);
        }


        ProbeProviderRegistry.getInstance().registerProbeProvider(
                provider, tClazz);
        return tClazz.newInstance();

    }

    public static void main(String[] args)
            throws Exception {
        FlashlightProbeProviderFactory factory = new FlashlightProbeProviderFactory();
        EjbContainerProvider<String, Date> provider =
                (EjbContainerProvider<String, Date>) factory.getProbeProvider("ejb", "container", "", EjbContainerProvider.class);

        ComputedParamsHandlerManager cphm =
                ComputedParamsHandlerManager.getInstance();
        cphm.addComputedParamHandler(
                new ComputedParamHandler() {
                    public boolean canHandle(String name) {
                        return "$appName".equals(name);
                    }

                    public Object compute(String name) {
                        return "TestFlashlight";
                    }
                }
        );
        for (Method m : provider.getClass().getDeclaredMethods()) {
            provider.namedEntry(m, "fooBean");
        }

        EjbContainerListener listener = new EjbContainerListener();
        ProbeClientMediator pcm = FlashlightProbeClientMediator.getInstance();
        Collection<ProbeClientMethodHandle> handles = pcm.registerListener(listener);

        System.out.println("Handles.size(): " + handles.size());

        for (Method m : provider.getClass().getDeclaredMethods()) {
            provider.namedEntry(m, "fooBean");
        }


        ProbeClientMethodHandle[] hs = handles.toArray(new ProbeClientMethodHandle[0]);
        hs[0].disable();

        for (Method m : provider.getClass().getDeclaredMethods()) {
            provider.namedEntry(m, "fooBean");
        }

        hs[0].enable();

        for (Method m : provider.getClass().getDeclaredMethods()) {
            provider.namedEntry(m, "fooBean");
        }
    }

}

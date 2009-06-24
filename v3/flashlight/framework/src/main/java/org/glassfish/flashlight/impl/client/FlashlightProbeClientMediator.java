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
package org.glassfish.flashlight.impl.client;

/*
import org.glassfish.gfprobe.client.handler.ProbeClientMethodHandler;
import org.glassfish.gfprobe.common.HandlerRegistry;
*/

import org.glassfish.flashlight.client.*;
import org.glassfish.flashlight.provider.FlashlightProbe;
import org.glassfish.flashlight.provider.ProbeRegistry;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/*
import com.sun.tools.attach.spi.AttachProvider;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.VirtualMachine;
*/

/**
 * @author Mahesh Kannan
 *         Date: Jan 27, 2008
 */
@Service
public class FlashlightProbeClientMediator
        implements ProbeClientMediator, PostConstruct {

    private static ProbeClientMediator _me = new FlashlightProbeClientMediator();

    private AtomicBoolean agentInitialized =
            new AtomicBoolean(false);

    private AtomicInteger clientIdGenerator =
            new AtomicInteger(0);

    private static ConcurrentHashMap<Integer, Object> clients =
            new ConcurrentHashMap<Integer, Object>();
    /*
    private HandlerRegistry _handlerRegistry
            = HandlerRegistry.getInstance();
    */

    Instrumentation inst = null;

    public void postConstruct() {
        _me = this;
    }


    public static ProbeClientMediator getInstance() {
        return _me;
    }

    public static Object getClient(int id) {
        return clients.get(id);
    }

    public synchronized Collection<ProbeClientMethodHandle> registerListener(Object listener) {

        /*
        if (!agentInitialized.get()) {
            synchronized (agentInitialized) {
                if (!agentInitialized.get()) {
                    // The following heavily depends on Java 1.6 code
                    //  For now commented
                    
                    List<AttachProvider> lst = AttachProvider.providers();
                    String myVMId = "";
                    for (AttachProvider p : lst) {
                        if (agentInitialized.get()) {
                            break;
                        }
                        List<VirtualMachineDescriptor> vms = p.listVirtualMachines();
                        for (VirtualMachineDescriptor vmd : vms) {
                            myVMId = vmd.id();
                            System.out.println("***************** Attempting to attach to VM: <" + myVMId + ">");

                            try {
                                VirtualMachine vm = VirtualMachine.attach(myVMId);
                                vm.loadAgent("/space/v3/flashlight/glassfish/modules/flashlight-agent-10.0-SNAPSHOT.jar");
                                System.out.println("***************** DONE INITIALIZING VM: " + myVMId);


                                Class agentClazz = Class.forName("org.glassfish.flashlight.agent.ProbeAgentMain");
                                Method m = agentClazz.getMethod("getInstrumentation()", null);
                                this.inst = (Instrumentation) m.invoke(null);
                                System.out.println("Got Instrumentation: " + inst);
                                agentInitialized.set(true);
                            } catch (Throwable th) {
                                th.printStackTrace();
                            }

                        }
                    }

                    try {
                        Class agentClazz = Class.forName("org.glassfish.flashlight.agent.ProbeAgentMain");
                        Method m = agentClazz.getMethod("getInstrumentation()", null);
                        this.inst = (Instrumentation) m.invoke(null);
                        System.out.println("Got Instrumentation: " + inst);
                        agentInitialized.set(true);
                    } catch (ClassNotFoundException cnfEx) {
                        //TODO
                    } catch (NoSuchMethodException nsmEx) {
                        //TODO
                    } catch (IllegalAccessException iaEx) {
                        //TODO
                    } catch (InvocationTargetException invtEx) {
                        //TODO
                    }
                }
            }
        }
        */

        int clientID = clientIdGenerator.incrementAndGet();
        clients.put(clientID, listener);

        Class clientClz = listener.getClass();

        Collection<ProbeClientMethodHandle> pcms =
                new ArrayList<ProbeClientMethodHandle>();

        for (java.lang.reflect.Method clientMethod : clientClz.getDeclaredMethods()) {
            ProbeListener probeAnn = clientMethod.getAnnotation(ProbeListener.class);

            if (probeAnn != null) {
                String probeStr = probeAnn.value();
                String[] probeDesc = probeStr.split(":");
                FlashlightProbe probe = ProbeRegistry.getInstance().getProbe(probeStr);

                if (probe == null) {
                    throw new RuntimeException("Invalid probe desc: " + probeStr);
                }

                ProbeClientInvoker invoker = ProbeClientInvokerFactory.createInvoker(listener, clientMethod, probe);
                ProbeClientMethodHandleImpl hi = new ProbeClientMethodHandleImpl(
                        invoker.getId(), invoker, probe);
                pcms.add(hi);

                probe.addInvoker(invoker);
            }
        }

        return pcms;
    }

}

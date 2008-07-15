package org.glassfish.flashlight.impl.client;

/*
import org.glassfish.gfprobe.client.handler.ProbeClientMethodHandler;
import org.glassfish.gfprobe.common.HandlerRegistry;
*/

import org.glassfish.flashlight.client.ProbeClientMediator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.instrument.Instrumentation;


import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

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

    private static ProbeClientMediator _me;

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

    public static Object getClient(int id) {
        return clients.get(id);
    }

    public void registerListener(Object listener) {

        if (!agentInitialized.get()) {
            synchronized (agentInitialized) {
                if (!agentInitialized.get()) {
                    /*
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
                    */
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

        /*
        int clientID = clientIdGenerator.incrementAndGet();
        clients.put(clientID, listener);

        Class clientClz = listener.getClass();
        Collection<Class> redefClasses =
                new HashSet<Class>();
        Collection<ProbeClientMethodHandler> pcms =
                new ArrayList<ProbeClientMethodHandler>();
        */

        /*
        for (java.lang.reflect.Method clientMethod : clientClz.getDeclaredMethods()) {
            for (ProbeClientMethodHandler h :
                    _handlerRegistry.getHandlers()) {
                if (h.processClientMethod(clientID,
                        redefClasses, clientMethod)) {
                    //redefClasses.add(clientClz);
                }
            }
        }
        */

        /*
        if (redefClasses.size() >= 0) {
            Class[] clazzez = redefClasses.toArray(new Class[0]);
            try {
                Class agentClz = Class.forName("org.glassfish.gfprobe.agent.ProbeAgentMain");
                Method m = agentClz.getMethod("getInstance", null);
                Object agent = m.invoke(null);
                //Method retransformMethod = agentClz.getMethod("retransform", new Class[] {Class.class});
                System.out.println("agentClazz: " + System.identityHashCode(agentClz));
                //agent.retransform();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        */
    }

}
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

/*
 * Created on Jan 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sun.ejb.containers.interceptors;

import com.sun.ejb.EjbInvocation;
import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.StatelessSessionContainer;
import com.sun.ejb.containers.MessageBeanContainer;
import com.sun.ejb.containers.EJBContextImpl;
import com.sun.enterprise.deployment.*;
import static com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * UserInterceptorsManager manages UserInterceptors. There
 * is one instance of InterceptorManager per container.
 *
 * @author Mahesh Kannan
 */
public class InterceptorManager {

    private BaseContainer container;

    private EjbDescriptor ejbDesc;

    private ClassLoader loader;

    private Class beanClass;

    private String beanClassName;

    private Logger _logger;

    private Class[] interceptorClasses;
    
    private Class[] serializableInterceptorClasses;

    private Map<String, Integer> instanceIndexMap
            = new HashMap<String, Integer>();

    private boolean methodInterceptorsExists;

    private String[] pre30LCMethodNames;

    private Class[] lcAnnotationClasses;

    private CallbackChainImpl[] callbackChain;


    public InterceptorManager(Logger _logger, BaseContainer container,
                              Class[] lcAnnotationClasses, String[] pre30LCMethodNames)
            throws Exception {
        this._logger = _logger;
        this.container = container;
        this.lcAnnotationClasses = lcAnnotationClasses;
        this.pre30LCMethodNames = pre30LCMethodNames;

        ejbDesc = container.getEjbDescriptor();
        loader = container.getClassLoader();
        beanClassName = ejbDesc.getEjbImplClassName();

        this.beanClass = loader.loadClass(beanClassName);
        buildInterceptorChain();
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "InterceptorManager: " + toString());
        }
    }

    public Object[] createInterceptorInstances() {
        int size = serializableInterceptorClasses.length;
        Object[] interceptors = new Object[size];
        for (int index = 0; index < size; index++) {
            Class clazz = serializableInterceptorClasses[index];
            try {
                interceptors[index] = clazz.newInstance();
            } catch (IllegalAccessException illEx) {
                throw new RuntimeException(illEx);
            } catch (InstantiationException instEx) {
                throw new RuntimeException(instEx);
            }
        }

        return interceptors;
    }

    public EjbInvocation.InterceptorChain getAroundInvokeChain(MethodDescriptor mDesc, Method beanMethod) {
        List<EjbInterceptor> list = ejbDesc.getAroundInvokeInterceptors(mDesc);
        ArrayList<AroundInvokeInterceptor> interceptors =
                new ArrayList<AroundInvokeInterceptor>();
        for (EjbInterceptor interceptor : list) {
            String className = interceptor.getInterceptorClassName();
            Set<LifecycleCallbackDescriptor> aroundInvokeDescs = 
                interceptor.getAroundInvokeDescriptors();
            if(aroundInvokeDescs.isEmpty() ) {
                continue;
            }

            List<LifecycleCallbackDescriptor> orderedAIInterceptors =
                new ArrayList<LifecycleCallbackDescriptor>();
            try {
                orderedAIInterceptors = interceptor.getOrderedAroundInvokeDescriptors(loader);
            } catch (Exception e) {
               throw new IllegalStateException("No AroundInvokeIntercetpors found "
                   + " on class " + className, e); 
            }

            Iterator<LifecycleCallbackDescriptor> aiIterator = orderedAIInterceptors.iterator();
            while (aiIterator.hasNext()) {
                LifecycleCallbackDescriptor aroundInvokeDesc = aiIterator.next();
                
                Method method = null;
                try {
                    method = aroundInvokeDesc.getLifecycleCallbackMethodObject(loader);
                } catch(Exception e) {
                   throw new IllegalStateException("No callback method of name " +
                           aroundInvokeDesc.getLifecycleCallbackMethod() 
                   + " found on class " + className, e); 
                }

                if (interceptor.getFromBeanClass()) {
                    interceptors.add(new BeanAroundInvokeInterceptor(method));
                } else {
                    Integer bigInt = instanceIndexMap.get(className);
                    int index = (bigInt == null) ? -1 : bigInt;
                    if (index == -1) {
                        throw new IllegalStateException(getInternalErrorString(className));
                    }
                    Class clazz = interceptorClasses[index];
                    _logger.log(Level.FINE, "*[md.getDeclaredMethod() => " 
                                + method + " FOR CLAZZ: " + clazz);  
                    interceptors.add(new AroundInvokeInterceptor(index, method));
                }
            }
        }

        AroundInvokeInterceptor[] inter = interceptors.toArray(
                new AroundInvokeInterceptor[interceptors.size()]);
        return new AroundInvokeChainImpl(container, inter);
    }

    public boolean hasInterceptors() {
        return this.methodInterceptorsExists;
    }

    public Object intercept(EjbInvocation inv)
            throws Throwable {
        return inv.getInterceptorChain().invokeNext(0, inv);
    }

    public boolean intercept(CallbackType eventType, EJBContextImpl ctx)
            throws Throwable {

        CallbackChainImpl chain = null;
        switch (eventType) {
            case POST_CONSTRUCT:
            case PRE_PASSIVATE:
            case POST_ACTIVATE:
            case PRE_DESTROY:
                chain = callbackChain[eventType.ordinal()];
                CallbackInvocationContext invContext = new
                    CallbackInvocationContext(ctx, chain);
                if (chain != null) {
                    chain.invokeNext(0, invContext);
                }
                break;
            default:
                throw new IllegalStateException("Invalid event type");
        }

        return true;
    }

    private void buildInterceptorChain()
            throws ClassNotFoundException, Exception {
        initInterceptorClassNames();
        initCallbackIndices();
    }

    private void initInterceptorClassNames()
            throws ClassNotFoundException, Exception {
        Set<String> interceptorClassNames = ejbDesc.getInterceptorClassNames();
        int size = interceptorClassNames.size();
        interceptorClasses = new Class[size];
        serializableInterceptorClasses = new Class[size];
        int index = 0;
        for (String className : interceptorClassNames) {
        	Class interClass = loader.loadClass(className);
            interceptorClasses[index] = interClass;
            serializableInterceptorClasses[index] = interClass;
            instanceIndexMap.put(className, index);
            if (!Serializable.class.isAssignableFrom(interClass)) {
                /*TODO
                serializableInterceptorClasses[index] = 
                    EJBUtils.loadGeneratedSerializableClass(loader, className);
                */
            }
            index++;
        }
        methodInterceptorsExists = interceptorClassNames.size() > 0;

        if (ejbDesc.hasAroundInvokeMethod()) {
            methodInterceptorsExists = true;
        }
        instanceIndexMap.put(beanClassName, index++);
    }

    private void initCallbackIndices()
            throws ClassNotFoundException, Exception {

        int size = CallbackType.values().length;
        ArrayList[] callbacks = new ArrayList[size];
        boolean scanFor2xLifecycleMethods = true;

        for (CallbackType eventType : CallbackType.values()) {
            int index = eventType.ordinal();
            callbacks[index] = new ArrayList<CallbackInterceptor>();
            boolean scanForCallbacks = true;
            if (! (ejbDesc instanceof EjbSessionDescriptor)) {
                if ((eventType == CallbackType.PRE_PASSIVATE) ||
                        (eventType == CallbackType.POST_ACTIVATE)) {
                    scanForCallbacks = false;
                }
            }

            if (scanForCallbacks) {
                List<EjbInterceptor> callbackList = ejbDesc.getCallbackInterceptors(eventType);
                for (EjbInterceptor callback : callbackList) {
                    List<CallbackInterceptor> inters = createCallbackInterceptors(eventType, callback);
                    for (CallbackInterceptor inter : inters) {
                        callbacks[index].add(inter);
                    }
                }
            }

            if (callbacks[index].size() > 0) {
                scanFor2xLifecycleMethods = false;
            }
        }

        if (scanFor2xLifecycleMethods) {
            load2xLifecycleMethods(callbacks);
        }
        
        //The next set of lines are to handle the case where
        //  the app doesn't have a @PostConstruct or it 
        //  doesn't implement the EntrerpriseBean interface
        //  In this case we scan for ejbCreate() for MDBs and SLSBs
        boolean lookForEjbCreateMethod =
            (container instanceof StatelessSessionContainer)
            || (container instanceof MessageBeanContainer);
        
        if (lookForEjbCreateMethod) {
            loadOnlyEjbCreateMethod(callbacks);
        }
        
        callbackChain = new CallbackChainImpl[size];
        for (CallbackType eventType : CallbackType.values()) {
            int index = eventType.ordinal();
            CallbackInterceptor[] interceptors = (CallbackInterceptor[])
                    callbacks[index].toArray(new CallbackInterceptor[callbacks[index].size()]);
            callbackChain[index] = new CallbackChainImpl(container, interceptors);
        }

    }

    private List<CallbackInterceptor> createCallbackInterceptors(CallbackType eventType,
                                                          EjbInterceptor inter) throws Exception {
        List<CallbackInterceptor> callbackList = new ArrayList<CallbackInterceptor>();
        
        List<LifecycleCallbackDescriptor> orderedCallbackMethods = 
            inter.getOrderedCallbackDescriptors(eventType, loader);

        String className = inter.getInterceptorClassName();


        for (LifecycleCallbackDescriptor callbackDesc : orderedCallbackMethods) {
            Method method = null;
            try {
                method = callbackDesc.getLifecycleCallbackMethodObject(loader);
            } catch(Exception e) {
                throw new IllegalStateException("No callback method of name " +
                   callbackDesc.getLifecycleCallbackMethod() 
                   + " found on class " + className, e);
	    }


            CallbackInterceptor interceptor = null;
            if (inter.getFromBeanClass()) {
                interceptor = new BeanCallbackInterceptor(method);
            } else {
                Integer bigInt = instanceIndexMap.get(className);
                int index = (bigInt == null) ? -1 : bigInt;
                if (index == -1) {
                    throw new IllegalStateException(getInternalErrorString(className));
                }
                interceptor = new CallbackInterceptor(index, method);
            }
            callbackList.add(interceptor);
        }
        return callbackList;
    }


    private void load2xLifecycleMethods(ArrayList<CallbackInterceptor>[] metaArray) {

        if (javax.ejb.EnterpriseBean.class.isAssignableFrom(beanClass)) {
            int sz = lcAnnotationClasses.length;
            for (int i = 0; i < sz; i++) {
                if (pre30LCMethodNames[i] == null) {
                    continue;
                }
                try {
                    Method method = beanClass.getMethod(
                            pre30LCMethodNames[i], (Class[]) null);
                    if (method != null) {
                        CallbackInterceptor meta =
                                new BeanCallbackInterceptor(method);
                        metaArray[i].add(meta);
                        _logger.log(Level.FINE, "**## bean has 2.x LM: " + meta);
                    }
                } catch (NoSuchMethodException nsmEx) {
                    //TODO: Log exception
                    //Error for a 2.x bean????
                }
            }
        }
    }
    
    //TODO: load2xLifecycleMethods and loadOnlyEjbCreateMethod can be 
    //  refactored to use a common method.
    private void loadOnlyEjbCreateMethod(
            ArrayList<CallbackInterceptor>[] metaArray) {
        int sz = lcAnnotationClasses.length;
        for (int i = 0; i < sz; i++) {
            if (lcAnnotationClasses[i] != PostConstruct.class) {
                continue;
            }

            boolean needToScan = true;
            if (metaArray[i] != null) {
                ArrayList<CallbackInterceptor> al = metaArray[i];
                needToScan =  (al.size() == 0);
            }
            
            if (! needToScan) {
                // We already have found a @PostConstruct method
                // So just ignore any ejbCreate() method
                break;
            } else {
                try {
                    Method method = beanClass.getMethod(pre30LCMethodNames[i],
                            (Class[]) null);
                    if (method != null) {
                        CallbackInterceptor meta = new BeanCallbackInterceptor(
                                method);
                        metaArray[i].add(meta);
                        _logger.log(Level.FINE,
                                "**##[ejbCreate] bean has 2.x style ejbCreate: " + meta);
                    }
                } catch (NoSuchMethodException nsmEx) {
                    // TODO: Log exception
                    //Error for a 2.x bean????
                }
            }
        }
    }
    

    public String toString() {
        StringBuilder sbldr = new StringBuilder();
        sbldr.append("##########################################################\n");
        sbldr.append("InterceptorManager<").append(beanClassName).append("> has ")
                .append(interceptorClasses.length).append(" interceptors");
        sbldr.append("\n\tbeanClassName: ").append(beanClassName);
        sbldr.append("\n\tInterceptors: ");
        for (Class clazz : interceptorClasses) {
            sbldr.append("\n\t\t").append(clazz.getName());
        }
        sbldr.append("\n\tCallback Interceptors: ");
        for (int i = 0; i < lcAnnotationClasses.length; i++) {
            CallbackChainImpl chain = callbackChain[i];
            sbldr.append("\n\t").append(i)
                    .append(": ").append(lcAnnotationClasses[i]);
            sbldr.append("\n\t\t").append(chain.toString());
        }
        sbldr.append("\n");
        sbldr.append("##########################################################\n");
        return sbldr.toString();
    }

    private String getInternalErrorString(String className) {
        StringBuilder sbldr = new StringBuilder("Internal error: ");
        sbldr.append(" className: ").append(className)
                .append(" is neither a bean class (")
                .append(beanClassName).append(") nor an ")
                .append("interceptor class (");
        for (Class cn : interceptorClasses) {
            sbldr.append(cn.getName()).append("; ");
        }
        sbldr.append(")");
        _logger.log(Level.INFO, "++ : " + sbldr.toString());
        return sbldr.toString();
    }

}

class AroundInvokeChainImpl
        implements EjbInvocation.InterceptorChain {
    enum ChainType {
        METHOD, CALLBACK}

    ;

    protected BaseContainer container;
    protected AroundInvokeInterceptor[] interceptors;
    protected int size;

    protected AroundInvokeChainImpl(BaseContainer container,
                                    AroundInvokeInterceptor[] interceptors) {
        this.container = container;
        this.interceptors = interceptors;
        this.size = (interceptors == null) ? 0 : interceptors.length;
    }

    public Object invokeNext(int index, EjbInvocation inv)
            throws Throwable {
        return (index < size)
                ? interceptors[index].intercept(inv)
                : container.invokeBeanMethod(inv);
    }

    public String toString() {
        StringBuilder bldr = new StringBuilder();
        for (AroundInvokeInterceptor inter : interceptors) {
            bldr.append("\n\t").append(inter);
        }

        return bldr.toString();
    }
}

class AroundInvokeInterceptor {
    protected int index;
    protected Method method;

    AroundInvokeInterceptor(int index, Method method) {
        this.index = index;
        this.method = method;

        try {
            final Method finalM = method;
            if(System.getSecurityManager() == null) {
                if (!finalM.isAccessible()) {
                    finalM.setAccessible(true);
                }
            } else {
                java.security.AccessController
                        .doPrivileged(new java.security.PrivilegedExceptionAction() {
                    public java.lang.Object run() throws Exception {
                        if (!finalM.isAccessible()) {
                            finalM.setAccessible(true);
                        }
                        return null;
                    }});
            }
        } catch(Exception e) {
            throw new EJBException(e);
        }

    }

    Object intercept(final EjbInvocation invCtx) throws Throwable {
        try {
            final Object[] interceptors = ((EJBContextImpl) invCtx.context)
                    .getInterceptorInstances();

            if( System.getSecurityManager() != null ) {
            // Wrap actual value insertion in doPrivileged to
            // allow for private/protected field access.
            return java.security.AccessController
                    .doPrivileged(new java.security.PrivilegedExceptionAction() {
                        public java.lang.Object run() throws Exception {
                            return method.invoke(interceptors[index], invCtx);
                        }
                    });
            } else {

                 return method.invoke(interceptors[index], invCtx);

            }
        } catch (java.lang.reflect.InvocationTargetException invEx) {
            throw invEx.getCause();
        } catch (java.security.PrivilegedActionException paEx) {
            Throwable th = paEx.getCause();
            if (th.getCause() != null) {
                throw th.getCause();
            }
            throw th;
        }
    }

    public String toString() {
        return "[" + index + "]: " + method;
    }

}

class BeanAroundInvokeInterceptor
        extends AroundInvokeInterceptor {
    private static final Object[] NULL_ARGS = null;

    BeanAroundInvokeInterceptor(Method method) {
        super(-1, method);
    }

    Object intercept(final EjbInvocation invCtx) throws Throwable {
        try {

            if( System.getSecurityManager() != null ) {
            // Wrap actual value insertion in doPrivileged to
            // allow for private/protected field access.
            return java.security.AccessController
                    .doPrivileged(new java.security.PrivilegedExceptionAction() {
                        public java.lang.Object run() throws Exception {
                            return method.invoke(invCtx.getTarget(), invCtx);
                        }
                    });
            } else {
                return method.invoke(invCtx.getTarget(), invCtx);
            }
        } catch (java.lang.reflect.InvocationTargetException invEx) {
            throw invEx.getCause();
        } catch (java.security.PrivilegedActionException paEx) {
            Throwable th = paEx.getCause();
            if (th.getCause() != null) {
                throw th.getCause();
            }
            throw th;
        }
    }
}

class CallbackInterceptor {
    protected int index;
    protected Method method;

    CallbackInterceptor(int index, Method method) {
        this.index = index;
        this.method = method;

        try {
        final Method finalM = method;
        if(System.getSecurityManager() == null) {
            if (!finalM.isAccessible()) {
                finalM.setAccessible(true);
            }
        } else {
            java.security.AccessController
                    .doPrivileged(new java.security.PrivilegedExceptionAction() {
                public java.lang.Object run() throws Exception {
                    if (!finalM.isAccessible()) {
                        finalM.setAccessible(true);
                    }
                    return null;
                }});
        }
        } catch(Exception e) {
            throw new EJBException(e);
        }

    }

    Object intercept(final CallbackInvocationContext invContext) 
        throws Throwable {
        try {
            EJBContextImpl ejbContextImpl = (EJBContextImpl)
                invContext.getEJBContext();
            final Object[] interceptors = ejbContextImpl
                    .getInterceptorInstances();

            if( System.getSecurityManager() != null ) {
            // Wrap actual value insertion in doPrivileged to
            // allow for private/protected field access.
            return java.security.AccessController
                    .doPrivileged(new java.security.PrivilegedExceptionAction() {
                        public java.lang.Object run() throws Exception {
                            return method.invoke(interceptors[index],
                                                 invContext);
                        }
                    });
            } else {
                return method.invoke(interceptors[index], invContext);
                                     
            }
        } catch (java.lang.reflect.InvocationTargetException invEx) {
            throw invEx.getCause();
        } catch (java.security.PrivilegedActionException paEx) {
            Throwable th = paEx.getCause();
            if (th.getCause() != null) {
                throw th.getCause();
            }
            throw th;
        }
    }

    public String toString() {
        return "callback[" + index + "]: " + method;
    }
}

class BeanCallbackInterceptor
        extends CallbackInterceptor {
    private static final Object[] NULL_ARGS = null;

    BeanCallbackInterceptor(Method method) {
        super(-1, method);
    }

    Object intercept(final CallbackInvocationContext invContext) 
        throws Throwable {
        try {

            if( System.getSecurityManager() != null ) {
            // Wrap actual value insertion in doPrivileged to
            // allow for private/protected field access.
               java.security.AccessController
                    .doPrivileged(new java.security.PrivilegedExceptionAction() {
                        public java.lang.Object run() throws Exception {

                            method.invoke(invContext.getTarget(),
                                          NULL_ARGS);
                            return null;

                        }
                    });
            } else {
                method.invoke(invContext.getTarget(), NULL_ARGS);
            }

            return invContext.proceed();

        } catch (java.lang.reflect.InvocationTargetException invEx) {
            throw invEx.getCause();
        } catch (java.security.PrivilegedActionException paEx) {
            Throwable th = paEx.getCause();
            if (th.getCause() != null) {
                throw th.getCause();
            }
            throw th;
        }
    }

    public String toString() {
        return "beancallback[" + index + "]: " + method;
    }
}


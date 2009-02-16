
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
package com.sun.ejb;

//XXX: import javax.xml.rpc.handler.MessageContext;
/* HARRY : JACC Changes */

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.ejb.containers.EJBLocalRemoteObject;
import com.sun.ejb.containers.EjbFutureTask;
import org.glassfish.api.invocation.ComponentInvocation;
import com.sun.enterprise.transaction.spi.TransactionOperationsManager;

import javax.ejb.EJBContext;
import javax.interceptor.InvocationContext;
import javax.transaction.Transaction;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceContext;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The EjbInvocation object contains state associated with an invocation
 * on an EJB or EJBHome (local/remote). It is usually created by generated code
 * in *ObjectImpl and *HomeImpl classes. It is passed as a parameter to
 * Container.preInvoke() * and postInvoke(), which are called by the
 * EJB(Local)Object/EJB(Local)Home before and after an invocation.
 */

public class EjbInvocation
    extends ComponentInvocation
    implements InvocationContext, TransactionOperationsManager, Cloneable
{
  
    private static Map<Class, Set<Class>> compatiblePrimitiveWrapper
        = new HashMap<Class, Set<Class>>();

    static {
        
        Set<Class> smallerPrimitiveWrappers = null;
        
        smallerPrimitiveWrappers = new HashSet<Class>();
        smallerPrimitiveWrappers.add(Byte.class);
        compatiblePrimitiveWrapper.put(byte.class, smallerPrimitiveWrappers);
        
        smallerPrimitiveWrappers = new HashSet<Class>();
        smallerPrimitiveWrappers.add(Boolean.class);
        compatiblePrimitiveWrapper.put(boolean.class, smallerPrimitiveWrappers);
        
        smallerPrimitiveWrappers = new HashSet<Class>();
        smallerPrimitiveWrappers.add(Character.class);
        compatiblePrimitiveWrapper.put(char.class, smallerPrimitiveWrappers);
        
        smallerPrimitiveWrappers = new HashSet<Class>();
        smallerPrimitiveWrappers.add(Byte.class);
        smallerPrimitiveWrappers.add(Short.class);
        smallerPrimitiveWrappers.add(Integer.class);
        smallerPrimitiveWrappers.add(Float.class);
        smallerPrimitiveWrappers.add(Double.class);
        compatiblePrimitiveWrapper.put(double.class, smallerPrimitiveWrappers);
        
        smallerPrimitiveWrappers = new HashSet<Class>();
        smallerPrimitiveWrappers.add(Byte.class);
        smallerPrimitiveWrappers.add(Short.class);
        smallerPrimitiveWrappers.add(Integer.class);
        smallerPrimitiveWrappers.add(Float.class);
        compatiblePrimitiveWrapper.put(float.class, smallerPrimitiveWrappers);
        
        smallerPrimitiveWrappers = new HashSet<Class>();
        smallerPrimitiveWrappers.add(Byte.class);
        smallerPrimitiveWrappers.add(Short.class);
        smallerPrimitiveWrappers.add(Integer.class);
        compatiblePrimitiveWrapper.put(int.class, smallerPrimitiveWrappers);
        
        smallerPrimitiveWrappers = new HashSet<Class>();
        smallerPrimitiveWrappers.add(Byte.class);
        smallerPrimitiveWrappers.add(Short.class);
        smallerPrimitiveWrappers.add(Integer.class);
        smallerPrimitiveWrappers.add(Long.class);
        compatiblePrimitiveWrapper.put(long.class, smallerPrimitiveWrappers);
        
        smallerPrimitiveWrappers = new HashSet<Class>();
        smallerPrimitiveWrappers.add(Byte.class);
        smallerPrimitiveWrappers.add(Short.class);
        compatiblePrimitiveWrapper.put(short.class, smallerPrimitiveWrappers);
    }

    public ComponentContext context;
    
    EjbInvocation(String compEnvId, Container container) {
        super.componentId = compEnvId;
        super.container = container;
        super.setComponentInvocationType(ComponentInvocation.ComponentInvocationType.EJB_INVOCATION);
    }

    /**
     * The EJBObject/EJBLocalObject which created this EjbInvocation object.
     * This identifies the target bean.
     */
    public EJBLocalRemoteObject ejbObject;
    
    /**
     * Local flag: true if this invocation was made through an ejb local 
     * interface or a local business interface.
     */
    public boolean isLocal=false;
    
    /**
     * InvocationInfo object caches information about the current method
     */
    public InvocationInfo invocationInfo;
    
    /**
     * True if this invocation was made through a local business interface or
     * a remote business interface.
     */
    public boolean isBusinessInterface;

    /**
     * true if this is a web service invocation
     */
    public boolean isWebService=false;
    
    /**
     * true if this is a message-driven bean invocation
     */
    public boolean isMessageDriven=false;
    
    /**
     * true if this is an invocation on the home object
     * this is required for jacc.
     */
    public boolean isHome=false;

    /** 
     * Home, Remote, LocalHome, Local, WebService, or business interface
     * through which a synchronous ejb invocation was made.
     */
    public Class clientInterface;
    
    /**
     * Method to be invoked. This is a method of the EJB's local/remote
     * component interface for invocations on EJB(Local)Objects,
     * or of the local/remote Home interface
     * for invocations on the EJBHome.
     * Set by the EJB(Local)Object/EJB(Local)Home before calling
     * Container.preInvoke().
     */
    public java.lang.reflect.Method method;
    
    /**
     * The EJB instance to be invoked.
     * Set by Container and used by EJBObject/EJBHome.
     */
    public Object ejb;

    /**
     * This reflects any exception that has occurred during this invocation,
     * including preInvoke, bean method execution, and postInvoke.
     */
    public Throwable exception;

    /**
     * Set to any exception directly thrown from bean method invocation,
     * which could be either an application exception or a runtime exception.
     * This is set *in addition to* the this.exception field.  Some container
     * processing logic, e.g. @Remove, depends specifically on whether a
     * bean method threw an exception.  
     */
    public Throwable exceptionFromBeanMethod;
    
    
    /**
     * The client's transaction if any.
     * Set by the Container during preInvoke() and used by the Container
     * during postInvoke().
     */
    public Transaction clientTx;
    
    /**
     * The EJBContext object of the bean instance being invoked.
     * Set by the Container during preInvoke() and used by the Container
     * during postInvoke().
     */
    // Moved to com/sun/enterprise/ComponentInvocation
    // public ComponentContext context;
    
    /**
     * The transaction attribute of the bean method. Set in generated
     * EJBObject/Home/LocalObject/LocalHome class.
     */
    public int transactionAttribute;
    
    /**
     * The security attribute of the bean method. Set in generated
     * EJBObject/Home/LocalObject/LocalHome class.
     */
    public int securityPermissions;
    
    
    /**
     * Used by MessageBeanContainer.  true if container started
     * a transaction for this invocation.
     */
    public boolean containerStartsTx;
    
    /**
     * Used by MessageBeanContainer to keep track of the context class
     * loader that was active before message delivery began.
     */
    public ClassLoader originalContextClassLoader;
    
    /**
     * Used for web service invocations to hold SOAP message context.
     * EJBs can access message context through SessionContext.
     */
	/* HARRY: JACC Related Changes */
     public MessageContext messageContext;
    
    /**
     * Used for JACC PolicyContextHandlers. The handler can query the container
     * back for parameters on the ejb. This is set during the method invocation
     * and is not available for preInvoke calls.
     */
    public Object[] methodParams;


    /**
     * Result of txManager.getStatus() performed at the beginning of
     * BaseContainer.preInvoke() and valid up until preinvokeTx().
     * txManager.getStatus() accesses a thread-local which is an 
     * expensive operation.  Storing status in the invocation makes it
     * easier for some of the other early pre-invoke operations to
     * re-use it.  
     */
    private Integer preInvokeTxStatus;

    /**
     * Tells if a CMP2.x bean was found in the Tx cache. Applicable
     * only for CMP2.x beans
     */
    public boolean foundInTxCache = false;

    /**
     * Tells if a fast path can be taken for a business method
     * invocation.
     */
    public boolean useFastPath = false;
  
    private java.util.concurrent.locks.Lock cmcLock;

    private boolean doTxProcessingInPostInvoke;

    private long invId;

    private boolean yetToSubmitStatus = true;

    private EjbFutureTask asyncFuture;

    private boolean wasCancelCalled = false;

    public EjbFutureTask getEjbFutureTask() {
        return asyncFuture;
    }

    public void setEjbFutureTask(EjbFutureTask future) {
        asyncFuture = future;
    }

    public void setWasCancelCalled(boolean flag) {
        wasCancelCalled = flag;
    }

    public boolean getWasCancelCalled() {
        return wasCancelCalled;
    }

    public long getInvId() {
        return invId;
    }

    public void setInvId(long invId) {
        this.invId = invId;
    }

    public boolean mustInvokeAsynchronously() {
        return invocationInfo.isAsynchronous() && yetToSubmitStatus;
    }

    public void clearYetToSubmitStatus() {
        yetToSubmitStatus = false;
    }

    public boolean getDoTxProcessingInPostInvoke() {
        return doTxProcessingInPostInvoke;
    }

    public void setDoTxProcessingInPostInvoke(boolean doTxProcessingInPostInvoke) {
        this.doTxProcessingInPostInvoke = doTxProcessingInPostInvoke;
    }

    public EjbInvocation clone() {
        EjbInvocation newInv = (EjbInvocation) super.clone();

        newInv.ejb = null;
        newInv.exception = null;
        newInv.exceptionFromBeanMethod = null;
        newInv.clientTx = null;
        newInv.preInvokeTxStatus = null;
        newInv.originalContextClassLoader = null;
        
        return newInv;
    }

    /**
     * Used by JACC implementation to get an enterprise bean
     * instance for the EnterpriseBean policy handler.  The jacc
     * implementation should use this method rather than directly
     * accessing the ejb field.
     */
    public Object getJaccEjb() {
        Object bean = null;
        if( container != null ) {
            bean = ((Container) container).getJaccEjb(this);
        }
        return bean;
    }
    
    /**
     * This method returns the method interface constant for this EjbInvocation.
     */
    public String getMethodInterface() {
        if (isWebService) {
            return MethodDescriptor.EJB_WEB_SERVICE;
        } else if (isMessageDriven) {
            return MethodDescriptor.EJB_BEAN;
        } else if (isLocal) {
            return (isHome) ? MethodDescriptor.EJB_LOCALHOME :
                    MethodDescriptor.EJB_LOCAL;
        } else {
            return (isHome) ? MethodDescriptor.EJB_HOME :
                    MethodDescriptor.EJB_REMOTE;
        }
    }

    /**
     * Returns CachedPermission associated with this invocation, or
     * null if not available.
     */
    public Object getCachedPermission() {
        return (invocationInfo != null) ? invocationInfo.cachedPermission :
            null;
    }

    /**
     * @return Returns the ejbCtx.
     */
    public EJBContext getEJBContext() {
        return (EJBContext) this.context;
    }

    public Integer getPreInvokeTxStatus() {
        return preInvokeTxStatus;
    }
    
    public void setPreInvokeTxStatus(Integer txStatus) {
        // Can be null, which means preInvokeTxStatus is no longer applicable.
        preInvokeTxStatus = txStatus;
    }

    public java.util.concurrent.locks.Lock getCMCLock() {
        return cmcLock;
    }

    public void setCMCLock(java.util.concurrent.locks.Lock l) {
        cmcLock = l;
    }

    @Override
    public Object getTransactionOperationsManager() {
        return this;
    }

    //Implementation of TransactionOperationsManager methods
    
    /**
     * Called by the UserTransaction implementation to verify 
     * access to the UserTransaction methods.
     */
    public boolean userTransactionMethodsAllowed() {
        return ((Container) container).userTransactionMethodsAllowed(this);
    }

    /**
     * Called by the UserTransaction when transaction is started.
     */
    public void doAfterUtxBegin() {
        ((Container) container).doAfterBegin(this);
    }

    //Implementation of InvocationContext methods
    
    private int interceptorIndex;

    public Method   beanMethod;

    // Only set for web service invocations.
    private WebServiceContext webServiceContext;

    // Only set for EJB JAXWS
    //FIXME: private Message message = null;
    private Object message;

    private SOAPMessage soapMessage = null;

    private Map      contextData;

    public InterceptorChain getInterceptorChain() {
        return (invocationInfo == null)
            ? null : invocationInfo.interceptorChain;
    }

    /**
     * @return Returns the bean instance.
     */
    public Object getTarget() {
        return this.ejb;
    }
 
    
    /**
     * @return For AroundInvoke methods, returns the bean class 
     *         method being invoked.  For lifecycle callback methods, 
     *         returns null.
     */
    public Method getMethod() {
        return getBeanMethod();
    }
    public Method getBeanMethod() {
        return this.beanMethod;
    }

    /**
     * @return Returns the parameters that will be used to invoke
     * the business method.  If setParameters has been called, 
     * getParameters() returns the values to which the parameters 
     * have been set.
     */
    public Object[] getParameters() {
        return this.methodParams;
    }
    
    /**
     * Set the parameters that will be used to invoke the business method.
     *
     */
    public void setParameters(Object[] params) {
        Method method = getMethod();
        if (method != null) {
            Class[] paramTypes = method.getParameterTypes();
            if ((params == null) && (paramTypes.length != 0)) {
                throw new IllegalArgumentException("Wrong number of parameters for "
                        + " method: " + method);
            }
            if (paramTypes.length != params.length) {
                throw new IllegalArgumentException("Wrong number of parameters for "
                        + " method: " + method);
            }
            int index = 0 ;
            for (Class type : paramTypes) {
                if (params[index] == null) {
                    if (type.isPrimitive()) {
                        throw new IllegalArgumentException("Parameter type mismatch for method "
                                + method.getName() + ".  Attempt to set a null value for Arg["
                            + index + "]. Expected a value of type: " + type.getName());
                    }
                } else if (type.isPrimitive()) {
                    Set<Class> compatibles = compatiblePrimitiveWrapper.get(type);
                    if (! compatibles.contains(params[index].getClass())) {
                        throw new IllegalArgumentException("Parameter type mismatch for method "
                                + method.getName() + ".  Arg["
                            + index + "] type: " + params[index].getClass().getName()
                            + " is not compatible with the expected type: " + type.getName());   
                    }
                } else if (! type.isAssignableFrom(params[index].getClass())) {
                    throw new IllegalArgumentException("Parameter type mismatch for method "
                            + method.getName() + ".  Arg["
                        + index + "] type: " + params[index].getClass().getName()
                        + " does not match the expected type: " + type.getName());   
                }
                index++;
            }
        } else {
            throw new IllegalStateException("Internal Error: Got null method");
        }
        this.methodParams = params;
    }
    
    //The following method is not part of InvocationContext interface
    //  but needed for JAXWS message context propagation
    public void setContextData(WebServiceContext context) {
        this.webServiceContext = context;
    }
    
    /**
     * @return Returns the contextMetaData.
     */
    public Map<String, Object> getContextData() {
        if (this.contextData == null) {
            if (webServiceContext != null)
                this.contextData = webServiceContext.getMessageContext();
            else
                this.contextData = new HashMap<String, Object>();
        }
        return contextData;
    }

    /**
     * This is for EJB JAXWS only.
     * @param message  an unconsumed message
     */
    public <T> void setMessage(T message) {
        this.message = message;
    }

    /**
     * This is for EJB JAXWS only.
     */
    public SOAPMessage getSOAPMessage() {
        if (message != null && soapMessage == null) {
            try {
                //FIXME: soapMessage = message.readAsSOAPMessage();
                soapMessage = (SOAPMessage) message;
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
            //message consumed, set it to null
            message = null;
        }
        return soapMessage;
    }

    /* (non-Javadoc)
     * @see javax.interceptor.InvocationContext#proceed()
     */
    public Object proceed()
        throws Exception
    {
        try {
            //TODO: Internal error if getInterceptorChain() is null
            interceptorIndex++;
            return getInterceptorChain().invokeNext(interceptorIndex, this);
        } catch (Exception ex) {
            throw ex;
        } catch (Throwable th) {
            throw new Exception(th);
        } finally {
            interceptorIndex--;
        }
    }

    /*********************************************************/

    public static interface InterceptorChain {
	public Object invokeNext(int index, EjbInvocation invCtx)
	    throws Throwable;
    }

}


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
package com.sun.enterprise.deployment;

import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.internal.api.Globals;

import java.util.*;
import java.lang.reflect.Method;

/**
    * Objects of this kind represent the deployment information describing a single 
    * Session Ejb, either stateful or stateless.
    *@author Danny Coward
    */

public class EjbSessionDescriptor extends EjbDescriptor {
    private boolean isStateless = false;
    private int timeout = 0;

    private Set<LifecycleCallbackDescriptor> postActivateDescs =
        new HashSet<LifecycleCallbackDescriptor>();
    private Set<LifecycleCallbackDescriptor> prePassivateDescs =
        new HashSet<LifecycleCallbackDescriptor>();

    // For EJB 3.0 stateful session beans, information about the assocation
    // between a business method and bean removal.
    private Map<MethodDescriptor, EjbRemovalInfo> removeMethods
        = new HashMap<MethodDescriptor, EjbRemovalInfo>();

    // For EJB 3.0 stateful session beans with adapted homes, list of
    // business methods corresponding to Home/LocalHome create methods.
    private Set<EjbInitInfo> initMethods=new HashSet<EjbInitInfo>();

    private Method afterBeginMethod = null;
    private Method beforeCompletionMethod = null;
    private Method afterCompletionMethod = null;

    /** The Session type String.*/
    public final static String TYPE = "Session";
    /** The String to indicate stalessness. */
    public final static String STATELESS = "Stateless";
    /** Idicates statefullness of a session ejb.*/
    public final static String STATEFUL = "Stateful";
    
    private static LocalStringManagerImpl localStrings =
	    new LocalStringManagerImpl(EjbSessionDescriptor.class); 

    /**
	*  Default constructor.
	*/
    public EjbSessionDescriptor() {
    }
    
    /** 
    * The copy constructor.
    */
    
    public EjbSessionDescriptor(EjbDescriptor other) {
	super(other);
	if (other instanceof EjbSessionDescriptor) {
	    EjbSessionDescriptor session = (EjbSessionDescriptor) other;
	    this.isStateless = session.isStateless;
	    this.timeout = session.timeout;
	}
    }
    
	/**
	* Returns the type of this bean - always "Session".
	*/
    public String getType() {
	return TYPE;
    }
    
    /**
    * Returns the string STATELESS or STATEFUL according as to whether
    * the bean is stateless or stateful.
    **/
    
    public String getSessionType() {
	if (this.isStateless()) {
	    return STATELESS;
	} else {
	    return STATEFUL;
	}
    }
    
	/** 
	* Accepts the Strings STATELESS or STATEFUL.
	*/
    public void setSessionType(String sessionType) {
	if (STATELESS.equals(sessionType)) {
	    this.setStateless(true);
	    return;
	}
	if (STATEFUL.equals(sessionType)) {
	    this.setStateless(false);
	    return;
	}
	if (this.isBoundsChecking()) {
	    throw new IllegalArgumentException(localStrings.getLocalString(
									   "enterprise.deployment.exceptionsessiontypenotlegaltype",
									   "{0} is not a legal session type for session ejbs. The type must be {1} or {2}", new Object[] {sessionType, STATEFUL, STATELESS}));
	}
    }
    
	/**
	* Sets my type
	*/
    public void setType(String type) {
	throw new IllegalArgumentException(localStrings.getLocalString(
								   "enterprise.deployment.exceptioncannotsettypeofsessionbean",
								   "Cannot set the type of a session bean"));
    }
    
	/**
	* Sets the timeout value for this session bean. 
	*/

    public void setTimeout(int timeout) {
	this.timeout = timeout;
    }
    
	/**
	* Returns the timeout value of this bean. 
	*/
    public int getTimeout() {
	return this.timeout;
    }
    
	/**
	*  Sets the transaction type for this bean. Must be either BEAN_TRANSACTION_TYPE or CONTAINER_TRANSACTION_TYPE.
	*/
    public void setTransactionType(String transactionType) {
	boolean isValidType = (BEAN_TRANSACTION_TYPE.equals(transactionType) ||
				CONTAINER_TRANSACTION_TYPE.equals(transactionType));
				
	if (!isValidType && this.isBoundsChecking()) {
	    throw new IllegalArgumentException(localStrings.getLocalString(
									   "enterprise.deployment..exceptointxtypenotlegaltype",
									   "{0} is not a legal transaction type for session beans", new Object[] {transactionType}));
	} else {
	    super.transactionType = transactionType;
	    super.setMethodContainerTransactions(new Hashtable());

	}
    }
    
	/**
	* Returns true if I am describing a stateless session bean.
	*/
    public boolean isStateless() {
	return isStateless;
    }
    
    public boolean isStateful() {
        return !isStateless();
    }
    
	/**
	* Sets the isStateless attribute of this session bean.
	*/
    public void setStateless(boolean isStateless) {
	this.isStateless = isStateless;

    }

    public boolean hasRemoveMethods() {
        return (!removeMethods.isEmpty());
    }

    /**
     * @return remove method info for the given method or null if the
     * given method is not a remove method for this stateful session bean.
     */
    public EjbRemovalInfo getRemovalInfo(MethodDescriptor method) {
        // first try to find the exact match
        for (MethodDescriptor methodDesc : removeMethods.keySet()) {
            if (methodDesc.equals(method)) {
                return removeMethods.get(methodDesc);
            }
        }

        // if nothing is found, try to find the loose match
        for (MethodDescriptor methodDesc : removeMethods.keySet()) {
            if (methodDesc.implies(method)) {
                return removeMethods.get(methodDesc);
            }
        }

        return null;
    }

    public Set<EjbRemovalInfo> getAllRemovalInfo() {
        return new HashSet<EjbRemovalInfo>(removeMethods.values());
    }

    public void addRemoveMethod(EjbRemovalInfo removalInfo) {
        removeMethods.put(removalInfo.getRemoveMethod(), removalInfo);
    }

    public boolean hasInitMethods() {
        return (!initMethods.isEmpty());
    }

    public Set<EjbInitInfo> getInitMethods() {
        return new HashSet<EjbInitInfo>(initMethods);
    }

    public void addInitMethod(EjbInitInfo initInfo) {
        initMethods.add(initInfo);
    }
    
    public Set<LifecycleCallbackDescriptor> getPostActivateDescriptors() {
        if (postActivateDescs == null) {
            postActivateDescs = 
                new HashSet<LifecycleCallbackDescriptor>(); 
        }
        return postActivateDescs;
    }   
            
    public void addPostActivateDescriptor(LifecycleCallbackDescriptor
        postActivateDesc) {
        String className = postActivateDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next :
             getPostActivateDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getPostActivateDescriptors().add(postActivateDesc);
        }
    }

    public LifecycleCallbackDescriptor 
        getPostActivateDescriptorByClass(String className) {

        for (LifecycleCallbackDescriptor next :
                 getPostActivateDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                return next;
            }
        }
        return null;
    }

    public boolean hasPostActivateMethod() {
        return (getPostActivateDescriptors().size() > 0);
    }

    public Set<LifecycleCallbackDescriptor> getPrePassivateDescriptors() {
        if (prePassivateDescs == null) {
            prePassivateDescs = 
                new HashSet<LifecycleCallbackDescriptor>(); 
        }
        return prePassivateDescs;
    }   
            
    public void addPrePassivateDescriptor(LifecycleCallbackDescriptor
        prePassivateDesc) {
        String className = prePassivateDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next :
             getPrePassivateDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getPrePassivateDescriptors().add(prePassivateDesc);
        }
    }

    public LifecycleCallbackDescriptor 
        getPrePassivateDescriptorByClass(String className) {

        for (LifecycleCallbackDescriptor next :
                 getPrePassivateDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                return next;
            }
        }
        return null;
    }

    public boolean hasPrePassivateMethod() {
        return (getPrePassivateDescriptors().size() > 0);
    }

    public Vector getPossibleTransactionAttributes() {
        Vector txAttributes = super.getPossibleTransactionAttributes();

        // Session beans that implement SessionSynchronization interface
        // have a limited set of possible transaction attributes.
        if( isStateful() ) {
            try {
                EjbBundleDescriptor ejbBundle = getEjbBundleDescriptor();

                ClassLoader classLoader = ejbBundle.getClassLoader();
                Class ejbClass = classLoader.loadClass(getEjbClassName());

                AnnotationTypesProvider provider = Globals.getDefaultHabitat().getComponent(AnnotationTypesProvider.class, "EJB");
                if (provider!=null) {
                    Class sessionSynchClass = provider.getType("javax.ejb.SessionSynchronization");
                    if( sessionSynchClass.isAssignableFrom(ejbClass) ) {
                        txAttributes = new Vector();
                        txAttributes.add(new ContainerTransaction
                            (ContainerTransaction.REQUIRED, ""));
                        txAttributes.add(new ContainerTransaction
                            (ContainerTransaction.REQUIRES_NEW, ""));
                        txAttributes.add(new ContainerTransaction
                            (ContainerTransaction.MANDATORY, ""));
                    }
                }
            } catch(Exception e) {
                // Don't treat this as a fatal error.  Just return full
                // set of possible transaction attributes.
            }
        }
        return txAttributes;
    }

    /**
     * Set the Method annotated @AfterBegin.
     */
    public void setAfterBeginMethod(Method m) {
        afterBeginMethod = m;
    }
    
    /**
     * Returns the Method annotated @AfterBegin.
     */
    public Method getAfterBeginMethod() {
        return afterBeginMethod;
    }
    
    /**
     * Set the Method annotated @BeforeCompletion.
     */
    public void setBeforeCompletionMethod(Method m) {
        beforeCompletionMethod = m;
    }
    
    /**
     * Returns the Method annotated @AfterBegin.
     */
    public Method getBeforeCompletionMethod() {
        return beforeCompletionMethod;
    }
    
    /**
     * Set the Method annotated @AfterCompletion.
     */
    public void setAfterCompletionMethod(Method m) {
        afterCompletionMethod = m;
    }
    
    /**
     * Returns the Method annotated @AfterCompletion.
     */
    public Method getAfterCompletionMethod() {
        return afterCompletionMethod;
    }
    
    
	/**
	* Returns a formatted String of the attributes of this object.
	*/
    public void print(StringBuffer toStringBuffer) {
	toStringBuffer.append("Session descriptor");
	toStringBuffer.append("\n isStateless ").append(isStateless);
	super.print(toStringBuffer);
    }


}

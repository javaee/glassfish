/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.descriptors;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.lang.reflect.*;
import java.io.*;
import oracle.toplink.essentials.descriptors.DescriptorEventListener;
import oracle.toplink.essentials.descriptors.DescriptorEvent;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.sessions.SessionProfiler;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: The event manager allows for a descriptor to specify that 
 * an object should be notified when a TopLink event occurs.  It also determines 
 * how the object will be notified. To specify an event a method name can be 
 * registered to be called on the object when the event occurs. Events can be 
 * used to extend the TopLink reading and writing behavior.
 * <p>
 * These events include:
 * <ul>
 * <li> pre/postWrite - occurs when an object is written (occurs even if no changes to the object).
 * <li> pre/postInsert - occurs when an object is inserted.
 * <li> pre/postUpdate - occurs when an object is updated (occurs even if no changes to the object).
 * <li> pre/postDeleted - occurs when an object is deleted.
 * <li> postBuild/postRefresh - occurs after a object has been built/refreshed from its database row.
 * <li> aboutTo/Insert/Update - occurs when an object is about to be inserted/update allows for row modification.
 * <li> postClone - occurs when an object is registered/cloned in a unit of work.
 * <li> postMerge - occurs when an object is merged with its original in a unit of work.
 * </ul>
 *
 * @see ClassDescriptor
 */
public class DescriptorEventManager implements Cloneable, Serializable {
    protected ClassDescriptor descriptor;
    protected Vector eventSelectors;
    protected transient Vector eventMethods;
    protected transient Vector eventListeners;
    
    // EJB 3.0 support for event listeners.
    protected transient Vector defaultEventListeners;
    protected transient Vector entityListenerEventListeners;
    protected transient DescriptorEventListener entityEventListener;
    
    // EJB 3.0 support - cache our parent event managers.
    protected transient Vector entityEventManagers;
    protected transient Vector entityListenerEventManagers;
    
    // EJB 3.0 support for event listener configuration flags.
    protected boolean excludeDefaultListeners;
    protected boolean excludeSuperclassListeners;

    /** PERF: Cache if any events listener exist. */
    protected boolean hasAnyEventListeners;
    public static final int PreWriteEvent = 0;
    public static final int PostWriteEvent = 1;
    public static final int PreDeleteEvent = 2;
    public static final int PostDeleteEvent = 3;
    public static final int PreInsertEvent = 4;
    public static final int PostInsertEvent = 5;
    public static final int PreUpdateEvent = 6;
    public static final int PostUpdateEvent = 7;
    public static final int PostBuildEvent = 8;
    public static final int PostRefreshEvent = 9;
    public static final int PostCloneEvent = 10;
    public static final int PostMergeEvent = 11;
    public static final int AboutToInsertEvent = 12;
    public static final int AboutToUpdateEvent = 13;

    // CR#2660080 was missing aboutToDelete
    public static final int AboutToDeleteEvent = 14;
    
    // EJB 3.0 events
    public static final int PrePersistEvent = 15;
    public static final int PreRemoveEvent = 16;
    public static final int PreUpdateWithChangesEvent = 17;

    public static final int NumberOfEvents = 18;
    
    /**
     * INTERNAL:
     * Returns a new DescriptorEventManager for the specified Descriptor.
     */
    public DescriptorEventManager() {
        this.eventSelectors = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(NumberOfEvents);
        this.eventMethods = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(NumberOfEvents);
        this.hasAnyEventListeners = false;
        this.excludeDefaultListeners = false;
        this.excludeSuperclassListeners = false;
        

        for (int index = 0; index < NumberOfEvents; index++) {
            this.eventSelectors.addElement(null);
            this.eventMethods.addElement(null);
        }
    }

    /**
     * PUBLIC:
     * EJB 3.0 support for default listeners.
     */
    public void addDefaultEventListener(DescriptorEventListener listener) {
        getDefaultEventListeners().addElement(listener);
    }
    
    /**
     * PUBLIC:
     * EJB 3.0 support for lifecycle callback events defined on an entity 
     * listener class.
     */
    public void addEntityListenerEventListener(DescriptorEventListener listener) {
        getEntityListenerEventListeners().addElement(listener);
    }
     
    /**
     * PUBLIC:
     * Listener objects can be registered with the event manager to be notified 
     * when an event occurs on any instance of the descriptor's class.
     */
    public void addListener(DescriptorEventListener listener) {
        getEventListeners().addElement(listener);
        setHasAnyEventListeners(true);
    } 
    
    /**
     * INTERNAL:
     * Clone the manager and its private parts.
     */
    public Object clone() {
        DescriptorEventManager clone = null;

        try {
            clone = (DescriptorEventManager)super.clone();
            clone.setEventSelectors((Vector)getEventSelectors().clone());
            clone.setEventMethods((Vector)getEventMethods().clone());
            clone.setEventListeners((Vector)getEventListeners());
        } catch (Exception exception) {
            ;
        }

        return clone;
    }
    
    /**
     * INTERNAL:
     * EJB 3.0 support. Returns true if this event manager should exclude the 
     * invocation of the default listeners for this descriptor.
     */
    public boolean excludeDefaultListeners() {
        return excludeDefaultListeners;
    }

    /**
     * INTERNAL:
     * EJB 3.0 support. Returns true is this event manager should exclude the 
     * invocation of the listeners defined by the entity listener classes for 
     * the superclasses of this descriptor.
     */
    public boolean excludeSuperclassListeners() {
        return excludeSuperclassListeners;
    }
    
    /**
     * INTERNAL:
     * Execute the given selector with the event as argument.
     * @exception DescriptorException - the method cannot be found or executed
     */
    public void executeEvent(DescriptorEvent event) throws DescriptorException {
        try {
            event.getSession().startOperationProfile(SessionProfiler.DescriptorEvent);
            // CR#3467758, ensure the descriptor is set on the event.
            event.setDescriptor(getDescriptor());
            notifyListeners(event);
            notifyEJB30Listeners(event);

            if (event.getSource() instanceof DescriptorEventListener) {
                // Allow the object itself to implement the interface.
                notifyListener((DescriptorEventListener)event.getSource(), event);
                return;
            }

            Method eventMethod = (Method)getEventMethods().elementAt(event.getEventCode());
            if (eventMethod == null) {
                return;
            }

            // Now that I have the method, I need to invoke it
            try {
                Object[] runtimeParameters = new Object[1];
                runtimeParameters[0] = event;
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        AccessController.doPrivileged(new PrivilegedMethodInvoker(eventMethod, event.getSource(), runtimeParameters));
                    } catch (PrivilegedActionException exception) {
                        Exception throwableException = exception.getException();
                        if (throwableException instanceof IllegalAccessException) {
                            throw DescriptorException.illegalAccessWhileEventExecution(eventMethod.getName(), getDescriptor(), throwableException);
                        } else {
                            throw DescriptorException.targetInvocationWhileEventExecution(eventMethod.getName(), getDescriptor(), throwableException);
                        }
                    }
                } else {
                    PrivilegedAccessHelper.invokeMethod(eventMethod, event.getSource(), runtimeParameters);
                }
            } catch (IllegalAccessException exception) {
                throw DescriptorException.illegalAccessWhileEventExecution(eventMethod.getName(), getDescriptor(), exception);
            } catch (IllegalArgumentException exception) {
                throw DescriptorException.illegalArgumentWhileObsoleteEventExecute(eventMethod.getName(), getDescriptor(), exception);
            } catch (InvocationTargetException exception) {
                throw DescriptorException.targetInvocationWhileEventExecution(eventMethod.getName(), getDescriptor(), exception);
            }
        } finally {
            event.getSession().endOperationProfile(SessionProfiler.DescriptorEvent);
        }
    }

    /**
     * Find the method corresponding to the event selector. The method MUST take 
     * DescriptorEvent as argument, Session is also supported as argument for 
     * backward compatibility.
     */
    protected Method findMethod(int selector) throws DescriptorException {
        Class[] declarationParameters = new Class[1];
        declarationParameters[0] = ClassConstants.DescriptorEvent_Class;
        String methodName = (String)getEventSelectors().elementAt(selector);

        try {
            return Helper.getDeclaredMethod(getDescriptor().getJavaClass(), methodName, declarationParameters);
        } catch (NoSuchMethodException exception) {
            throw DescriptorException.noSuchMethodOnFindObsoleteMethod(methodName, getDescriptor(), exception);
        } catch (SecurityException exception) {
            throw DescriptorException.securityOnFindMethod(methodName, getDescriptor(), exception);
        }
    }
    
    /**
     * INTERNAL:
     */
    public String getAboutToInsertSelector() {
        return (String)getEventSelectors().elementAt(AboutToInsertEvent);
    }

    /**
     * INTERNAL:
     */
    public String getAboutToUpdateSelector() {
        return (String)getEventSelectors().elementAt(AboutToUpdateEvent);
    }

    /**
     * INTERNAL:
     * EJB 3.0 support. Returns the default listeners.
     */
    public Vector getDefaultEventListeners() {
        if (defaultEventListeners == null) {
            defaultEventListeners = new Vector();
        }
        
        return defaultEventListeners;
    }
    
    /**
     * INTERNAL:
     */
    protected ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * INTERNAL:
     * EJB 3.0 support. Returns the entity event listener.
     */
    public DescriptorEventListener getEntityEventListener() {
        return entityEventListener;
    }
    
    /**
     * INTERNAL:
     * EJB 3.0 support. Returns the entity listener event listeners.
     */
    public Vector getEntityListenerEventListeners() {
        if (entityListenerEventListeners == null) {
            entityListenerEventListeners = new Vector();
        }
        
        return entityListenerEventListeners;
    }
    
    /**
     * PUBLIC:
     * Returns the Listener objects that have been added.
     *
     * @see #addListener(DescriptorEventListener)
     */
    public Vector getEventListeners() {
        // Lazy initialize to avoid unessisary enumerations.
        if (eventListeners == null) {
            eventListeners = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        }
        return eventListeners;
    }

    protected Vector getEventMethods() {
        //Lazy Initialized to prevent Null Pointer exception after serialization
        if (this.eventMethods == null) {
            this.eventMethods = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(NumberOfEvents);
            for (int index = 0; index < NumberOfEvents; ++index) {
                this.eventMethods.addElement(null);
            }
        }
        return eventMethods;
    }

    protected Vector getEventSelectors() {
        if (this.eventSelectors == null) {
            this.eventSelectors = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(NumberOfEvents);
            for (int index = 0; index < NumberOfEvents; ++index) {
                this.eventSelectors.addElement(null);
            }
        }
        return eventSelectors;
    }

    /**
     * PUBLIC:
     * The name of the method called after an object is built
     */
    public String getPostBuildSelector() {
        return (String)getEventSelectors().elementAt(PostBuildEvent);
    }

    /**
     * PUBLIC:
     * The name of the method called after an object is cloned
     */
    public String getPostCloneSelector() {
        return (String)getEventSelectors().elementAt(PostCloneEvent);
    }

    /**
     * PUBLIC:
     * The name of the method called after an object is deleted
     */
    public String getPostDeleteSelector() {
        return (String)getEventSelectors().elementAt(PostDeleteEvent);
    }

    /**
     * PUBLIC:
     * The name of the method called after an object is inserted
     */
    public String getPostInsertSelector() {
        return (String)getEventSelectors().elementAt(PostInsertEvent);
    }

    /**
     * PUBLIC:
     * The name of the method called after an object is merged
     */
    public String getPostMergeSelector() {
        return (String)getEventSelectors().elementAt(PostMergeEvent);
    }

    /**
     * PUBLIC:
     * The name of the method called after an object is refreshed
     */
    public String getPostRefreshSelector() {
        return (String)getEventSelectors().elementAt(PostRefreshEvent);
    }

    /**
     * PUBLIC:
     * The name of the method called after an object is updated
     */
    public String getPostUpdateSelector() {
        return (String)getEventSelectors().elementAt(PostUpdateEvent);
    }

    /**
     * PUBLIC:
     * The name of the method called after an object is written
     */
    public String getPostWriteSelector() {
        return (String)getEventSelectors().elementAt(PostWriteEvent);
    }

    /**
     * PUBLIC:
     * The name of the method called before the create operation is applied to 
     * an object
     */
    public String getPrePersistSelector() {
        return (String)getEventSelectors().elementAt(PrePersistEvent);
    }

  /**
     * PUBLIC:
     * The name of the method called before an object is deleted
     */
    public String getPreDeleteSelector() {
        return (String)getEventSelectors().elementAt(PreDeleteEvent);
    }

    /**
     * PUBLIC:
     * The name of the method called before an object is inserted
     */
    public String getPreInsertSelector() {
        return (String)getEventSelectors().elementAt(PreInsertEvent);
    }

    /**
     * PUBLIC:
     * The name of the method called before the remove operation is applied to 
     * an object
     */
    public String getPreRemoveSelector() {
        return (String)getEventSelectors().elementAt(PreRemoveEvent);
    }

  /**
     * PUBLIC:
     * The name of the method called before an object is updated
     */
    public String getPreUpdateSelector() {
        return (String)getEventSelectors().elementAt(PreUpdateEvent);
    }

    /**
     * PUBLIC:
     * The name of the method called before an object is written
     */
    public String getPreWriteSelector() {
        return (String)getEventSelectors().elementAt(PreWriteEvent);
    }

    /**
     * INTERNAL:
     * Return if the event manager has any event listeners, or event methods.
     * If nothing is listening to event they can be avoided.
     */
    public boolean hasAnyEventListeners() {
        // Check listeners in case of collection added to directly as occurs 
        // for aggregates that have a clone of the event manager but not the 
        // listeners.
        return hasAnyEventListeners || hasAnyListeners() || hasEntityEventListener() || hasEntityListenerEventListeners();
    }   
    
    protected boolean hasAnyListeners() {
        return (eventListeners != null) && (!eventListeners.isEmpty());
    }
    
    /** 
     * INTERNAL:
     * EJB 3.0 support. Return true if this event manager has any entity event 
     * listeners.
     */
    public boolean hasEntityEventListener() {
        return entityEventListener != null;
    }
    
    /** 
     * INTERNAL:
     * EJB 3.0 support. Return true if this event manager has any entity 
     * listener event listeners.
     */
    public boolean hasEntityListenerEventListeners() {
        return entityListenerEventListeners != null && entityListenerEventListeners.size() > 0;
    }

    /**
     * INTERNAL:
     * Configure inherited selectors.
     */
    public void initialize(AbstractSession session) {
        // Initialize the EJB 3.0 supported listeners.
        initializeEJB30EventManagers();
        
        // Initialize if events are required at all.
        if (hasAnyListeners() || DescriptorEventListener.class.isAssignableFrom(getDescriptor().getJavaClass())) {
            setHasAnyEventListeners(true);
        }

        for (int index = 0; index < NumberOfEvents; index++) {
            if (getEventSelectors().elementAt(index) != null) {
                setHasAnyEventListeners(true);
                getEventMethods().setElementAt(findMethod(index), index);
            }
        }

        // Inherit all parent defined event method
        // Do NOT inherit the listener as the events are broadcast to the parent.
        if (getDescriptor().isChildDescriptor()) {
            DescriptorEventManager parentEventManager = getDescriptor().getInheritancePolicy().getParentDescriptor().getEventManager();

            for (int index = 0; index < NumberOfEvents; index++) {
                if (getEventSelectors().elementAt(index) == null) {
                    setHasAnyEventListeners(true);
                    getEventSelectors().setElementAt(parentEventManager.getEventSelectors().elementAt(index), index);
                    getEventMethods().setElementAt(parentEventManager.getEventMethods().elementAt(index), index);
                }
            }
        }
    }
    
    /**
     * INTERNAL:
     * EJB 3.0 support. Builds our chains of descriptor event managers that will 
     * need to be notified. The chains are cache so we only need to build them
     * once.
     */
    protected void initializeEJB30EventManagers() {
        entityEventManagers = new Vector();
        entityListenerEventManagers = new Vector();
             
        if (hasEntityEventListener()) {
            entityEventManagers.add(this);
        }
        
        if (hasEntityListenerEventListeners()) {
            entityListenerEventManagers.add(this);
        }
        
        ClassDescriptor currentDescriptor = getDescriptor();     
        boolean excludeEntityListeners = excludeSuperclassListeners();
            
        while (currentDescriptor.isChildDescriptor()) {
            currentDescriptor = currentDescriptor.getInheritancePolicy().getParentDescriptor();
            
            DescriptorEventManager eventManager = currentDescriptor.getEventManager();
            
            if (eventManager.hasEntityEventListener()) {
                entityEventManagers.add(eventManager);
            }
            
            if (eventManager.hasEntityListenerEventListeners()) {
                if (!excludeEntityListeners) {
                    entityListenerEventManagers.add(eventManager);
                }
            }
            
            excludeEntityListeners = eventManager.excludeSuperclassListeners();
        }
    }
    
    /**
     * INTERNAL:
     * Notify the EJB 3.0 event listeners.
     */
    protected void notifyEJB30Listeners(DescriptorEvent event) {
        // Step 1 - notify our default listeners.
        if (! excludeDefaultListeners()) {
            for (int i = 0; i < getDefaultEventListeners().size(); i++) {
                DescriptorEventListener listener = (DescriptorEventListener) getDefaultEventListeners().get(i);
                notifyListener(listener, event);
            }
        }
             
        // Step 2 - Notify the Entity Listener's first, top -> down.
        for (int index = entityListenerEventManagers.size() - 1; index >= 0; index--) {
            Vector entityListenerEventListeners = ((DescriptorEventManager) entityListenerEventManagers.get(index)).getEntityListenerEventListeners();
                 
            for (int i = 0; i < entityListenerEventListeners.size(); i++) {
                DescriptorEventListener listener = (DescriptorEventListener) entityListenerEventListeners.get(i);
                notifyListener(listener, event);        
            }
        }
             
        // Step 3 - Notify the Entity event listeners. top -> down, unless 
        // they are overriden in a subclass.
        for (int index = entityEventManagers.size() - 1; index >= 0; index--) {
            DescriptorEventListener entityEventListener = ((DescriptorEventManager) entityEventManagers.get(index)).getEntityEventListener();     
            
            if (! entityEventListener.isOverriddenEvent(event, entityEventManagers)) {
                notifyListener(entityEventListener, event);
            }
        }
    }
    
    /**
     * INTERNAL:
     * Big ugly case statement to notify listeners.
     */
    protected void notifyListener(DescriptorEventListener listener, DescriptorEvent event) throws DescriptorException {
        switch (event.getEventCode()) {
        case PreWriteEvent:
            listener.preWrite(event);
            break;
        case PostWriteEvent:
            listener.postWrite(event);
            break;
        case PreDeleteEvent:
            listener.preDelete(event);
            break;
        case PostDeleteEvent:
            listener.postDelete(event);
            break;
        case PreInsertEvent:
            listener.preInsert(event);
            break;
        case PostInsertEvent:
            listener.postInsert(event);
            break;
        case PreUpdateEvent:
            listener.preUpdate(event);
            break;
        case PostUpdateEvent:
            listener.postUpdate(event);
            break;
        case PostMergeEvent:
            listener.postMerge(event);
            break;
        case PostCloneEvent:
            listener.postClone(event);
            break;
        case PostBuildEvent:
            listener.postBuild(event);
            break;
        case PostRefreshEvent:
            listener.postRefresh(event);
            break;
        case AboutToInsertEvent:
            listener.aboutToInsert(event);
            break;
        case AboutToUpdateEvent:
            listener.aboutToUpdate(event);
            break;
        case AboutToDeleteEvent:
            listener.aboutToDelete(event);
            break;
        case PrePersistEvent:
            listener.prePersist(event);
            break;
        case PreRemoveEvent:
            listener.preRemove(event);
            break;
        case PreUpdateWithChangesEvent:
            listener.preUpdateWithChanges(event);
            break;
        default:
            throw DescriptorException.invalidDescriptorEventCode(event, getDescriptor());
        }
    }

    /**
     * INTERNAL:
     * Notify the event listeners.
     */
    public void notifyListeners(DescriptorEvent event) {
        if (hasAnyListeners()) {
            for (int index = 0; index < getEventListeners().size(); index++) {
                DescriptorEventListener listener = (DescriptorEventListener)getEventListeners().get(index);
                notifyListener(listener, event);
            }
        }
  
        // Also must notify any inherited listeners.
        if (getDescriptor().isChildDescriptor()) {
            getDescriptor().getInheritancePolicy().getParentDescriptor().getEventManager().notifyListeners(event);
        }
    }
    
    /**
     * INTERNAL:
     * Used to initialize a remote DescriptorEventManager.
     */
    public void remoteInitialization(AbstractSession session) {
        this.eventMethods = new Vector(NumberOfEvents);

        for (int index = 0; index < NumberOfEvents; index++) {
            this.eventMethods.addElement(null);
        }

        initialize(session);
    }

    /**
     * PUBLIC:
     * Remove a event listener.
     */
    public void removeListener(DescriptorEventListener listener) {
        getEventListeners().removeElement(listener);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called when an object's row it about to 
     * be inserted. This uses the optional event argument of the DatabaseRow.
     * This is different from pre/postInsert because it occurs after the row has 
     * already been built. This event can be used to modify the row before 
     * insert, such as adding a user inserted by.
     */
    public void setAboutToInsertSelector(String aboutToInsertSelector) {
        getEventSelectors().setElementAt(aboutToInsertSelector, AboutToInsertEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called when an object's row it about to 
     * be updated. This uses the optional event argument of the DatabaseRow.
     * This is different from pre/postUpdate because it occurs after the row has 
     * already been built, and it ONLY called if the update is required (changed 
     * within a unit of work), as the other occur ALWAYS. This event can be used 
     * to modify the row before insert, such as adding a user inserted by.
     */
    public void setAboutToUpdateSelector(String aboutToUpdateSelector) {
        getEventSelectors().setElementAt(aboutToUpdateSelector, AboutToUpdateEvent);
    }

    /**
     * INTERNAL:
     * Set the descriptor.
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    /**
     * PUBLIC:
     * EJB 3.0 support for lifecycle callback events defined on an entity class.
     */
    public void setEntityEventListener(DescriptorEventListener listener) {
        this.entityEventListener = listener;
    }

    protected void setEventListeners(Vector eventListeners) {
        this.eventListeners = eventListeners;
    }

    protected void setEventMethods(Vector eventMethods) {
        this.eventMethods = eventMethods;
    }

    protected void setEventSelectors(Vector eventSelectors) {
        this.eventSelectors = eventSelectors;
    }
    
    /**
     * INTERNAL:
     * EJB 3.0 support. Default listeners apply to all entities in a persistence 
     * unit. Set this flag to true to exclude the invocation of the default 
     * listeners for this descriptor.
     */
    public void setExcludeDefaultListeners(boolean excludeDefaultListeners) {
        this.excludeDefaultListeners = excludeDefaultListeners;
    }

    /**
     * INTERNAL:
     * EJB 3.0 support. If multiple entity classes in an inheritance hierarchy 
     * define entity listeners, the listeners defined for a superclass are 
     * invoked before the listeners defined for its subclasses. Set this flag 
     * to true to exclude the invocation of the listeners defined by the entity 
     * listener classes for the superclasses of this descriptor.
     */
    public void setExcludeSuperclassListeners(boolean excludeSuperclassListeners) {
        this.excludeSuperclassListeners = excludeSuperclassListeners;
    }
    
    /**
     * INTERNAL:
     * Set if the event manager has any event listeners, or event methods.
     * If nothing is listening to event they can be avoided.
     */
    protected void setHasAnyEventListeners(boolean hasAnyEventListeners) {
        this.hasAnyEventListeners = hasAnyEventListeners;
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that has just been 
     * built from the database. This uses the optional event argument of the 
     * DatabaseRow. This event can be used to correctly initialize an object's 
     * non-persistent attributes or to perform complex optimizations or 
     * mappings. This event is called whenever an object is built.
     */
    public void setPostBuildSelector(String postBuildSelector) {
        getEventSelectors().setElementAt(postBuildSelector, PostBuildEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that has just been 
     * cloned into a unit of work. This uses the optional event argument of the 
     * orignial object (the source object it the clone). This event can be used 
     * to correctly initialize an object's non-persistent attributes.
     */
    public void setPostCloneSelector(String postCloneSelector) {
        getEventSelectors().setElementAt(postCloneSelector, PostCloneEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that has just been 
     * deleted from the database. This event can notify/remove any dependents 
     * on the object.
     */
    public void setPostDeleteSelector(String postDeleteSelector) {
        getEventSelectors().setElementAt(postDeleteSelector, PostDeleteEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that has just been 
     * inserted into the database. This event can be used to notify any 
     * dependent on the object, or to update information not accessible until 
     * the object has been inserted.
     */
    public void setPostInsertSelector(String postInsertSelector) {
        getEventSelectors().setElementAt(postInsertSelector, PostInsertEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that has just been 
     * merge from a unit of work. This uses the optional event argument of the 
     * orignial object which is the object being merged from, the source object 
     * is the object being merged into. This event can be used to correctly 
     * initialize an object's non-persistent attributes.
     */
    public void setPostMergeSelector(String postMergeSelector) {
        getEventSelectors().setElementAt(postMergeSelector, PostMergeEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that has just been 
     * refreshed from the database. This uses the optional event argument of 
     * the DatabaseRow. This event can be used to correctly initialize an 
     * object's non-persistent attributes or to perform complex optimizations or 
     * mappings. This event is only called on refreshes of existing objects.
     */
    public void setPostRefreshSelector(String postRefreshSelector) {
        getEventSelectors().setElementAt(postRefreshSelector, PostRefreshEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that has just been 
     * updated into the database.
     */
    public void setPostUpdateSelector(String postUpdateSelector) {
        getEventSelectors().setElementAt(postUpdateSelector, PostUpdateEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that has just been 
     * written to the database. This event is raised on any registered object 
     * in a unit of work, even if it has not changed, refer to the 
     * "aboutToUpdate" selector if it is required for the event to be raised 
     * only when the object has been changed. This will be called on all inserts 
     * and updates, after the "postInsert/Update" event has been raised. This 
     * event can be used to notify any dependent on the object.
     */
    public void setPostWriteSelector(String postWriteSelector) {
        getEventSelectors().setElementAt(postWriteSelector, PostWriteEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that is going to be 
     * deleted from the database. This event can notify/remove any dependents 
     * on the object.
     */
    public void setPreDeleteSelector(String preDeleteSelector) {
        getEventSelectors().setElementAt(preDeleteSelector, PreDeleteEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that is going to be 
     * inserted into the database. This event can be used to notify any 
     * dependent on the object or acquire the object's id through a custom 
     * mechanism.
     */
    public void setPreInsertSelector(String preInsertSelector) {
        getEventSelectors().setElementAt(preInsertSelector, PreInsertEvent);
    }
    
    /**
     * PUBLIC:
     * A method can be registered to be called on a object when that object has 
     * the create operation applied to it.
     */
    public void setPrePersistSelector(String prePersistSelector) {
        getEventSelectors().setElementAt(prePersistSelector, PrePersistEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object when that object has 
     * the remove operation applied to it.
     */
    public void setPreRemoveSelector(String preRemoveSelector) {
        getEventSelectors().setElementAt(preRemoveSelector, PreRemoveEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that is going to be 
     * updated into the database. This event is raised on any registered object 
     * in a unit of work, even if it has not changed, refer to the 
     * "aboutToUpdate" selector if it is required for the event to be raised 
     * only when the object has been changed. This event can be used to notify 
     * any dependent on the object.
     */
    public void setPreUpdateSelector(String preUpdateSelector) {
        getEventSelectors().setElementAt(preUpdateSelector, PreUpdateEvent);
    }

    /**
     * PUBLIC:
     * A method can be registered to be called on a object that is going to be 
     * written to the database. This event is raised on any registered object 
     * in a unit of work, even if it has not changed, refer to the 
     * "aboutToUpdate" selector if it is required for the event to be raised 
     * only when the object has been changed. This will be called on all inserts 
     * and updates, before the "preInsert/Update" event has been raised. This 
     * event can be used to notify any dependent on the object.
     */
    public void setPreWriteSelector(String preWriteSelector) {
        getEventSelectors().setElementAt(preWriteSelector, PreWriteEvent);
    }
}

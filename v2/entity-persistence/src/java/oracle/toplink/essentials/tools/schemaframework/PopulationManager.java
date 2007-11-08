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
package oracle.toplink.essentials.tools.schemaframework;

import java.util.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p>
 * <b>Purpose</b>: This class is used to populate example data into the database, it allows for cirrcular references to be resolved.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Allow objects to be registered.
 * <li> Allow objects to be looked up.
 * <li> Store a globally accessable default instance.
 */
public class PopulationManager {

    /** Store the objects registered. */
    protected Hashtable registeredObjects;

    /** Store the default instance. */
    protected static PopulationManager defaultManager;

    public PopulationManager() {
        registeredObjects = new Hashtable();
    }

    /**
     * Add all of the objects of the class and all of its subclasses.
     * The session is needed because there is no other way to find all subclasses.
     */
    public void addAllObjectsForAbstractClass(Class objectsClass, AbstractSession session, Vector allObjects) {
        ClassDescriptor descriptor = session.getDescriptor(objectsClass);
        addAllObjectsForClass(objectsClass, allObjects);
        for (Enumeration enumeration = descriptor.getInheritancePolicy().getChildDescriptors().elements();
                 enumeration.hasMoreElements();) {
            addAllObjectsForAbstractClass(((ClassDescriptor)enumeration.nextElement()).getJavaClass(), session, allObjects);
        }
    }

    /**
     * Add all of the objects of the class and all of its subclasses.
     * The session is needed because there is no other way to find all subclasses.
     */
    public void addAllObjectsForAbstractClass(Class objectsClass, oracle.toplink.essentials.sessions.Session session, Vector allObjects) {
        addAllObjectsForAbstractClass(objectsClass, (AbstractSession)session, allObjects);
    }

    /**
     * Add all of the objects of the class.
     */
    public void addAllObjectsForClass(Class objectsClass, Vector allObjects) {
        if (!getRegisteredObjects().containsKey(objectsClass)) {
            return;
        }

        for (Enumeration enumeration = ((Hashtable)getRegisteredObjects().get(objectsClass)).elements();
                 enumeration.hasMoreElements();) {
            allObjects.addElement(enumeration.nextElement());
        }
    }

    /**
     * Check if the object is registred given its name.
     */
    public boolean containsObject(Class objectsClass, String objectsName) {
        return ((getRegisteredObjects().containsKey(objectsClass)) && (((Hashtable)getRegisteredObjects().get(objectsClass)).containsKey(objectsName)));
    }

    /**
     * Check if the object is registred given its name.
     */
    public boolean containsObject(Object objectToCheck, String objectsName) {
        return containsObject(objectToCheck.getClass(), objectsName);
    }

    /**
     * Return all of the objects registered.
     */
    public Vector getAllClasses() {
        Vector allClasses = new Vector();

        for (Enumeration e = getRegisteredObjects().keys(); e.hasMoreElements();) {
            allClasses.addElement(e.nextElement());
        }

        return allClasses;
    }

    /**
     * Return all of the objects registered.
     */
    public Vector getAllObjects() {
        Vector allObjects;

        allObjects = new Vector();
        for (Enumeration e = getAllClasses().elements(); e.hasMoreElements();) {
            Class eachClass = (Class)e.nextElement();
            addAllObjectsForClass(eachClass, allObjects);
        }

        return allObjects;
    }

    /**
     * Return all of the objects of the class and all of its subclasses.
     */
    public Vector getAllObjectsForAbstractClass(Class objectsClass) {
        Vector allObjects;

        allObjects = new Vector();
        // hummm, how can this be done....
        return allObjects;
    }

    /**
     * Return all of the objects of the class and all of its subclasses.
     * The session is needed because there is no other way to find all subclasses.
     */
    public Vector getAllObjectsForAbstractClass(Class objectsClass, AbstractSession session) {
        ClassDescriptor descriptor = session.getDescriptor(objectsClass);
        Vector allObjects = new Vector();
        addAllObjectsForClass(objectsClass, allObjects);
        if (descriptor.hasInheritance()) {
            for (Enumeration enumeration = descriptor.getInheritancePolicy().getChildDescriptors().elements();
                     enumeration.hasMoreElements();) {
                addAllObjectsForClass(((ClassDescriptor)enumeration.nextElement()).getJavaClass(), allObjects);
            }
        }

        return allObjects;
    }

    /**
     * Return all of the objects of the class.
     */
    public Vector getAllObjectsForClass(Class objectsClass) {
        Vector allObjects;

        allObjects = new Vector();
        addAllObjectsForClass(objectsClass, allObjects);

        return allObjects;
    }

    /**
     * Lazy initialize the default instance.
     */
    public static PopulationManager getDefaultManager() {
        if (defaultManager == null) {
            defaultManager = new PopulationManager();
        }
        return defaultManager;
    }

    /**
     * Return the object registred given its name.
     */
    public Object getObject(Class objectsClass, String objectsName) {
        if (!(getRegisteredObjects().containsKey(objectsClass))) {
            return null;
        }

        return ((Hashtable)getRegisteredObjects().get(objectsClass)).get(objectsName);
    }

    /**
     * Return the registered objects.
     */
    public Hashtable getRegisteredObjects() {
        return registeredObjects;
    }

    /**
     * Register the object given its name.
     * The objects are represented as a hashtable of hashtables, lazy initialized on the class.
     */
    public Object registerObject(Class javaClass, Object objectToRegister, String objectsName) {
        if (!(getRegisteredObjects().containsKey(javaClass))) {
            getRegisteredObjects().put(javaClass, new Hashtable());
        }
        ((Hashtable)getRegisteredObjects().get(javaClass)).put(objectsName, objectToRegister);

        return objectToRegister;
    }

    /**
     * Register the object given its name.
     * The objects are represented as a hashtable of hashtables, lazy initialized on the class.
     */
    public Object registerObject(Object objectToRegister, String objectsName) {
        if (!(getRegisteredObjects().containsKey(objectToRegister.getClass()))) {
            getRegisteredObjects().put(objectToRegister.getClass(), new Hashtable());
        }
        ((Hashtable)getRegisteredObjects().get(objectToRegister.getClass())).put(objectsName, objectToRegister);

        return objectToRegister;
    }

    /**
     * Remove the object given its class and name.
     */
    public void removeObject(Class classToRemove, String objectsName) {
        if (getRegisteredObjects().containsKey(classToRemove)) {
            ((Hashtable)getRegisteredObjects().get(classToRemove)).remove(objectsName);
        }
    }

    /**
     * Remove the object given its name.
     */
    public Object removeObject(Object objectToRemove, String objectsName) {
        removeObject(objectToRemove.getClass(), objectsName);

        return objectToRemove;
    }

    /**
     * Reset the default instance.
     */
    public static void resetDefaultManager() {
        defaultManager = null;
    }

    /**
     * Set the default instance.
     */
    public static void setDefaultManager(PopulationManager theDefaultManager) {
        defaultManager = theDefaultManager;
    }

    /**
     * Set the registered objects.
     */
    public void setRegisteredObjects(Hashtable registeredObjects) {
        this.registeredObjects = registeredObjects;
    }
}

/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package javax.ejb;

import java.util.Map;
import java.util.Set;

import javax.naming.Context;

/** 
  * Used to execute an EJB application in an embeddable container.  
  */
public abstract class EJBContainer {

    /**
     * Standard property name for specifying the embeddable container implementation bootstrap
     * class.  Property value is a fully-qualified class name.
     */
    public static final String EMBEDDABLE_INITIAL_PROPERTY = "javax.ejb.embeddable.initial";

    /**
     * Standard property name for specifying the set of modules to be initialized.  Property
     * value is either a single module name String or a List<String> containing module names.
     */
    public static final String EMBEDDABLE_MODULES_PROPERTY = "javax.ejb.embeddable.modules";

    /**
     * Create and initialize an embeddable EJB container.  JVM classpath is 
     * searched for all ejb-jars or exploded ejb-jars in directory format.
     *
     * @return EJBContainer instance
     *
     * @exception java.lang.EJBException  Thrown if the container or application
     * could not be successfully initialized.
     */
    public static EJBContainer createEJBContainer() { 
	return null;
    }

    /**
     * Create and initialize an embeddable EJB container with a
     * set of configuration properties.
     *
     * @param properties  Spec-defined and/or vendor-specific properties.
     * The spec reserves the prefix "javax.ejb." for spec-defined properties.
     *
     * @return EJBContainer instance
     *
     * @exception java.lang.EJBException  Thrown if the container or application
     * could not be successfully initialized.
     */
    public static EJBContainer createEJBContainer(Map<?,?> properties) {
	return null;
    }

   /**
    * Retrieve the last EJBContainer instance to be successfully returned from
    * an invocation to a createEJBContainer method.  
    *
    * @return EJBContainer instance,  or null if none exists or if the last 
    * EJBContainer instance has been closed.
    * 
    */  
    public static EJBContainer getCurrentEJBContainer() {
        return null;
    }
   
    /**
     * Retrieve a naming context for looking up references to session beans
     * executing in the embeddable container.
     *
     * @return naming context
     */
    abstract public Context getContext();

    /**
     * Shutdown an embeddable EJBContainer instance.
     */
    abstract public void close(); 
	

}


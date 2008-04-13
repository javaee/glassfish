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

package org.glassfish.api.deployment;

/**
 * Interface to an application container. mainly used to start and stop 
 * the application.
 *
 * @author Jerome Dochez
 */

public interface ApplicationContainer<T> {

    /**
     * Returns the deployment descriptor associated with this application
     * 
     * @return deployment descriptor if they exist or null if not
     */
    public T getDescriptor();
    
    /**
     * Starts an application container. 
     * ContractProvider starting should not throw an exception but rather should
     * use their prefered Logger instance to log any issue they encounter while 
     * starting. Returning false from a start mean that the container failed 
     * to start
     * @param cl the application classloader 
     * @return true if the container startup was successful. 
     */
    public boolean start(ClassLoader cl);
    
    /**
     * Stop the application container
     * @return true if stopping was successful.  
     */
    public boolean stop();

    /**
     * Returns the class loader associated with this application
     * @return ClassLoader for this app
     */
    public ClassLoader getClassLoader();
    
}

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
package javax.ejb;

import java.rmi.RemoteException;

/**
 * The EJBObject interface is extended by all enterprise Beans' remote 
 * interfaces. An enterprise Bean's remote interface provides the remote
 * client view
 * of an EJB object. An enterprise Bean's remote interface defines 
 * the business methods callable by a remote client.
 *
 * <p> The remote interface must
 * extend the javax.ejb.EJBObject interface, and define the enterprise Bean
 * specific business methods.
 *
 * <p> The enterprise Bean's remote interface is defined by the enterprise
 * Bean provider and implemented by the enterprise Bean container.
 */
public interface EJBObject extends java.rmi.Remote {
    /**
     * Obtain the enterprise Bean's remote home interface. The remote home 
     * interface defines the enterprise Bean's create, finder, remove,
     * and home business methods.
     * 
     * @return A reference to the enterprise Bean's home interface.
     *
     * @exception RemoteException Thrown when the method failed due to a
     *    system-level failure.
     */
    public EJBHome getEJBHome() throws RemoteException; 

    /**
     * Obtain the primary key of the EJB object. 
     *
     * <p> This method can be called on an entity bean. An attempt to invoke
     * this method on a session bean will result in RemoteException.
     *
     * @return The EJB object's primary key.
     *
     * @exception RemoteException Thrown when the method failed due to a
     *    system-level failure or when invoked on a session bean.
     */
    public Object getPrimaryKey() throws RemoteException;

    /**
     * Remove the EJB object.
     *
     * @exception RemoteException Thrown when the method failed due to a
     *    system-level failure.
     *
     * @exception RemoveException The enterprise Bean or the container
     *    does not allow destruction of the object.
     */ 
    public void remove() throws RemoteException, RemoveException;

    /**
     * Obtain a handle for the EJB object. The handle can be used at later
     * time to re-obtain a reference to the EJB object, possibly in a
     * different Java Virtual Machine.
     *
     * @return A handle for the EJB object.
     *
     * @exception RemoteException Thrown when the method failed due to a
     *    system-level failure.
     */
    public Handle getHandle() throws RemoteException;

    /**
     * Test if a given EJB object is identical to the invoked EJB object.
     *
     * @param obj An object to test for identity with the invoked object.
     *
     * @return True if the given EJB object is identical to the invoked object,
     *    false otherwise.
     *
     * @exception RemoteException Thrown when the method failed due to a
     *    system-level failure.
     */
    boolean isIdentical(EJBObject obj) throws RemoteException;
} 

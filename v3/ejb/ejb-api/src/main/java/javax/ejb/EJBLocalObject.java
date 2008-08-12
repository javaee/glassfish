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

/**
 * The EJBLocalObject interface must be extended by all enterprise Beans' local
 * interfaces. An enterprise Bean's local interface provides the local client 
 * view of an EJB object. An enterprise Bean's local interface defines 
 * the business methods callable by local clients.
 *
 * <p> The enterprise Bean's local interface is defined by the enterprise
 * Bean provider and implemented by the enterprise Bean container.
 */
public interface EJBLocalObject {
    /**
     * Obtain the enterprise Bean's local home interface. The local home
     * interface defines the enterprise Bean's create, finder, remove,
     * and home business methods that are available to local clients.
     * 
     * @return A reference to the enterprise Bean's local home interface.
     *
     * @exception EJBException Thrown when the method failed due to a
     *    system-level failure.
     *
     */
    public EJBLocalHome getEJBLocalHome() throws EJBException; 

    /**
     * Obtain the primary key of the EJB local object. 
     *
     * <p> This method can be called on an entity bean. 
     * An attempt to invoke this method on a session Bean will result in
     * an EJBException.
     *
     * @return The EJB local object's primary key.
     *
     * @exception EJBException Thrown when the method failed due to a
     *    system-level failure or when invoked on a session bean.
     *
     */
    public Object getPrimaryKey() throws EJBException;

    /**
     * Remove the EJB local object.
     *
     * @exception RemoveException The enterprise Bean or the container
     *    does not allow destruction of the object.
     *
     * @exception EJBException Thrown when the method failed due to a
     *    system-level failure.
     *
     */ 
    public void remove() throws RemoveException, EJBException;

    /**
     * Test if a given EJB local object is identical to the invoked EJB 
     * local object.
     *
     * @param obj An object to test for identity with the invoked object.
     *
     * @return True if the given EJB local object is identical to the 
     * invoked object, false otherwise.
     *
     *
     * @exception EJBException Thrown when the method failed due to a
     *    system-level failure.
     *
     */
    boolean isIdentical(EJBLocalObject obj) throws EJBException;
} 

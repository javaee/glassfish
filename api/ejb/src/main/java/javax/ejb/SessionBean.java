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
 * The SessionBean interface is implemented by every session enterprise Bean 
 * class. The container uses the SessionBean methods to notify the enterprise
 * Bean instances of the instance's life cycle events.
 */
public interface SessionBean extends EnterpriseBean {
    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     *
     * <p> The enterprise Bean instance should store the reference to the
     * context object in an instance variable.
     *
     * <p> This method is called with no transaction context.
     *
     * @param ctx A SessionContext interface for the instance.
     *
     * @exception EJBException Thrown by the method to indicate a failure
     *    caused by a system-level error.
     *
     * @exception RemoteException This exception is defined in the method
     *    signature to provide backward compatibility for applications written
     *    for the EJB 1.0 specification. Enterprise beans written for the 
     *    EJB 1.1 specification should throw the
     *    javax.ejb.EJBException instead of this exception.
     *    Enterprise beans written for the EJB2.0 and higher specifications
     *    must throw the javax.ejb.EJBException instead of this exception.
     */
    void setSessionContext(SessionContext ctx) throws EJBException,
	    RemoteException;

    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     * 
     * <p> This method is called with no transaction context.
     *
     * @exception EJBException Thrown by the method to indicate a failure
     *    caused by a system-level error.
     *
     * @exception RemoteException This exception is defined in the method
     *    signature to provide backward compatibility for enterprise beans 
     *    written for the EJB 1.0 specification. Enterprise beans written 
     *    for the EJB 1.1 specification should throw the
     *    javax.ejb.EJBException instead of this exception.
     *    Enterprise beans written for the EJB2.0 and higher specifications
     *    must throw the javax.ejb.EJBException instead of this exception.
     */
     void ejbRemove() throws EJBException, RemoteException;    

    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     *
     * <p> This method is called with no transaction context.
     *
     * @exception EJBException Thrown by the method to indicate a failure
     *    caused by a system-level error.
     *
     * @exception RemoteException This exception is defined in the method
     *    signature to provide backward compatibility for enterprise beans 
     *    written for the EJB 1.0 specification. Enterprise beans written 
     *    for the EJB 1.1 specification should throw the
     *    javax.ejb.EJBException instead of this exception.
     *    Enterprise beans written for the EJB2.0 and higher specifications
     *    must throw the javax.ejb.EJBException instead of this exception.
     */
    void ejbActivate() throws EJBException, RemoteException;

    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     *
     * <p> After the passivate method completes, the instance must be
     * in a state that allows the container to use the Java Serialization
     * protocol to externalize and store away the instance's state.
     *
     * <p> This method is called with no transaction context.
     *
     * @exception EJBException Thrown by the method to indicate a failure
     *    caused by a system-level error.
     *
     * @exception RemoteException This exception is defined in the method
     *    signature to provide backward compatibility for enterprise beans 
     *    written for the EJB 1.0 specification. Enterprise beans written 
     *    for the EJB 1.1 specification should throw the
     *    javax.ejb.EJBException instead of this exception.
     *    Enterprise beans written for the EJB2.0 and higher specifications
     *    must throw the javax.ejb.EJBException instead of this exception.
     */
    void ejbPassivate() throws EJBException, RemoteException;
}

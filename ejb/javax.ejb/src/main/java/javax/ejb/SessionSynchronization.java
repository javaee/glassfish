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
 * <p> The SessionSynchronization interface allows a session Bean instance
 * to be notified by its container of transaction boundaries.
 *
 * <p>  An session Bean class is not required to implement this interface.
 * A session Bean class should implement this interface only if it wishes 
 * to synchronize its state with the transactions.
 */
public interface SessionSynchronization {
    /**
     * The afterBegin method notifies a session Bean instance that a new
     * transaction has started, and that the subsequent business methods on the
     * instance will be invoked in the context of the transaction.
     *
     * <p> The instance can use this method, for example, to read data
     * from a database and cache the data in the instance fields.
     *
     * <p> This method executes in the proper transaction context.
     *
     * @exception EJBException Thrown by the method to indicate a failure
     *    caused by a system-level error.
     *
     * @exception RemoteException This exception is defined in the method
     *    signature to provide backward compatibility for enterprise beans 
     *    written for the EJB 1.0 specification. Enterprise beans written 
     *    for the EJB 1.1 and higher specifications should throw the
     *    javax.ejb.EJBException instead of this exception. 
     *    Enterprise beans written for the EJB 2.0 and higher specifications 
     *    must not throw the java.rmi.RemoteException.
     */
    public void afterBegin() throws EJBException, RemoteException;

    /**
     * The beforeCompletion method notifies a session Bean instance that
     * a transaction is about to be committed. The instance can use this
     * method, for example, to write any cached data to a database.
     *
     * <p> This method executes in the proper transaction context.
     *
     * <p><b>Note:</b> The instance may still cause the container to
     * rollback the transaction by invoking the setRollbackOnly() method
     * on the instance context, or by throwing an exception.
     *
     * @exception EJBException Thrown by the method to indicate a failure
     *    caused by a system-level error.
     *
     * @exception RemoteException This exception is defined in the method
     *    signature to provide backward compatibility for enterprise beans 
     *    written for the EJB 1.0 specification. Enterprise beans written 
     *    for the EJB 1.1 and higher specification should throw the
     *    javax.ejb.EJBException instead of this exception.
     *    Enterprise beans written for the EJB 2.0 and higher specifications 
     *    must not throw the java.rmi.RemoteException.
     */
    public void beforeCompletion() throws EJBException, RemoteException;

    /**
     * The afterCompletion method notifies a session Bean instance that a
     * transaction commit protocol has completed, and tells the instance
     * whether the transaction has been committed or rolled back.
     *
     * <p> This method executes with no transaction context.
     *
     * <p> This method executes with no transaction context.
     *
     * @param committed True if the transaction has been committed, false
     *    if is has been rolled back.
     *
     * @exception EJBException Thrown by the method to indicate a failure
     *    caused by a system-level error.
     *
     * @exception RemoteException This exception is defined in the method
     *    signature to provide backward compatibility for enterprise beans 
     *    written for the EJB 1.0 specification. Enterprise beans written 
     *    for the EJB 1.1 and higher specification should throw the
     *    javax.ejb.EJBException instead of this exception. 
     *    Enterprise beans written for the EJB 2.0 and higher specifications 
     *    must not throw the java.rmi.RemoteException.
     */
    public void afterCompletion(boolean committed) throws EJBException,
	    RemoteException;
}

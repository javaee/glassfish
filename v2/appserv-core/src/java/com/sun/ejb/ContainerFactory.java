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

import java.util.Enumeration;
import java.rmi.Remote;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;

import javax.ejb.*;
import javax.ejb.spi.HandleDelegate;
import javax.transaction.TransactionManager;
import com.sun.enterprise.deployment.EjbDescriptor;

import com.sun.enterprise.config.ConfigContext;

/**
 * ContainerFactory creates the appropriate Container instance
 * (StatefulSessionContainer, StatelessSessionContainer, EntityContainer, 
 * MessageBeanContainer) and initializes it.
 *
 * It is also a factory for EJBObject/Home instances which are needed
 * by the Protocol Manager when a remote invocation arrives.
 *
 */
public interface ContainerFactory {
    /**
     * Create the appropriate Container instance and initialize it.
     * @param ejbDescriptor the deployment descriptor of the EJB
			    for which a container is to be created.
     */
    Container createContainer(EjbDescriptor ejbDescriptor, 
			      ClassLoader loader, 
			      com.sun.enterprise.SecurityManager sm,
			      ConfigContext dynamicConfigContext)
	throws Exception;

    /**
     * Get the container instance corresponding to the given EJB id.
     */
    Container getContainer(long ejbId);

    /**
     * Remove the container instance corresponding to the given EJB id.
     */
    public void removeContainer(long ejbId);

    /**
     * List all container instances in this JVM.
     */
    public Enumeration listContainers();

    /**
     * Return the container factory's TransactionManager object.
     * Called from SerialContext during JNDI lookup at 
     * "java:pm/TransactionManager". Note that the container's
     * TransactionManager is a wrapper over the real TM.
     */
    public TransactionManager getTransactionMgr();

    /**
     * Return the EjbDescriptor for the given ejbId.
     */
    public EjbDescriptor getEjbDescriptor(long ejbId);


    public Object getEJBContextObject(String contextType);


    /**
     * Register an EntityManager with EXTENDED persistence context for
     * the current ejb component.  Only applicable for stateful session
     * beans.
     */
    public EntityManager lookupExtendedEntityManager(EntityManagerFactory factory);

    /**
     * EJB Timer Service operations.
     */
    public void initEJBTimerService() throws Exception;
    public void restoreEJBTimers() throws Exception;
    public void shutdownEJBTimerService();

}

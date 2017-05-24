/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.enterprise.iiop.api;


import java.rmi.Remote;

/**
 * The RemoteReferenceFactory interface provides methods to
 * create and destroy remote EJB references. Instances of the
 * RemoteReferenceFactory are obtained from the ProtocolManager.
 *
 */
public interface RemoteReferenceFactory {
 
    /**
     * Create a remote reference for an EJBObject which can
     * be used for performing remote invocations.
     * The key specifies the unique
     * "object-id" of the EJBObject. This operation should not
     * create any "tie" for the particular remote object instance.
     * This operation should not cause the ProtocolManager to maintain
     * any instance-specific state about the EJB instance.
     *
     * @param instanceKey a unique identifier for the EJB instance 
     *          which is unique across all EJB refs created using this 
     *          RemoteReferenceFactory instance.
     * @return the protocol-specific stub of the proper derived type.
     *       It should not be necessary to narrow this stub again.
     */
    Remote createRemoteReference(byte[] instanceKey);

   
    /**
     * Create a remote reference for an EJBHome which can
     * be used for performing remote invocations.
     * The key specifies the unique
     * "object-id" of the EJBHome. This operation should not
     * create any "tie" for the particular remote object instance.
     * This operation should not cause the ProtocolManager to maintain
     * any instance-specific state about the EJB instance.
     *
     * @param homeKey a unique identifier for the EJB instance
     *          which is unique across all EJB refs created using this 
     *          RemoteReferenceFactory instance.
     * @return the protocol-specific stub of the proper derived type.
     *       It should not be necessary to narrow this stub again.
     */
    Remote createHomeReference(byte[] homeKey);

    /**
     * Destroy an EJBObject or EJBHome remote ref 
     * so that it can no longer be used for remote invocations. 
     * This operation should destroy any state such as "tie" objects 
     * maintained by the ProtocolManager for the EJBObject or EJBHome.
     *
     * @param remoteRef the remote reference for the EJBObject/EJBHome
     * @param remoteObj the servant corresponding to the remote reference.
     */
    void destroyReference(Remote remoteRef, Remote remoteObj);

    /**
     * Destroy the factory itself. Called during shutdown / undeploy.
     * The factory is expected to release all resources in this method.
     */
    public void destroy();


    public boolean hasSameContainerID(org.omg.CORBA.Object ref)
	throws Exception;

    public void setRepositoryIds(Class homeIntf, Class remoteIntf);
    
    public void cleanupClass(Class clazz);

    public int getCSIv2PolicyType();

}


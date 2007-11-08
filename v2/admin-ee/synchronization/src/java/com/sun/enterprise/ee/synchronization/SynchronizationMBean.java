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
package com.sun.enterprise.ee.synchronization;

import java.io.IOException;
import javax.management.MBeanException; 
import com.sun.enterprise.config.ConfigException;

/**
 * Interface SynchronizationMBean
 */
public interface SynchronizationMBean
{

    /**
     * Method synchronize.
     *
     * @param request  synchronization request(s)
     *
     * @return  synchronization response 
     *
     * @throws IOException  if an error during synchronization
     * @throws ConfigException if a configuration parsing error
     */
    public SynchronizationResponse synchronize(SynchronizationRequest[] request)
        throws IOException, ConfigException, MBeanException;

    /**
     * Health check method called from sever instance or node agents.
     *
     * This mehtod contains a light-weight implementation. It is called 
     * from server instance (or node agent) at the beginning of the 
     * synchronization. 
     *
     * @param    serverName    name of the server instance
     * @return   a synchronization response containing the names of the 
     *           application the given server instance is using
     *
     * @throws   IOException  if an error during health check
     * @throws   ConfigException if a configuration parsing error
     */
    public SynchronizationResponse ping(String serverName) 
            throws IOException, ConfigException, MBeanException;

    /**
     * Audit method called at the end of synchronization.
     *
     * @param   request  synchronization requests 
     *
     * @throws IOException  if an error during audit
     * @throws ConfigException if a configuration parsing error
     */
    public SynchronizationResponse audit(SynchronizationRequest[] request)
        throws IOException, ConfigException, MBeanException;

    /**
     * Assembles the repository zip for the given target.
     *
     * @param   target   target server instance or node agent 
     * @return    path to the newly created zip file
     *
     * @throws  SynchronizationException  if an error while assembling zip
     */
    public String createRepositoryZip(String target) 
            throws SynchronizationException, MBeanException;

}

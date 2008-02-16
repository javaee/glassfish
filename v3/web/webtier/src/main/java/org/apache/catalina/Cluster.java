

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.catalina;

import java.io.IOException;
import java.net.URL;
/**
 * A <b>Cluster</b> works as a Cluster client/server for the local host
 * Different Cluster implementations can be used to support different
 * ways to communicate within the Cluster. A Cluster implementation is
 * responsible for setting up a way to communicate within the Cluster
 * and also supply "ClientApplications" with <code>ClusterSender</code>
 * used when sending information in the Cluster and
 * <code>ClusterInfo</code> used for receiving information in the Cluster.
 *
 * @author Bip Thelin
 * @author Remy Maucherat
 * @author Filip Hanik
 * @version $Revision: 1.2 $, $Date: 2005/12/08 01:27:14 $
 */

public interface Cluster {

    // ------------------------------------------------------------- Properties

    /**
     * Return descriptive information about this Cluster implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();

    /**
     * Return the name of the cluster that this Server is currently
     * configured to operate within.
     *
     * @return The name of the cluster associated with this server
     */
    public String getClusterName();

    /**
     * Set the name of the cluster to join, if no cluster with
     * this name is present create one.
     *
     * @param clusterName The clustername to join
     */
    public void setClusterName(String clusterName);

    /**
     * Set the Container associated with our Cluster
     *
     * @param container The Container to use
     */
    public void setContainer(Container container);

    /**
     * Get the Container associated with our Cluster
     *
     * @return The Container associated with our Cluster
     */
    public Container getContainer();

    /**
     * The debug detail level for this Cluster
     *
     * @param debug The debug level
     */
    public void setDebug(int debug);

    /**
     * Returns the debug level for this Cluster
     *
     * @return The debug level
     */
    public int getDebug();

    /**
     * Set the protocol parameters.
     *
     * @param protocol The protocol used by the cluster
     */
    public void setProtocol(String protocol);

    /**
     * Get the protocol used by the cluster.
     *
     * @return The protocol
     */
    public String getProtocol();

    // --------------------------------------------------------- Public Methods

    /**
     * Create a new manager which will use this cluster to replicate its
     * sessions.
     *
     * @param name Name (key) of the application with which the manager is
     * associated
     */
    public Manager createManager(String name);

    // --------------------------------------------------------- Cluster Wide Deployments
    /**
     * Start an existing web application, attached to the specified context
     * path in all the other nodes in the cluster.
     * Only starts a web application if it is not running.
     *
     * @param contextPath The context path of the application to be started
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalArgumentException if the specified context path does
     *  not identify a currently installed web application
     * @exception IOException if an input/output error occurs during
     *  startup
     */
    public void startContext(String contextPath) throws IOException;


    /**
     * Install a new web application, whose web application archive is at the
     * specified URL, into this container with the specified context path.
     * A context path of "" (the empty string) should be used for the root
     * application for this container.  Otherwise, the context path must
     * start with a slash.
     * <p>
     * If this application is successfully installed, a ContainerEvent of type
     * <code>PRE_INSTALL_EVENT</code> will be sent to registered listeners
     * before the associated Context is started, and a ContainerEvent of type
     * <code>INSTALL_EVENT</code> will be sent to all registered listeners
     * after the associated Context is started, with the newly created
     * <code>Context</code> as an argument.
     *
     * @param contextPath The context path to which this application should
     *  be installed (must be unique)
     * @param war A URL of type "jar:" that points to a WAR file, or type
     *  "file:" that points to an unpacked directory structure containing
     *  the web application to be installed
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalStateException if the specified context path
     *  is already attached to an existing web application
     * @exception IOException if an input/output error was encountered
     *  during installation
     */
    public void installContext(String contextPath, URL war);

    /**
     * Stop an existing web application, attached to the specified context
     * path.  Only stops a web application if it is running.
     *
     * @param contextPath The context path of the application to be stopped
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalArgumentException if the specified context path does
     *  not identify a currently installed web application
     * @exception IOException if an input/output error occurs while stopping
     *  the web application
     */
    public void stop(String contextPath) throws IOException;
    
    /**
     * Notifies the cluster if a context is distributable or not
     * @param contextName - the name of the registed context
     * @param distributable - true means that the sessions will be replicated
     */
    public void setDistributable(String contextName, boolean distributable);
    

}

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

package com.sun.ejb.spi.sfsb.store;


/**
 * SFSBManagerConstants defines a set of "keys"
 * that can be used to populate / query values that
 * will be needed during SFSBManager creation.
 * 
 * @author Mahesh Kannan
 */
public interface SFSBStoreManagerConstants {

    /**
     * Name of the store manager. Used mainly for logging / debugging
     */
    public static String STORE_MANAGER_NAME =
        "com.sun.ejb.spi.sfsb.store.manager.name";

    /**
     * The unique prefix essentially contains some bits that
     *  is unique across VMs. For example, this can contain,
     *  in a clustered environment, a clusterID, nodeID etc.
     *  The prefix can be used to generate clusterwide unique
     *  session IDs.
     */
    public static String UNIQUE_PREFIX =
        "com.sun.ejb.spi.sfsb.unique.prefix";

    /**
     * The session group essentially contains some bits that
     *  can be used to group sessions. For example, this can
     *  contain the beanID (appID-moduleID-ejbID).
     *  The session group can be used to perform operations
     *  that affect all sessions for this bean
     */
    public static String SESSION_GROUP =
        "com.sun.ejb.spi.sfsb.session.group";

    /**
     * Defines the session timeout in seconds. Sessions not accessed
     *  for this duration can be removed and will no longer
     *  be available to clients
     */
    public static String SESSION_TIMEOUT_IN_SECONDS =
        "com.sun.ejb.spi.sfsb.session.timeout.in.seconds";

    /**
     * This will be used by File system sfsbManager
     *  This param could be null
     */
    public static String PASSIVATION_DIRECTORY_NAME =
        "com.sun.ejb.spi.sfsb.passivation.directory.name";


    /**
     * Defines the DataSource object that can be used to 
     *  obtained (possibly pooled) JDBC connections
     *  This will be used by StoreManagers that persists
     *  sessions using relational databases
     */
    public static String JDBC_DATASOURCE_INSTANCE =
        "com.sun.ejb.spi.sfsb.jdbc.datasource.instance";


    /**
     * Class Loader for this app
     */
    public static String CLASS_LOADER =
	"com.sun.ejb.spi.sfsb.classloader";

}

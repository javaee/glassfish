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

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.ejb.ee.sfsb.store;

import com.sun.enterprise.web.ShutdownCleanupCapable;
import com.sun.ejb.ee.sfsb.store.BaseSFSBStoreManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.WeakHashMap;
import java.util.Iterator;
import java.util.logging.Level;

/** <p>Title: HASFSBStoreManagerBase.java</p>
 * <p>Description: This class mainly impolements the ShutdoownCleanupCapable</p>
 *
 * @author Sridhar Satuloori <Sridhar.Satuloori@Sun.Com>
 * @version 1.0
 */

public abstract class HASFSBStoreManagerBase extends BaseSFSBStoreManager implements
    ShutdownCleanupCapable {
        /** holds the refrence to all the connections being used by ejb persistence layer.
         * So that they can be closed at the time of graceful shutdown
         */
    private Map _connectionsMap = Collections.synchronizedMap(new WeakHashMap(
        50));

    /** Adds the connectyion to the list of connections used by this manager
     * @param conn Connection used by this manager
     */
    public void putConnection(Connection conn) {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "In HASFSBStoreManagerBase.putConnection  :" +
                        _connectionsMap.size());
        }
        _connectionsMap.put(conn, null);
    }

    /** this will be called by ConnectionUtil when there is an event for shutdown
     * @return Returns the number of connections closed
     */
    public int doShutdownCleanup() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, "In HASFSBStoreManagerBase.doShutdownCleanup");
        }
        return this.closeAllConnections();
    }

    /** Closes all the connections used by this manager
     * @return Returns the number of connections closed by this manager
     */
    private int closeAllConnections() {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.log(Level.FINEST, "In HASFSBStoreManagerBase.closeAllConnections");
        }
        int count = 0;
        int iterations = 0;
        Set connections = _connectionsMap.keySet();
        Iterator it = connections.iterator();
        while (it.hasNext()) {
            iterations++;
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                    "In HASFSBStoreManagerBase.closeAllConnections:iteration" +
                    iterations);
            }
            Connection nextConn = (Connection) it.next();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "In HASFSBStoreManagerBase.nextConn=" + nextConn);
            }
            if (nextConn != null) {
                try {
                    nextConn.close();
                    count++;
                }
                catch (SQLException ex) {
                    //if any errors; give up trying to close other connections
                    _logger.log(Level.WARNING,"In HASFSBStoreManagerBase.closeAllConnections: failed " );
                    break;
                }
            }
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                "In HASFSBStoreManagerBase.closeAllConnections manager closed " +
                count + " connections");
        }
        return count;
    }

    /** this method closes any connections cached by store. This is not used in the EJB layer
     */
    public void doCloseCachedConnection() {

    }

}
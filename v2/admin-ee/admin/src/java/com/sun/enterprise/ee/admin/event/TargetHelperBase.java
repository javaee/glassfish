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
package com.sun.enterprise.ee.admin.event;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.config.ConfigException;

/**
 * Base class for resolving end points for a resource or application.
 * All resources (except pools) and applications are associated by
 * server instances by reference.  When a resource or application
 * is modified at domain level, implementation of this class will 
 * resolve the associated end points.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
abstract class TargetHelperBase {

    /**
     * Constructor.
     *
     * @param  target  flattened target of type 
     *    <type>|<name-of-resourceORapplication>|<type-of-resourceORapplication>
     * @param  ctx  config context
     */
    TargetHelperBase(String target, ConfigContext ctx) {

        StringTokenizer st = new StringTokenizer(target, DELIM);

        int tokens = st.countTokens();
        if (tokens == TOKEN_NUM) {
            String prefix = st.nextToken();
            _name = st.nextToken();
            _type = st.nextToken();
        } else {
            String msg = _strMgr.getString("invalid.target", target);
            throw new IllegalArgumentException(msg);
        }
        _context = ctx;
    }

    /**
     * Returns the type of the target.
     *
     * @return  type of the target
     */
    String getType() {
        return _type;
    }

    /**
     * Returns the name of the target.
     * 
     * @return  name of the target
     */
    String getName() {
        return _name;
    }

    /**
     * Helper method to convert servers to end points.
     * 
     * @param   servers  array of servers
     * @param   ctx   config context
     * @return  a list of end points for the given servers
     *
     * @throws  ConfigException  if an error while parsing the config
     */
    static EndPoint[] createEndPoints(Server[] servers, ConfigContext ctx) 
            throws ConfigException {

        EndPoint[] endPoints = null;

        if (servers != null) {
            endPoints = new EndPoint[servers.length];

            for (int i=0; i<servers.length; i++) {
                _logger.finest("[TargetHelper] Creating end point for " 
                                + servers[i].getName());

                endPoints[i] = new EndPoint(servers[i], ctx);
            }
        }

        return endPoints;
    }

    /**
     * Returns true if the given target string conforms to the 
     * resource target format:
     *    resources|<name-of-resource>|<type-of-resource>
     *
     * @param   target   flattened resource target 
     *
     * @return  true if it is a resource target
     */
    static boolean isResourceTarget(String target) {

        boolean tf = false;

        if ((target != null) && (target.startsWith(ServerTags.RESOURCES))) {

            StringTokenizer st = new StringTokenizer(target, DELIM);
            int tokens = st.countTokens();
            if (tokens == TOKEN_NUM) {
                tf = true;
            }
        }

        return tf;
    }

    /**
     * Returns true if the given target string conforms to the 
     * application target format:
     *    applications|<name-of-application>|<type-of-application>
     *
     * @param   target   flattened resource target 
     *
     * @return  true if it is a resource target
     */
    static boolean isApplicationTarget(String target) {

        boolean tf = false;

        if ((target != null) && (target.startsWith(ServerTags.APPLICATIONS))) {

            StringTokenizer st = new StringTokenizer(target, DELIM);
            int tokens = st.countTokens();
            if (tokens == TOKEN_NUM) {
                tf = true;
            }
        }

        return tf;
    }

    /**
     * Returns a list of end points for this target.
     *
     * @return  list of end points
     *
     * @throws  ConfigException  if an error while parsing the config
     */
    abstract EndPoint[] getEndPoints() throws ConfigException;

    // ---- INSTANCE VARIABLE(S) -----------------------------------------
    String _name                           = null;
    String _type                           = null;
    ConfigContext _context                 = null;
    static final String DELIM              = "|";
    static final int TOKEN_NUM             = 3;
    static final StringManager _strMgr     = 
            StringManager.getManager(TargetHelperBase.class);
    static Logger _logger = Logger.getLogger(EELogDomains.ADMIN_LOGGER);
}

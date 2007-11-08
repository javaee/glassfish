/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.ejb.cmp3;

import java.util.Collection;
import oracle.toplink.essentials.queryframework.DatabaseQuery;

/**
 * PUBLIC:
 * TopLInk specific EJB 3.0 query interface.  Provides the functionality defined in
 * javax.persistence.Query and adds access to the underlying database query for TopLink specific
 * functionality.
 */
public interface EJBQuery extends javax.persistence.Query {

    /**
     * PUBLIC:
     * Return the cached database query for this EJBQueryImpl.  If the query is
     * a named query and it has not yet been looked up, the query will be looked up
     * and stored as the cached query.
     */
    public DatabaseQuery getDatabaseQuery();

    /**
     * PUBLIC:
     * return the EntityManager for this query
     */
    public EntityManager getEntityManager();

    /**
     * PUBLIC:
     * Non-standard method to return results of a ReadQuery that has a containerPoliry
     * that returns objects as a collection rather than a List
     * @return Collection of results
     */
    public Collection getResultCollection();

    /**
     * PUBLIC:
     * Replace the cached query with the given query.
     */
    public void setDatabaseQuery(DatabaseQuery query);

}

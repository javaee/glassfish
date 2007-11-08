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
package oracle.toplink.essentials.queryframework;


import oracle.toplink.essentials.exceptions.DatabaseException;
import oracle.toplink.essentials.exceptions.OptimisticLockException;
import java.util.HashMap;
import oracle.toplink.essentials.internal.ejb.cmp3.base.EJBQueryImpl;
import oracle.toplink.essentials.sessions.Session;


/**
 * <b>Purpose</b>: 
 * A EJB3 placeholder Query object to store EJBQL strings so that processing the string is delayed 
 *  until Login<p>
 *
 * @author Chris Delahunt
 * @since TopLink Java Essentials
 */

public class EJBQLPlaceHolderQuery extends DatabaseQuery  {

    private String ejbQLString;
    private Boolean flushOnExecute;
    private HashMap hints;
    
    public EJBQLPlaceHolderQuery() {
    }
    public EJBQLPlaceHolderQuery(String ejbQLString) {
        this.ejbQLString=ejbQLString;
    }
    //buildEJBQLDatabaseQuery(queryString, session, hints, m_loader)
    public EJBQLPlaceHolderQuery(String name, String ejbql, HashMap hints) {
        this.name=name;
        this.ejbQLString=ejbql;
        this.flushOnExecute=null;
        this.hints=hints;
    }  
    
    public EJBQLPlaceHolderQuery(String name, String ejbql,  Boolean flushOnExecute, HashMap hints) {
        this.name=name;
        this.ejbQLString=ejbql;
        this.flushOnExecute=flushOnExecute;
        this.hints=hints;
    }    

    /**
     * INTERNAL:
     * Add the expression value to be included in the result.
     * EXAMPLE: reportQuery.addItem("name", expBuilder.get("firstName").toUpperCase());
     * The resultType can be specified to support EJBQL that adheres to the
     * EJB 3.0 spec.
     */
    public String getEJBQLString(){
        return ejbQLString;
    }
    public void setEJBQLString(String ejbQLString){
        this.ejbQLString = ejbQLString;
    }
    
    /**
     * INTERNAL:
     * Accessor methods for hints that would be added to the EJBQuery class and 
     * applied to the TopLink query.
     */
    public HashMap getHints(){
        return hints;
    }
    public void setHints(HashMap hints){
        this.hints = hints;
    }
    
    
    public DatabaseQuery processEjbQLQuery(Session session){
        ClassLoader classloader = session.getDatasourcePlatform().getConversionManager().getLoader();
        DatabaseQuery ejbquery = EJBQueryImpl.buildEJBQLDatabaseQuery(
            this.getName(), ejbQLString,  flushOnExecute, session, hints, classloader);
        ejbquery.setName(this.getName());
        return ejbquery;
    }
    
    
    
    /**
     * INTERNAL:
     * This should never be called and is only here because it is needed as an extension
     * to DatabaseQuery.  An exception should be thrown to warn users, but for now
     * it will process the EJBQL and execute the resulting query instead.
     *
     * @exception  DatabaseException - an error has occurred on the database.
     * @exception  OptimisticLockException - an error has occurred using the optimistic lock feature.
     * @return - the result of executing the query.
     */
    public Object executeDatabaseQuery() throws DatabaseException, OptimisticLockException{
        DatabaseQuery ejbquery = processEjbQLQuery(this.getSession());
        return ejbquery.executeDatabaseQuery();
    }
}

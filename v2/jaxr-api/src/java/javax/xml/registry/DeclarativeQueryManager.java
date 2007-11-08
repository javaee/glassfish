/*
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the License).  You may not use this file except in
* compliance with the License.
* 
* You can obtain a copy of the license at 
* https://glassfish.dev.java.net/public/CDDLv1.0.html or
* glassfish/bootstrap/legal/CDDLv1.0.txt.
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* Header Notice in each file and include the License file 
* at glassfish/bootstrap/legal/CDDLv1.0.txt.  
* If applicable, add the following below the CDDL Header, 
* with the fields enclosed by brackets [] replaced by
* you own identifying information: 
* "Portions Copyrighted [year] [name of copyright owner]"
* 
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/

package javax.xml.registry;

import java.util.*;
import javax.xml.registry.infomodel.*;

/**
 * This interface provides the ability to execute declarative queries (e.g. SQL)
 *
 * @author Farrukh S. Najmi
 */
public interface DeclarativeQueryManager extends QueryManager {

	/**
	 * Creates a Query object given a queryType (for example, QUERY_TYPE_SQL) and a String
	 * that represents a query in the syntax appropriate for queryType.
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @see Query#QUERY_TYPE_SQL
	 * @see Query#QUERY_TYPE_XQUERY
	 * @see Query#QUERY_TYPE_EBXML_FILTER_QUERY
	 * @param queryType	the type of query
	 * @param queryString	the query in its string representation
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 * @throws InvalidRequestException	If the JAXR provider validates query syntax (optional) and the sqlQuery is not valid
	 *
	 */
	Query createQuery(int queryType, String queryString) throws InvalidRequestException, JAXRException;

	/**
	 * Executes a query as specified by query parameter.
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param query	the query to be executed
	 * @return the BulkResponse that is the result of the query
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public BulkResponse executeQuery(Query query) throws JAXRException;


    /** @link dependency 
     * @label processes*/
    /*#Query lnkQuery;*/
}

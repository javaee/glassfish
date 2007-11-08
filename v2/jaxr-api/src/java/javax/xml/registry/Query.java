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

/**
 * The Query interface encapsulates a query in a declarative query language.
 * Currently a Query can be an SQL query or an ebXML Filter Query only.
 * In future support for other query languages such as XQL query may be added. 
 * The query must conform to a fixed schema as defined by the JAXR specification.
 *
 * @author Farrukh S. Najmi
 */
public interface Query {

	/**
	 * An SQL query type.
	 */
	public static final int QUERY_TYPE_SQL=0;

	/**
	 * A W3C XQuery type.
	 */
	public static final int QUERY_TYPE_XQUERY=1;

	/**
	 * An OASIS ebXML Registry XML Filter Query type.
	 */
	public static final int QUERY_TYPE_EBXML_FILTER_QUERY=2;
	
    /**
	 * Gets the type of Query (for example, QUERY_TYPE_SQL).
	 *
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @see Query#QUERY_TYPE_SQL
	 * @see Query#QUERY_TYPE_XQUERY
	 * @see Query#QUERY_TYPE_EBXML_FILTER_QUERY
	 * @return the type of query
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public int getType() throws JAXRException;

	/**
	 * Returns the String representing the query. For example,
	 * in the case of an SQL query, returns the SQL query as a string.
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @return the String representation for this query
	 *
	 */	
	public String toString();
}

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


package com.sun.xml.registry.uddi;

import javax.xml.registry.*;

/**
 * The Query interface encapsulates a query in a declarative query language.
 * Currently a Query can be an SQL query only.
 * In future support for other query languages such as XQL query may be added. 
 * The query must conform to a fixed schema as defined by the JAXR specification.
 *
 * @author Farrukh S. Najmi
 */
public class QueryImpl {

    /**
	 * Gets the type of Query (e.g. QUERY_TYPE_SQL)
	 *
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @see Query#QUERY_TYPE_SQL
	 * @see Query#QUERY_TYPE_XQUERY
	 * @return the type of query
	 */
	public int getType() throws JAXRException {
		return 0;
	}

	/**
	 * Must print the String representing the query. For example
	 * in case of SQL query prints the SQL query as a string.
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 */	
	public String toString() {
		return null;
	}
}

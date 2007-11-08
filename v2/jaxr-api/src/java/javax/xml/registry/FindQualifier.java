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
 * FindQualifier provides various constants
 * that identify options that effect find method behavior.
 * A JAXR provider may silently ignore any qualifiers marked as optional.
 * A JAXR provider must support qualifiers not marked optional.
 *
 * @see BusinessQueryManager
 */
public interface FindQualifier {

	//Based on UDDI V1.0 API
	
        /**
         * Specifies a hint to queries that they should use exact (case-insensitive) match when comparing String values.
         */
	public static final String EXACT_NAME_MATCH = "exactNameMatch";

        /**
         * Specifies a hint to queries that they should case-sensitive match when comparing String values.
         */
        public static final String CASE_SENSITIVE_MATCH = "caseSensitiveMatch";

        /**
         * Specifies a hint to queries that they should sort results by Name in ascending alpha-numeric order.
         */
	public static final String SORT_BY_NAME_ASC  = "sortByNameAsc";

        /**
         * Specifies a hint to queries that they should sort results by Name in descending alpha-numeric order.
         */
	public static final String SORT_BY_NAME_DESC = "sortByNameDesc";

        /**
         * Specifies a hint to queries that they should sort results by creation/modification date in ascending order.
         */
        public static final String SORT_BY_DATE_ASC  = "sortByDateAsc";

        /**
         * Specifies a hint to queries that they should sort results by creation/modification date in descending order.
         */
	public static final String SORT_BY_DATE_DESC = "sortByDateDesc";
	
        /**
         * Specifies a hint to queries that when multiple taxonomy values from the same ClassificationScheme are specified 
         * in the Classifications for the query then the candidate set of objects matching the query should include
         * any object that is classified by any one of the taxonomy values. For example, if a query includes Classifications with 
         * both Japan and Korea taxonomy values under the geography ClassificationScheme, then it should match objects that are 
         * classified by either Japan or Korea ClassificationNodes.
         */
	public static final String OR_LIKE_KEYS = "orLikeKeys";

        /**
         * Specifies a hint to queries that the candidate set of objects matching the query should include
         * any object that is classified by any one of the taxonomy values specified in any of the Classification for the query. 
         */
        public static final String OR_ALL_KEYS = "orAllKeys";

	/** Maps to UDDI combineCategoryBags. Required for JAXR UDDI providers, optional for all others. */
	public static final String COMBINE_CLASSIFICATIONS  = "combineClassifications";

	/** Maps to UDDI serviceSubset. Required for JAXR UDDI providers, optional for all others. */
	public static final String SERVICE_SUBSET = "serviceSubset";

	public static final String AND_ALL_KEYS  = "andAllKeys";

	/** Optional qualifier that allows matching strings by their sounds. */
	public static final String SOUNDEX = "soundex";
}

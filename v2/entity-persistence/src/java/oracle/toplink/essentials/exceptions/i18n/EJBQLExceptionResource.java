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
package oracle.toplink.essentials.exceptions.i18n;

import java.util.ListResourceBundle;

/**
 * INTERNAL:
 * English ResourceBundle for EJBQLException.
 *
 */
public class EJBQLExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "8001", "Syntax Recognition Problem parsing the query [{0}]. The parser returned the following [{1}]." },
                                           { "8002", "General Problem parsing the query [{0}]. The parser returned the following [{1}]." },
                                           { "8003", "The class [{0}] was not found. The parser returned the following [{1}]." },
                                           { "8004", "Error compiling the query [{0}], line {1}, column {2}: unknown identification variable [{3}]. The FROM clause of the query does not declare an identification variable [{3}]." },
                                           { "8005", "Error compiling the query [{0}]. A problem was encountered resolving the class name - The class [{1}] was not found." },
                                           { "8006", "Error compiling the query [{0}]. A problem was encountered resolving the class name - The descriptor for [{1}] was not found." },
                                           { "8007", "Error compiling the query [{0}]. A problem was encountered resolving the class name - The mapping for [{1}] was not found." },
                                           { "8008", "Error compiling the query [{0}]. A problem was encountered building the query expression - The expressionBuilder for [{1}] was not found." },
                                           { "8009", "Problem compiling the query [{0}]. The expression [{1}] is currently not supported." },
                                           { "8010", "General Problem parsing the query [{0}]." },
                                           { "8011", "Error compiling the query [{0}], line {1}, column {2}: invalid collection member declaration [{3}], expected collection valued association field." },
                                           { "8012", "Problem compiling the query [{0}]. Not yet implemented: {1}." },
                                           { "8013", "Error compiling the query [{0}], line {1}, column {2}: constructor class [{3}] not found." },
                                           { "8014", "Error compiling the query [{0}], line {1}, column {2}: invalid SIZE argument [{3}], expected collection valued association field." },
                                           { "8015", "Error compiling the query [{0}], line {1}, column {2}: invalid enum literal, the enum type {3} does not have an enum literal {4}." },
                                           { "8016", "Error compiling the query [{0}], line {1}, column {2}: invalid SELECT expression [{3}] for query with grouping [{4}]. Only aggregates, GROUP BY items or constructor expressions of these are allowed in the SELECT clause of a GROUP BY query." },
                                           { "8017", "Error compiling the query [{0}], line {1}, column {2}: invalid HAVING expression [{3}] for query with grouping [{4}]. The HAVING clause must specify search conditions over the grouping items or aggregate functions that apply to grouping items." },
                                           { "8018", "Error compiling the query [{0}], line {1}, column {2}: invalid multiple use of parameter [{3}] assuming different parameter types [{4}] and [{5}]." },
                                           { "8019", "Error compiling the query [{0}], line {1}, column {2}: multiple declaration of identification variable [{3}], previously declared as [{4} {3}]." },
                                           { "8020", "Error compiling the query [{0}], line {1}, column {2}: invalid {3} function argument [{4}], expected argument of type [{5}]." },
                                           { "8021", "Error compiling the query [{0}], line {1}, column {2}: invalid ORDER BY item [{3}] of type [{4}], expected expression of an orderable type." },
                                           { "8022", "Error compiling the query [{0}], line {1}, column {2}: invalid {3} expression argument [{4}], expected argument of type [{5}]." },
                                           { "8023", "Syntax error parsing the query [{0}]." },
                                           { "8024", "Syntax error parsing the query [{0}], line {1}, column {2}: syntax error at [{3}]." },
                                           { "8025", "Syntax error parsing the query [{0}], line {1}, column {2}: unexpected token [{3}]." },
                                           { "8026", "Syntax error parsing the query [{0}], line {1}, column {2}: unexpected char [{3}]." },
                                           { "8027", "Syntax error parsing the query [{0}], line {1}, column {2}: expected char [{3}], found [{4}]." },
                                           { "8028", "Syntax error parsing the query [{0}], line {1}, column {2}: unexpected end of query." },
                                           { "8029", "Error compiling the query [{0}], line {1}, column {2}: invalid navigation expression [{3}], cannot navigate expression [{4}] of type [{5}] inside a query." },
                                           { "8030", "Error compiling the query [{0}], line {1}, column {2}: unknown state or association field [{3}] of class [{4}]." },
                                           { "8031", "Error compiling the query [{0}], line {1}, column {2}: {3} of embedded entity {4} is not supported." },
                                           { "8032", "Error compiling the query [{0}], line {1}, column {2}: invalid access of attribute [{3}] in SET clause target [{4}], only state fields and single valued association fields may be updated in a SET clause." },
                                           { "8033", "Error compiling the query [{0}], line {1}, column {2}: invalid navigation expression [{3}], cannot navigate association field [{4}] in the SET clause target." },
                                           { "8034", "Error compiling the query [{0}]. Unknown abstract schema type [{1}]." },
                                           { "8035", "Error compiling the query [{0}], line {1}, column {2}: invalid enum equal expression, cannot compare enum value of type [{3}} with a non enum value of type [{4}]." },
                                           { "8036", "Error compiling the query [{0}], line {1}, column {2}: invalid navigation expression [{3}], cannot navigate collection valued association field [{4}]." },
                                           { "8037", "Error compiling the query [{0}], line {1}, column {2}: unknown abstract schema type [{3}]." },
                                           { "8038", "Error compiling the query [{0}], line {1}, column {2}: a problem was encountered resolving the class name - The class [{3}] was not found." },
    };

    /**
    * Return the lookup table.
    */
    protected Object[][] getContents() {
        return contents;
    }
}

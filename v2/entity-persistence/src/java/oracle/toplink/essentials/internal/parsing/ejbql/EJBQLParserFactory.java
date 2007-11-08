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
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
/* Copyright (c) 2005, 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Created to remove EJBQLCall's Antlr.jar compile dependency.  

   MODIFIED    (MM/DD/YY)
    gyorke      03/10/06 - 
    pkrogh      10/07/05 - 
    pkrogh      09/29/05 - 
    tware       09/22/05 - 
    gyorke      08/09/05 - gyorke_10-essentials-directory-creation_050808
    dmahar      08/05/05 - 
    cdelahun    04/29/05 - cdelahun_10_main_4324564_050429
    cdelahun    04/29/05 - Creation
 */

package oracle.toplink.essentials.internal.parsing.ejbql;

import oracle.toplink.essentials.internal.parsing.EJBQLParseTree;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

public class EJBQLParserFactory
{
  public EJBQLParserFactory(){}
  
  public EJBQLParser buildParserFor(String ejbqlString){
    return EJBQLParser.buildParserFor(ejbqlString);
  }
  public EJBQLParser parseEJBQLString(String ejbqlString){
    EJBQLParser parser = buildParserFor(ejbqlString);
    parser.parse();
    return parser;
  }

  /**
   * Populate the query using the information retrieved from parsing the EJBQL.
   */
  public void populateQuery(String ejbqlString, ObjectLevelReadQuery theQuery, AbstractSession session){
    EJBQLParser parser = parseEJBQLString(ejbqlString);
    EJBQLParseTree parseTree = parser.getParseTree();
    parseTree.populateQuery(theQuery, session);
  }
  
}

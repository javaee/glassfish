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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
 package com.sun.mfwk.agent.appserv.mapping;

 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.w3c.dom.Element;

 import javax.management.ObjectName; 
 import javax.management.MalformedObjectNameException;

  import com.sun.mfwk.agent.appserv.logging.LogDomains;

/*
 * Handler to determine whether the context of the given object name.
 */
 public class EjbContextHandler implements MappingHandler {

    /**
     * Returns the context of the given AS object name. This method determines 
     * the context by inspecting the given object name.
     *
     * @return String the context of the given object name. That is, whether the
     * given object name belongs to standalone ejb module or ejb module inside
     * an application
     */
     public String mappingHandler(String AS_ObjectNameStr, 
         Element element, MappingQueryService mappingQueryService) {
         Logger logger = LogDomains.getLogger();
         String ejbContext = null;

         try {
             ObjectName AS_ObjectName = new ObjectName(AS_ObjectNameStr);
             if(AS_ObjectName.getKeyProperty(APPLICATION) != null) {
                 if(AS_ObjectName.getKeyProperty(EJB_MODULE) != null) {
                     ejbContext = "ejb-module";
                 } else {
                     if(logger != null){
                         logger.log(Level.SEVERE, 
                             "Error - Not a valid AS Object Name");
                     }
                 }
             } else {
                 if(AS_ObjectName.getKeyProperty(STANDALONE_EJB_MODULE) != null) {
                     ejbContext = "standalone-ejb-module";
                 } else {
                     if(logger != null){
                         logger.log(Level.SEVERE, 
                             "Error - Not a valid AS Object Name");
                     }
                 }
             }
         }
         catch(MalformedObjectNameException exception) {
             if(logger != null){
                 logger.log(Level.SEVERE,
                     "Error while constructing ObjectName", exception);
             }
         }
         return ejbContext;
     } 


     static private String APPLICATION = "application"; 
     static private String EJB_MODULE = "ejb-module"; 
     static private String STANDALONE_EJB_MODULE = "standalone-ejb-module";
 }


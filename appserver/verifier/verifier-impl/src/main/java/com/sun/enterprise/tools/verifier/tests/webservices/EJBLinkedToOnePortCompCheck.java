/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.tools.verifier.tests.webservices;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import java.util.*;
import com.sun.enterprise.tools.verifier.tests.*;
import java.lang.reflect.*;

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids:  JSR109_WS_44; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: A session EJB must only be linked to by a single port-component.
 */
public class EJBLinkedToOnePortCompCheck  extends WSTest implements WSCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint descriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (!descriptor.implementedByEjbComponent()) {
           result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
           result.notApplicable(smh.getLocalString
                 (getClass().getName() + ".notapp",
                 "This is a JAX-RPC Service Endpoint"));
           return result;
        }

        if (isLinkedToSinglePortComp(getAllEndPointsInApp(descriptor),descriptor.getLinkName())) {
           result.addGoodDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
           result.passed(smh.getLocalString
                   (getClass().getName() + ".passed",
           "The session EJB associated with this end-point is linked to by a single port-component."));

        }
        else {
          // result.fail
          result.addErrorDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
          result.failed(smh.getLocalString
                (getClass().getName() + ".failed",
                "The session EJB associated with this end-point is linked to by a multiple port-components."));

        }

        return result;
    }

    Collection getAllEndPointsInApp(WebServiceEndpoint desc) {
       Collection endPoints = new Vector();
       Collection allWebServices = desc.getWebService().getWebServicesDescriptor().getWebServices();
       for (Iterator it = allWebServices.iterator(); it.hasNext();) {
           endPoints.addAll(((WebService)it.next()).getEndpoints());
       }

     return endPoints;
    }

    // the compLink here is either an ejb-link or a servlet-link
    boolean isLinkedToSinglePortComp(Collection endPoints, String compLink) {
       boolean single = true;
       boolean linkAlreadySeen = false;
       for (Iterator it = endPoints.iterator(); it.hasNext();) {
           String myCompLink = ((WebServiceEndpoint)it.next()).getLinkName();

           if (myCompLink.equals(compLink)) {
              if (!linkAlreadySeen) {
                 linkAlreadySeen = true;
              }
              else {
                 single = false;
                 break;
              }
           }
       }
     return single;
    }
 }


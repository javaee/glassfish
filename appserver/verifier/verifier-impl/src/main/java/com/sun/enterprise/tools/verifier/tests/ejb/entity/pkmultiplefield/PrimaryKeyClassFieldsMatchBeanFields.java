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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.pkmultiplefield;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbUtils;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.FieldDescriptor;

import java.lang.reflect.Field;
import java.util.Vector;

/** 
 * Enterprise Java Bean primary key maps to multiple fields in the Entity bean
 * class test.  
 *
 * The primkey-field element is not used if the primary key maps to multiple 
 * container-managed fields (i.e. the key is a compound key). In this case, the 
 * fields of the primary key class must be public, and their names must 
 * correspond to the field names of the entity bean class that comprise the key.
 */
public class PrimaryKeyClassFieldsMatchBeanFields extends EjbTest implements EjbCheck { 


    /**
     * Enterprise Java Bean primary key maps to multiple fields in the Entity bean
     * class test.  
     *
     * The primkey-field element is not used if the primary key maps to multiple 
     * container-managed fields (i.e. the key is a compound key). In this case, the 
     * fields of the primary key class must be public, and their names must 
     * correspond to the field names of the entity bean class that comprise the key.
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	// fields of the primary key class names must correspond to the 
	// field names of the entity bean class that comprise the key.
	if (descriptor instanceof EjbEntityDescriptor) {
	    String persistence =
		((EjbEntityDescriptor)descriptor).getPersistenceType();
	    if (EjbEntityDescriptor.CONTAINER_PERSISTENCE.equals(persistence)) {
 
		// do we have  primekey that maps to single or multiple fields in entity
		// bean class?  if primekey-field exist, then primekey maps to single
		// field in entity bean class and this test in notApplicable
		try {
                    FieldDescriptor fd = ((EjbCMPEntityDescriptor)descriptor).getPrimaryKeyFieldDesc();
                    if (fd != null) {
                        String pkf = fd.getName();
                        if (pkf.length() > 0) {
			    // N/A case
			    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			    result.notApplicable(smh.getLocalString
					         (getClass().getName() + ".notApplicable2",
					          "Entity Bean [ {0} ] with primekey-field non-blank, test not applicable.",
					          new Object[] {descriptor.getEjbClassName()}));
                        }
		    } else {
			try {
			    VerifierTestContext context = getVerifierContext();
			    ClassLoader jcl = context.getClassLoader();
			    Class c = Class.forName(((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName(), false, getVerifierContext().getClassLoader());
			    Field [] fields = c.getDeclaredFields();
      
			    Vector beanFields = ((EjbDescriptor)descriptor).getFieldDescriptors();
			
			    boolean oneFailed = false;
			    boolean badField = false;
			    for (int i = 0; i < fields.length; i++) {
				badField = false;
				if (EjbUtils.isPKFieldMatchingBeanFields(fields[i],beanFields)) {
				    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
				    result.addGoodDetails(smh.getLocalString
					  (getClass().getName() + ".passed1",
					   "Valid: Field [ {0} ] defined within primary key class [ {1} ] does correspond to the field names of the entity bean class [ {2} ] that comprise the key.",
					   new Object[] {fields[i].getName(),((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName(),descriptor.getEjbClassName()}));
				    continue;
				} else {
				    if (!oneFailed) {
					oneFailed = true;
				    }
				    badField = true;
				}
          
				if (badField == true) {
				    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
				    result.failed(smh.getLocalString
						  (getClass().getName() + ".failed",
						   "Error: Field [ {0} ] defined within primary key class [ {1} ] does not correspond to the field names of the entity bean class [ {2} ] that comprise the key.",
						   new Object[] {fields[i].getName(),((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName(),descriptor.getEjbClassName()}));
				}
			    }
			    if (!oneFailed) {
				result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
				result.passed(smh.getLocalString
					      (getClass().getName() + ".passed",
					       "This primary key class [ {0} ] has defined all fields which correspond to the field names of the entity bean class [ {1} ] that comprise the key.",
					       new Object[] {((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName(),descriptor.getEjbClassName()}));
			    }
			} catch (ClassNotFoundException e) {
			    Verifier.debug(e);
			    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			    result.failed(smh.getLocalString
					  (getClass().getName() + ".failedException",
					   "Error: [ {0} ] class or [ {1} ] class not found.",
					   new Object[] {((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName(),((EjbEntityDescriptor)descriptor).getEjbClassName()}));
                        } catch (Throwable t) {
			    result.addWarningDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                            result.warning(smh.getLocalString
                                (getClass().getName() + ".warningException",
                                 "Warning: [ {0} ] class encountered [ {1} ]. Cannot access fields of class [ {2} ] which is external to [ {3} ].",
                                 new Object[] {(descriptor).getEjbClassName(),t.toString(), t.getMessage(), descriptor.getEjbBundleDescriptor().getModuleDescriptor().getArchiveUri()}));
			}  
		    }
		} catch (NullPointerException e) {
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
					 (getClass().getName() + ".failedException2",
					  "Error: Primkey field not defined within [ {0} ] bean.",
					  new Object[] {descriptor.getName()}));
		}
		return result;
 
	    } else { //if (BEAN_PERSISTENCE.equals(persistence)
		result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable1",
				      "Expected [ {0} ] managed persistence, but [ {1} ] bean has [ {2} ] managed persistence.", 
				      new Object[] {EjbEntityDescriptor.CONTAINER_PERSISTENCE,descriptor.getName(),persistence}));
		return result;
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "{0} expected {1} bean, but called with {2}.",
				  new Object[] {getClass(),"Entity","Session"}));
	    return result;
	}
    }
}

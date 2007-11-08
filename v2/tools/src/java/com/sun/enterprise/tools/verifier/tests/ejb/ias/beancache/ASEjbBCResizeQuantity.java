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

package com.sun.enterprise.tools.verifier.tests.ejb.ias.beancache;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import java.util.*;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

import com.sun.enterprise.tools.common.dd.ejb.*;

/** ejb [0,n]
 *    bean-cache ?
 *        max-cache-size ? [String]
 *        resize-quantity ? [String]
 *        is-cache-overflow-allowed ? [String]
 *        cache-idle-timout-in-seconds ? [String]
 *        removal-timeout-in-seconds ? [String]
 *        victim-selection-policy ? [String]
 *
 * The max-cache-size specifies the maximum number of beans in the cache.
 * Valid values are between 1 and MAX_INT
 */
public class ASEjbBCResizeQuantity extends ASEjbBeanCache
{
    
    public Result check(EjbDescriptor descriptor) 
    {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);

        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        String ejbName = null;
        Ejb testCase = null;
        boolean oneFailed = false;
        if(ejbJar!=null)
        {
            getBeanCache(descriptor,ejbJar);
            if(beanCache!=null)
            {
                try
                {
                    String resizeQtyStr = beanCache.getResizeQuantity();
                    if(resizeQtyStr!=null)
                    {
                        if(resizeQtyStr.length()==0)
                        {
                            result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                "FAILED [AS-EJB bean-cache] : resize-quantity cannot be empty. It should be between 1 and {0}",new Object[]{new Integer(Integer.MAX_VALUE)}));
                        }
                        else
                        {
                            int resizeQty = Integer.valueOf(resizeQtyStr).intValue();
                            if(resizeQty < 1 || resizeQty > Integer.MAX_VALUE)
                            {
                                result.failed(smh.getLocalString(getClass().getName()+".failed2",
                                    "FAILED [AS-EJB bean-cache] : resize-quantity cannot be less than 1 or greater than {0}", new Object[]{new Integer(Integer.MAX_VALUE)}));
                            }
                            else
							{

							   try{
								   String maxCacheSizeStr = beanCache.getMaxCacheSize();
								   if(maxCacheSizeStr != null && maxCacheSizeStr.length() >0)
								   {
								       int maxCacheSize = Integer.valueOf(beanCache.getMaxCacheSize()).intValue();
									   if(maxCacheSize >= 1 && maxCacheSize <= Integer.MAX_VALUE)
									   {
									       if(resizeQty <= maxCacheSize)
										   {
										       result.passed(smh.getLocalString(getClass().getName()+".passed1","PASSED [AS-EJB bean-cache] : resize-quantity is {0} and is less than max-cache-size {1}",new Object[]{(new Integer(resizeQty)),new Integer(maxCacheSize)}));
										   }
										   else
										   {
										       result.failed(smh.getLocalString(getClass().getName()+".failed3","FAILED [AS-EJB bean-cache] : resize-quantity {0} should be less than max-cache-size {1}",new Object[]{new Integer(resizeQty), new Integer(maxCacheSize)}));
										   }
									   }else
									   {
									       result.failed(smh.getLocalString(getClass().getName()+".failed4","FAILED [AS-EJB bean-cache] : resize-quantity should be less than max-cache-size and max-cache-size is not a valid integer"));
									   }
								   }
								   else
								   {
								       result.passed(smh.getLocalString(getClass().getName()+".passed2","PASSED [AS-EJB bean-cache] : resize-quantity is {0}",new Object[]{(new Integer(resizeQty))}));
								   }

						        }catch(NumberFormatException nfex){
								    result.failed(smh.getLocalString(getClass().getName()+".failed3","FAILED [AS-EJB bean-cache] : resize-quantity should be less than max-cache-size and max-cache-size is not a valid integer"));
								}
							}
                        }
                    }
                    else
                    {
                        result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
                          "NOT APPLICABLE [AS-EJB bean-cache] : resize-quantity element is not defined"));
                    }
                }
                catch(NumberFormatException nfex)
                {
                    Verifier.debug(nfex);
                    result.failed(smh.getLocalString(getClass().getName()+".failed5", 
                        "FAILED [AS-EJB bean-cache] : resize-quantity is invalid. It should be a valid integer"));
                }
            }
            else
            {
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable2",
                    "NOT APPLICABLE [AS-EJB] : bean-cache element not defined"));
            }
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                                   ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
        }
        return result;
    }
}


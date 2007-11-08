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
 *  DeletionBean.java
 */

package com.sun.jbi.jsf.bean;

import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.SharedConstants;
import com.sun.jbi.jsf.util.I18nUtilities;
import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import com.sun.jbi.ui.common.JBIRemoteException;
import com.sun.jbi.ui.common.JBIManagementMessage;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class DeletionBean
{
     /**
     * Controls printing of diagnostic messages to the log
     */
	private static Logger sLog = JBILogger.getInstance();

    public DeletionBean()
    {
        mJac = BeanUtilities.getClient();
    }

    public Properties delete (Properties aRequestResponse, 
                              boolean aRetainFlag, 
                              List aTargetsList)
    {
        sLog.fine("DeletionBean.delete(" + aRequestResponse + 
                               ", " + aTargetsList + ")");

        Properties result = aRequestResponse;
        for (Iterator it = aTargetsList.iterator(); it.hasNext();)
        {
            String target = (String)it.next();
            result = doDeleteOnTarget (aRequestResponse, aRetainFlag, target);
        }

        sLog.fine("DeletionBean.delete(...), result=" + result);
        return result;
    }


    /**
     * Will make sure the Service Assembly or Component is shutdown.
     * @param aType specifies the type of the component/service assembly
     * @param aName the name of the component/service assembly
     * @param aTarget the target that the component/service assembly resides on
     * @return the results string if shutdown was performed
     */
    private String shutItDown (String aType, String aName, String aTarget) throws JBIRemoteException
    {
        String componentDetails = "";
        String state = "";
        if (JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE.equals(aType))
        {
            componentDetails = mJac.showServiceAssembly(aName,
                                                        SharedConstants.NO_STATE_CHECK,
                                                        SharedConstants.NO_COMPONENT_CHECK,
                                                        aTarget);
            if (componentDetails != null)
            {
                List list = ServiceAssemblyInfo.readFromXmlTextWithProlog(componentDetails);
                Iterator itr = list.iterator();
                ServiceAssemblyInfo saInfo = (ServiceAssemblyInfo) itr.next();
                List suInfoList = saInfo.getServiceUnitInfoList();
                state = saInfo.getState();
                if (!(state.equalsIgnoreCase(JBIComponentInfo.SHUTDOWN_STATE)))
                {
                    componentDetails = mJac.shutdownServiceAssembly(aName,
                                                                    aTarget);
                }
            }
        }
        else if ((JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(aType)) ||
                 (JBIConstants.JBI_SERVICE_ENGINE_TYPE.equals(aType)))
        {
            if (JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(aType))
            {
                componentDetails = mJac.showBindingComponent(aName,
                                                             SharedConstants.NO_STATE_CHECK,
                                                             SharedConstants.NO_LIBRARY_CHECK,
                                                             SharedConstants.NO_DEPLOYMENT_CHECK,
                                                             aTarget);
            }
            else if (JBIConstants.JBI_SERVICE_ENGINE_TYPE.equals(aType))
            {
                componentDetails = mJac.showServiceEngine(aName,
                                                          SharedConstants.NO_STATE_CHECK,
                                                          SharedConstants.NO_LIBRARY_CHECK,
                                                          SharedConstants.NO_DEPLOYMENT_CHECK,
                                                          aTarget);
            }
            if (componentDetails != null)
            {
                List list = JBIComponentInfo.readFromXmlText(componentDetails);
                Iterator it = list.iterator();
                JBIComponentInfo info = ((JBIComponentInfo)it.next());
                state = info.getState();
                if (!(state.equalsIgnoreCase(JBIComponentInfo.SHUTDOWN_STATE)))
                {
                    componentDetails = mJac.shutdownComponent(aName,
                                                              SharedConstants.NO_FORCE_DELETE,
                                                              aTarget);
                }
            }
        }
        return componentDetails;
    }


    private Properties doDeleteOnTarget (Properties aRequestResponse, 
                                         boolean aRetainFlag, 
                                         String aTarget)
    {
	    sLog.fine("DeletionBean.doDeleteOnTarget(" + aRequestResponse + ", " + aTarget + ")");

        Properties result = aRequestResponse;
        String jbiName = (String)aRequestResponse.getProperty(SharedConstants.KEY_NAME);
        String jbiType = (String)aRequestResponse.getProperty(SharedConstants.KEY_TYPE);

        String deletionResult = "";
        String shutdownResult = "";

        try
        {
            if (null != mJac)
            {
                if (JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE.equals(jbiType))
                {
                    //shutdownResult = shutItDown (jbiType, jbiName, aTarget);
                    deletionResult = mJac.undeployServiceAssembly(jbiName, 
                                                                  SharedConstants.NO_FORCE_DELETE,
                                                                  aRetainFlag, 
                                                                  aTarget);
                    result.setProperty(SharedConstants.SUCCESS_RESULT, deletionResult);
                }
                else if ((JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(jbiType))
                         ||(JBIConstants.JBI_SERVICE_ENGINE_TYPE.equals(jbiType)))
                {
                    //shutdownResult = shutItDown (jbiType, jbiName, aTarget);
                    deletionResult = mJac.uninstallComponent(jbiName, 
                                                             SharedConstants.NO_FORCE_DELETE,
                                                             aRetainFlag,
                                                             aTarget);
                    result.setProperty(SharedConstants.SUCCESS_RESULT, deletionResult);
                }
                else if (JBIConstants.JBI_SHARED_LIBRARY_TYPE.equals(jbiType))
                {
                    deletionResult = mJac.uninstallSharedLibrary (jbiName, 
                                                                  SharedConstants.NO_FORCE_DELETE,
                                                                  aRetainFlag,
                                                                  aTarget);
                    result.setProperty(SharedConstants.SUCCESS_RESULT, deletionResult);
                }
                else
                {
                    // error unexpected or missing JBI type
                    result.setProperty(SharedConstants.FAILURE_RESULT, "unexpected or missing JBI type=" + jbiType);
                }
            }
            else
            {
                // error: no JBI admin common client/connection
                result.setProperty(SharedConstants.FAILURE_RESULT, "missing JBI admin common client");
            }
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
        {
            jbiRemoteEx.printStackTrace(System.err);
            JBIManagementMessage mgmtMsg = BeanUtilities.extractJBIManagementMessage(jbiRemoteEx);
            if (mgmtMsg == null)
            {
                String internalErrorMsg = I18nUtilities.getResourceString ("jbi.internal.error.invalid.remote.exception");
                result.setProperty(SharedConstants.INTERNAL_ERROR, internalErrorMsg);
            }
            else
            {
                String msg = mgmtMsg.getMessage();
                result.setProperty(SharedConstants.FAILURE_RESULT, msg);
            }
	           sLog.fine("Deletion.doDeleteOnTarget(...), result=" + result);
        }
        return result;
    }

    private JBIAdminCommands mJac;

}


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
 *  OperationBean.java
 */

package com.sun.jbi.jsf.bean;

import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.I18nUtilities;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.SharedConstants;
import com.sun.jbi.ui.common.JBIManagementMessage;
import com.sun.jbi.ui.common.JBIRemoteException;
import com.sun.jbi.ui.common.JBIAdminCommands;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

public class OperationBean
{

    public final static String OPERATION_SHUT_DOWN         = "Shut Down"; // not i18n
    public final static String OPERATION_START             = "Start";     // not i18n
    public final static String OPERATION_STOP              = "Stop";      // not i18n

   //Get Logger to log fine mesages for debugging
    private static Logger sLog = JBILogger.getInstance();

    public OperationBean()
    {
        mJac = BeanUtilities.getClient();
    }

    public Properties shutDown(Properties aRequestResponse, List aTargetsList)
    {
        sLog.fine("OperationBean.shutDown(" + aRequestResponse +
                               ", " + aTargetsList + ")");
        Properties result =
        doCommand(OPERATION_SHUT_DOWN, aRequestResponse, aTargetsList);
        sLog.fine("OperationBean.shutDown(), result=" + aRequestResponse);
        return result;
    }

    public Properties start(Properties aRequestResponse, List aTargetsList)
    {
        sLog.fine("OperationBean.start(" + aRequestResponse +
                               ", " + aTargetsList + ")");

        Properties result =
        doCommand(OPERATION_START, aRequestResponse, aTargetsList);
        sLog.fine("OperationBean.start(), result=" + aRequestResponse);
        return result;
    }

    public Properties stop(Properties aRequestResponse, List aTargetsList)
    {
        sLog.fine("OperationBean.stop(" + aRequestResponse +
                               ", " + aTargetsList + ")");
        Properties result =
        doCommand(OPERATION_STOP, aRequestResponse, aTargetsList);
        sLog.fine("OperationBean.stop(), result=" + aRequestResponse);
        return result;
    }

    public String getValueShutDown()
    {
        return OPERATION_SHUT_DOWN;
    }

    public String getValueStart()
    {
        return OPERATION_START;
    }

    public String getValueStop()
    {
        return OPERATION_STOP;
    }

    private Properties doCommand(String anOperation, Properties aRequestResponse, List aTargetsList)
    {
        int targetDownCount = 0;
	String downTargetsList = "";

        sLog.fine("OperationBean.doCommand(" + anOperation + ", " + aRequestResponse +
                               ", " + aTargetsList + ")");
        Properties result = aRequestResponse;

        String jbiName = (String)
                         aRequestResponse.getProperty(SharedConstants.KEY_NAME);
        String jbiType = (String)
                         aRequestResponse.getProperty(SharedConstants.KEY_TYPE);

        String lifeCycleResult = null;

        if (null != mJac)
        {
            Iterator targetsIterator = aTargetsList.iterator();
            while (targetsIterator.hasNext())
            {
                Properties targetProp = (Properties) targetsIterator.next();
                String target = targetProp.getProperty(SharedConstants.KEY_NAME,
                                                       JBIAdminCommands.SERVER_TARGET_KEY); // PE defaults to 'server'

		sLog.fine("OperationBean.doCommand(...), target=" + target);
                try
                {
                    sLog.fine("OperationBean.doCommand(...), target=" + target);

                    // Before processing the lifecycle operation, check to see if the target is up.
                    // If the target is down, then we need to inc the down count. This will cause
                    // us to create the warning message after the operation has completed.
                    if (!mJac.isTargetUp(target))
                    {
                        targetDownCount++;
			if ("".equals(downTargetsList)) // first down target
			    {
				downTargetsList = target;
			    }
			else // add subsequent targets
			    {
				downTargetsList = downTargetsList + ", " + target;
			    }
                    }
		    sLog.fine("OperationBean.doCommand(...), target=" + target + ", isTargetUp=" + mJac.isTargetUp(target) + 
			      ", targetDownCount=" + targetDownCount + ", downTargetsList=" + downTargetsList);
		    
                    if (JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE.equals(jbiType))
                    {
                        if (OPERATION_SHUT_DOWN.equals(anOperation))
                        {
                            lifeCycleResult = mJac.shutdownServiceAssembly(jbiName, target);
                            result.setProperty(SharedConstants.SUCCESS_RESULT, lifeCycleResult);
                        }
                        else if (OPERATION_START.equals(anOperation))
                        {
                            lifeCycleResult = mJac.startServiceAssembly(jbiName, target);
                            result.setProperty(SharedConstants.SUCCESS_RESULT, lifeCycleResult);
                        }
                        else if (OPERATION_STOP.equals(anOperation))
                        {
                            lifeCycleResult = mJac.stopServiceAssembly(jbiName, target);
                            result.setProperty(SharedConstants.SUCCESS_RESULT, lifeCycleResult);
                        }
                        else
                        {
                            // error: unexpected or missing lifecycle state
                            result.setProperty(SharedConstants.FAILURE_RESULT, I18nUtilities.getResourceString("jbi.operate.missing.state") + anOperation);
                        }
                    }

                    else if ((JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(jbiType))
                             ||(JBIConstants.JBI_SERVICE_ENGINE_TYPE.equals(jbiType)))
                    {
                        if (OPERATION_SHUT_DOWN.equals(anOperation))
                        {
                            lifeCycleResult = mJac.shutdownComponent(jbiName, target);
                            sLog.fine("OperationBean.doCommand(...), shutdownComponent jbiName=" + jbiName + ", lifeCycleResult=" + lifeCycleResult);
                            result.setProperty(SharedConstants.SUCCESS_RESULT, lifeCycleResult);
                        }
                        else if (OPERATION_START.equals(anOperation))
                        {
                            lifeCycleResult = mJac.startComponent(jbiName, target);
                            sLog.fine("OperationBean.doCommand(...), startComponent jbiName=" + jbiName + ", lifeCycleResult=" + lifeCycleResult);
                            result.setProperty(SharedConstants.SUCCESS_RESULT, lifeCycleResult);
                        }
                        else if (OPERATION_STOP.equals(anOperation))
                        {
                            lifeCycleResult = mJac.stopComponent(jbiName, target);
                            sLog.fine("OperationBean.doCommand(...), stopComponent jbiName=" + jbiName + ", lifeCycleResult=" + lifeCycleResult);
                            result.setProperty(SharedConstants.SUCCESS_RESULT, lifeCycleResult);
                        }
                        else
                        {
                            // error: unexpected or missing lifecycle state
                            result.setProperty(SharedConstants.FAILURE_RESULT, I18nUtilities.getResourceString ("jbi.operate.missing.state") + anOperation);
                        }
                    }
                    else if (JBIConstants.JBI_SHARED_LIBRARY_TYPE.equals(jbiType))
                    {
                        // error: shared-library has no lifecycle
                        result.setProperty(SharedConstants.FAILURE_RESULT, I18nUtilities.getResourceString("jbi.operate.lib.error"));
                    }
                    else
                    {
                        // error unexpected or missing jbiType
                        result.setProperty(SharedConstants.FAILURE_RESULT, I18nUtilities.getResourceString("jbi.operate.missing.type") + jbiType);
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
                        result.setProperty(SharedConstants.FAILURE_RESULT, internalErrorMsg);
                        sLog.fine("OperationBean.doCommand(...), catch (empty) jbiRemoteEx; mgmtMsg=null, intnernalErrorMsg=" + internalErrorMsg);
                    }
                    else
                    {
                        String msg = mgmtMsg.getMessage();
			msg = msg.replaceAll("\n","<br />");
                        result.setProperty(SharedConstants.FAILURE_RESULT, msg);
                        sLog.fine("OperationBean.doCommand(...), catch (valid) jbiRemoteEx; mgmtMsg.getMessage=" + msg);
                    }
                }
                catch (Exception e)
                {
                    String internalErrorMsg = I18nUtilities.getResourceString ("jbi.internal.error.invalid.remote.exception");
                    result.setProperty(SharedConstants.INTERNAL_ERROR, internalErrorMsg);
                    sLog.fine("OperationBean.doCommand(...), catch (empty) Exception; mgmtMsg=null, intnernalErrorMsg=" + internalErrorMsg);
                }
            }

	    // If any target(s) are reported down, create the warning message and store it in the result properties 
	    if (targetDownCount > 0)
		{
		    Object[] args = {downTargetsList, jbiName};
		    String resultString = "";
		    if (targetDownCount == 1) // use singular messages
			{
			    result.setProperty(SharedConstants.WARNING_SUMMARY, I18nUtilities.getResourceString("jbi.operation.deferred.target.summary"));
			    resultString = GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.operation.deferred.target.down.msg"), args);
			}
		    else // more than one target down, use plural messages
			{
			    result.setProperty(SharedConstants.WARNING_SUMMARY, I18nUtilities.getResourceString("jbi.operation.deferred.targets.summary"));
			    resultString = GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.operation.deferred.targets.down.msg"), args);
			}
		    result.setProperty(SharedConstants.WARNING_RESULT, resultString);
                    }

        }
        else
        {
            // error: no JBI admin common client/connection
            result.setProperty(SharedConstants.FAILURE_RESULT, I18nUtilities.getResourceString ("jbi.missing.admin.client"));
        }

       sLog.fine("OperationBean.doCommand(" + anOperation + ", " + aRequestResponse +
                               ", " + aTargetsList + "), result=" + result);

        return result;
    }

    /** 
     * validates a string input field contains valid contents based on property metadata
     * @param aContext with the Faces Context
     * @param aComponent to be validated
     * @param aValue String to be validated
     * @throws ValidatorException if the value is invalid
     */
    public void validateField(FacesContext aContext, UIComponent aComponent, Object aValue)
	throws ValidatorException
    {
	sLog.fine("OperationBean.validateField(" + aContext + 
		  ", aComponent=" + aComponent + ", aValue=" + aValue);
	String alertDetail = "";

	// look up propertyName based on UIComponent mapping
	FacesContext facesContext = FacesContext.getCurrentInstance();
        Map sessionMap = 
	    (Map) facesContext.getApplication().createValueBinding("#{sessionScope}").getValue(facesContext);
	String textFieldId = aComponent.getClientId(facesContext);
	String propertyName = (String) sessionMap.get(textFieldId);

	sLog.finer("OperationBean.validateField before sessionMap=" + sessionMap); 

	boolean isValid = false;
	if (aValue instanceof String)
	    {
		
		// if found, validate value against min/max
		if (null != propertyName)
		    {
			isValid = validatePropertyNameValueInRange(propertyName, (String)aValue);
		    }
	    }
	sLog.fine("OperationBean.validateField(...), aValue=" + aValue + 
		  ", isValid=" + isValid);
	if (!isValid)
	    {
		// Replace the general anticipatory error for this invalid property
		// with a more specific error.
		Properties alertDetailProperties = 
		    (Properties) sessionMap.get("jbiAlertDetailProperties");
		Properties propertyLabels =
		    (Properties) sessionMap.get("jbiAlertPropertyLabels");
		if (null != alertDetailProperties)
		    {
			Properties metadata = getJbiRtConfigMetadata(propertyName);
			String failedValidationRangeMessage =
			    getFailedValidationRangeMessage(metadata, propertyLabels);

			alertDetailProperties.put(propertyName, failedValidationRangeMessage);

			// replace alert detail message with now-updated set of messages.
			for(Object object : alertDetailProperties.keySet())
			    {
				String k = (String) object;
				String v = (String) alertDetailProperties.getProperty(k);
				sLog.finer("OperationBean.validateField(...), alertDetailProperties k=" + k + ", v=" + v);
				if (!"".equals(v))
				    {
					alertDetail = v + "<br />" + alertDetail;
				    }
			    }
		    }
		AlertBean alertBean = BeanUtilities.getAlertBean();
		alertBean.setAlertType("error");
		alertBean.setAlertSummary(I18nUtilities
					  .getResourceString ("jbi.configure.component.alert.summary"));
		alertBean.setAlertDetail(alertDetail);


		String msgString = textFieldId;
		sLog.fine("OperationBean.validateField(" + aContext + ", " +
			  aComponent + ", " + aValue + "), msgString=" + msgString);
		FacesMessage facesMessage = new FacesMessage(msgString);
		throw new ValidatorException(facesMessage);
	    }
	sLog.fine("OperationBean.validateField(" + aContext + 
		  ", aComponent=" + aComponent + ", aValue=" + aValue + 
		  ") validation passed");
    }

    private void showAlert(String aType, String aSummaryMessage, String aDetailsMessage)
    {
	sLog.fine("OperationBean.showAlert(" + aType + 
		  ", " + aSummaryMessage +
		  ", " + aDetailsMessage + ")");

	AlertBean alertBean = BeanUtilities.getAlertBean();
	alertBean.setAlertType(aType);
	alertBean.setAlertSummary(aSummaryMessage);
	alertBean.setAlertDetail(aDetailsMessage);
    }

    private boolean validatePropertyNameValueInRange(String aPropertyName, String aValue)
    {
	sLog.finer("OperationBean.validatePropertyNameValueInRange(" + aPropertyName + 
		   "), " + aValue + ")");
	boolean result = false;
	Properties metadata = getJbiRtConfigMetadata(aPropertyName);


	try
	    {
		int value = Integer.parseInt(aValue); 

		if (null != metadata)
		    {
			int minValue = getInteger(metadata, "minValue");
			int maxValue = getInteger(metadata, "maxValue");
			
			if ((value >= minValue)
			    &&(value <= maxValue))
			    {
				result = true;
			    }
		    }
	    }
	catch (NumberFormatException nfEx)
	    {
		sLog.fine("OperationBean.validatePropertyNameValueInRange(" + aPropertyName + 
			  ", " + aValue + "), caught nfEx=" + nfEx);
	    }

	sLog.fine("OperationBean.validatePropertyNameValueInRange(" + aPropertyName + 
		  ", " + aValue + "), result=" + result);
	return result;
    }

    private String getFailedValidationRangeMessage(Properties aMetadata, Properties aPropertyLabels)
    {
	sLog.fine("OperationBean.getFailedValidationRangeMessage(" + aMetadata + 
		  ", " + aPropertyLabels + ")");
	String result = I18nUtilities.getResourceString("jbi.root.configuration.failed.validation.internal.error.message");

	if ((null != aMetadata)
	    &&(null != aPropertyLabels))
	    {
		String name = (String) aMetadata.get("name"); 
		if (null != name)
		    {
			String label = (String) aPropertyLabels.get(name); // convert name to use the displayed label instead
			String minValue = (String)  aMetadata.get("minValue");
			String maxValue = (String) aMetadata.get("maxValue");
			Object[] args = {label, minValue, maxValue};
			result = 
			    GuiUtil.getMessage(I18nUtilities
					       .getResourceString("jbi.root.configuration.failed.validation.range.message"), 
					       args);
		    }
	    }

	sLog.fine("OperationBean.getFailedValidationRangeMessage(" + aMetadata + "), result=" + result);
	return result;
    }

    private int getInteger(Properties aMetadata, String aKey)
	throws NumberFormatException
    {
	sLog.fine("OperationBean.getInteger(" + aMetadata + ", " + aKey + ")");
	int result = -1;
	String intString = aMetadata.getProperty(aKey);
	if (null != intString)
	    {
		int parsedInt = Integer.parseInt(intString);
		result = parsedInt;
	    }
	sLog.fine("OperationBean.getInteger(" + aMetadata + ", " + aKey + "), result=" + result);
	return result;
    }

    private Properties getJbiRtConfigMetadata(String aPropertyName)
    {
	Properties result = new Properties();
	sLog.fine("OperationBean.getJbiRtConfigMetadata(" + aPropertyName + ")");
	try 
	    {
		Properties metadata = mJac.getRuntimeConfigurationMetaData(aPropertyName);
		sLog.fine("OperationBean.getMetadata(" + aPropertyName + "), metadata=" + metadata); 
		result = metadata;
	    }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx) 
	    {
		jbiRemoteEx.printStackTrace();
	    }
	sLog.fine("OperationBean.getJbiRtConfigMetadata(" + aPropertyName + "), result=" + result); 
	return result;
    }

    private JBIAdminCommands mJac;
    private String mJbiAutoInstallDir;
    private String mHeartBeatInterval;
}


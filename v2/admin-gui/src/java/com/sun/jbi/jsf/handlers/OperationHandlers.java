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

package com.sun.jbi.jsf.handlers;

import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.jbi.jsf.bean.ListBean;
import com.sun.jbi.jsf.bean.ShowBean;
import com.sun.jbi.jsf.bean.OperationBean;
import com.sun.jbi.jsf.bean.AlertBean;
import com.sun.jbi.jsf.configuration.beans.ConfigurationBean;
import com.sun.jbi.jsf.util.AlertUtilities;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.ClusterUtilities;
import com.sun.jbi.jsf.util.I18nUtilities;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.SharedConstants;
import com.sun.jbi.jsf.util.SystemLoggerUtilities;
import com.sun.jbi.jsf.util.TableUtilities;
import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import com.sun.jbi.ui.common.JBIManagementMessage;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.component.TextField;
import com.sun.webui.jsf.component.Checkbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * Provides jsftemplating handlers for table actions on selected rows,
 * such as JBI LifeCycle operations (start, stop, shut down), undeployment, or uninstallation.
 */
public class OperationHandlers
{
    private static Logger sLog = JBILogger.getInstance();
    private static String BREAK = "<br />";
    private static String NEWLINE = "\r";
    /**
     * <p> Enables (starts) or Disables (stops, then shuts down) each selected Component or Deployment row.
     * <p> Input  value: "tableRowGroup" -- Type: <code> com.sun.webui.jsf.component.TableRowGroup</code></p>
     * <p> Input  value: "tableType" -- Type: <code> java.lang.String</code>
     * Valid types: 'deployments' or 'bindingsEngines' (Note: 'libraries' do not have LifeCycles)
     * <p> Input  value: "isEnabled" -- Type: <code> java.lang.Boolean</code>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiSetEnablementForSelectedRows",
             input={
                 @HandlerInput(name="tableRowGroup", type=TableRowGroup.class, required=true),
                 @HandlerInput(name="tableType", type=String.class, required=true),
                 @HandlerInput(name="isEnabled", type=Boolean.class, required=true)},
             output={
                 @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
                 @HandlerOutput (name="alertSummary", type=String.class),
                 @HandlerOutput (name="alertDetails", type=String.class)})

    public static void jbiSetEnablementForSelectectedRows(HandlerContext handlerCtx)
    {
        String alertType     = "";
        String alertDetails  = "";
        String alertSummary  = "";
        String internalError = "";
        String successResult = "";
        String failureResult = "";
        String warningResult = "";

        int countSuccessfulOperations  = 0;
        int countFailedOperations      = 0;
        int countWarningOperations     = 0;

        boolean warningFlag       = false;
        boolean failureFlag       = false;
        boolean internalErrorFlag = false;
        boolean isAlertNeeded     = false;

        Properties rowProperties = new Properties();

        TableRowGroup trg = (TableRowGroup)handlerCtx.getInputValue("tableRowGroup");
        String tableType  = (String)handlerCtx.getInputValue("tableType");
        Boolean isEnabled = (Boolean)handlerCtx.getInputValue("isEnabled");

        sLog.fine("OperationHandlers.jbiSetEnablementForSelectedRows(...)" +
                               ", tableType=" + tableType + ", isEnabled=" + isEnabled);


        List componentOrDeploymentRows = getSelectedComponentOrDeploymentRowProperties(trg);
        Iterator rowIt = componentOrDeploymentRows.iterator();
        while (rowIt.hasNext())
        {
            rowProperties = (Properties)rowIt.next();
            sLog.fine("OperationHandlers.jbiSetEnablementForSelectedRows(...), rowProperties=" +
                                   rowProperties);
            String rowName = rowProperties.getProperty(SharedConstants.KEY_NAME);
            String rowType = rowProperties.getProperty(SharedConstants.KEY_TYPE);

            List targets = ClusterUtilities.findTargetsForNameByType(rowName, rowType);

            sLog.fine("OperationHandlers.jbiSetEnablementForSelectedRows(...)" +
                                   ", rowName=" + rowName + ", targets=" + targets);

            // If a target is not associated with this component or service assembly
            // then we need to add an error message to the alerts.
            if (targets.size() == 0)
            {
                String typeText = "";
                if (SharedConstants.COMPONENT_TABLE_TYPE.equals(tableType))
                {
                    typeText = I18nUtilities.getResourceString ("jbi.failed.no.target.component");
                }
                else
                {
                    typeText = I18nUtilities.getResourceString ("jbi.failed.no.target.deployment");
                }
                failureFlag = true;
                ++countFailedOperations;

                Object[] args = {typeText, rowName};
                failureResult = GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.failed.no.target"), args);
                alertDetails += failureResult + BREAK;
            }

            else
            {
                // If Enabling the component/deployment in the row
                if (isEnabled)
                {
                    rowProperties = start(rowProperties, targets);
                }

                // If Disabling the component/deployment in the row
                else
                {
                    rowProperties = shutDown(rowProperties, targets);
                }

                successResult = rowProperties.getProperty(SharedConstants.SUCCESS_RESULT);
                internalError = rowProperties.getProperty(SharedConstants.INTERNAL_ERROR);
                warningResult = rowProperties.getProperty(SharedConstants.WARNING_RESULT);

                if (null != internalError)
                {
                    internalErrorFlag = true;
                    break;
                }

                if (null != successResult)
                {
                    ++countSuccessfulOperations;
                }
                else
                {
                    failureFlag = true;
                    ++countFailedOperations;
                    failureResult = rowProperties.getProperty(SharedConstants.FAILURE_RESULT);
                    alertDetails += failureResult + BREAK;
                }

                if (null != warningResult)
                {
                    warningFlag = true;
                    ++countWarningOperations;
                    warningResult = rowProperties.getProperty(SharedConstants.WARNING_RESULT);
                    alertDetails += warningResult + BREAK;
                }
            }
        }

        // Display the alert message if a failure was encountered
        if (failureFlag || internalErrorFlag)
        {
            isAlertNeeded = true;
            if (isEnabled)
            {
                if (SharedConstants.COMPONENT_TABLE_TYPE.equalsIgnoreCase(tableType))
                {
                    alertSummary = I18nUtilities.getResourceString ("jbi.enable.component.error.summary");
                }
                else
                {
                    alertSummary = I18nUtilities.getResourceString ("jbi.enable.deployment.error.summary");
                }
            }

            else
            {
                if (SharedConstants.COMPONENT_TABLE_TYPE.equalsIgnoreCase(tableType)) {
                    alertSummary = I18nUtilities.getResourceString ("jbi.disable.component.error.summary");
                }
                else
                {
                    alertSummary = I18nUtilities.getResourceString ("jbi.disable.deployment.error.summary");
                }
            }
            alertType = "error";
            alertDetails = BeanUtilities.addAlertFooterMessage(alertDetails);
        }

        else if (warningFlag)
        {
            isAlertNeeded = true;
            alertType = "warning";
            alertSummary = rowProperties.getProperty(SharedConstants.WARNING_SUMMARY);
            alertDetails = BeanUtilities.addAlertFooterMessage(alertDetails);
        }

        // Set the alert type
        AlertBean alertBean = BeanUtilities.getAlertBean();
        alertBean.setAlertType(alertType);

        sLog.fine("OperationHandlers.jbiSetEnablementForSelectedRows(...), " +
                               " isAlertNeeded=" + isAlertNeeded +
                               ", alertSummary=" + alertSummary +
                               ", alertDetails=" + alertDetails);

        handlerCtx.setOutputValue ("isAlertNeeded", Boolean.toString(isAlertNeeded));
        handlerCtx.setOutputValue ("alertSummary", alertSummary);
        handlerCtx.setOutputValue ("alertDetails", alertDetails);
    }


    /**
     * <p> Delegates JBI LifeCycle operation requests (start, stop, or shut down) for each selected
     * Component or Deployment row.
     * <p> Input  value: "tableRowGroup" -- Type: <code> com.sun.webui.jsf.component.TableRowGroup</code></p>
     * <p> Input  value: "tableType" -- Type: <code> java.lang.String</code>
     * Valid types: 'deployments' or 'bindingsEngines' (Note: 'libraries' do not have LifeCycles)
     * <p> Input  value: "operation" -- Type: <code> java.lang.String</code>
     * Valid operations: 'start,' 'stop,' or 'shutDown.'
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiOperateSelectedComponentOrDeploymentRows",
             input={
                 @HandlerInput(name="tableRowGroup", type=TableRowGroup.class, required=true),
                 @HandlerInput(name="tableType", type=String.class, required=true),
                 @HandlerInput(name="operation", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
                 @HandlerOutput (name="alertSummary", type=String.class),
                 @HandlerOutput (name="alertDetails", type=String.class)} )

    public static void jbiOperateSelectedComponentOrDeploymentRows(HandlerContext handlerCtx)
    {
        TableRowGroup trg = (TableRowGroup)
                            handlerCtx.getInputValue("tableRowGroup");
        String tableType = (String)
                           handlerCtx.getInputValue("tableType");
        String operation = (String)
                           handlerCtx.getInputValue("operation");

	sLog.fine("OperationHandlers.jbiOperateSelectedRows(...) trg=" + trg + 
		  ", tableType=" + tableType + 
		  ", operation=" + operation);

        List targets = new ArrayList();
        Properties targetProperties = new Properties();
        targetProperties.setProperty(SharedConstants.KEY_NAME, JBIAdminCommands.SERVER_TARGET_KEY);
        targets.add(targetProperties);

        List componentOrDeploymentRows = getSelectedComponentOrDeploymentRowProperties(trg);
        Iterator rowIt = componentOrDeploymentRows.iterator();

        int countFailedOperations = 0;
        int countSuccessfulOperations = 0;

        String alertDetails = "";

        while (rowIt.hasNext())
        {
            Properties rowProperties = (Properties) rowIt.next();

            if (OperationBean.OPERATION_START.equals(operation))
            {
                rowProperties = start(rowProperties, targets);
            }
            else if (OperationBean.OPERATION_STOP.equals(operation))
            {
                rowProperties = stop(rowProperties, targets);
            }
            else if (OperationBean.OPERATION_SHUT_DOWN.equals(operation))
            {
                rowProperties = shutDown(rowProperties, targets);
            }
            else
            {
                String error = "internal error detected in OperationHandlers.jbiOperateSelectedRows, invalud operation=" +
                               operation;
                sLog.fine("OperationHandlers.jbiOperateSelectedRows(...) error=" + error);
                rowProperties.setProperty("failure-result",error);
            }

	    String failureResult = rowProperties.getProperty(SharedConstants.FAILURE_RESULT);
            String successResult = rowProperties.getProperty(SharedConstants.SUCCESS_RESULT);

            if ((null == failureResult) 
		&& (null != successResult))
		{
		    ++ countSuccessfulOperations; // full or "partial" success 
		    sLog.fine("OperationHandlers.jbiOperateSelectedRows(...), " +
			      " success for rowProperties=" + rowProperties +
			      ", countFailedOperations=" + countFailedOperations +
			      ", countSuccessfulOperations=" + countSuccessfulOperations);
		    
		    // since a partial success appears as a success, but with one or more task-result=FAILED, 
		    // need to handle those as warnings below
		    String exceptionMessage = AlertUtilities.getMessage(successResult);
		    if (!"".equals(exceptionMessage))
			{
			    failureResult = successResult; // "partial" success, handle failures below
			}
		    
		}
	    
	    if (null != failureResult)
            {
                ++ countFailedOperations;
                sLog.fine("OperationHandlers.jbiOperateSelectedRows(...), " +
                                       " failure for rowProperties=" + rowProperties +
                                       ", countFailedOperations=" + countFailedOperations +
                                       ", countSuccessfulOperations=" + countSuccessfulOperations);

                String name = (String)
                              rowProperties.getProperty("name");

                String details = "";
                String exceptionMessage = AlertUtilities.getMessage(failureResult);
                if ("".equals(exceptionMessage))
                {
                    details = failureResult;
                }
                else
                {
                    details = exceptionMessage;
                }
                Object[] args = {name, operation, details};
                String alertDet =
                    GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.operation.failed"), args);
                alertDetails += alertDet + BREAK;
            }
        }

        String alertSummary = "";
        if (0 < countFailedOperations)
        {
            if (0 < countSuccessfulOperations)
            {
                if (1 == countFailedOperations)
                {
                    alertSummary =
                    I18nUtilities.getResourceString ("jbi.operations.one.failed.alert.summary.text");
                }
                else
                {
                    alertSummary =
                    I18nUtilities.getResourceString ("jbi.operations.some.failed.alert.summary.text");
                }
            }
            else
            {
                alertSummary =
                I18nUtilities.getResourceString ("jbi.operations.all.failed.alert.summary.text");
            }
            alertDetails = BeanUtilities.addAlertFooterMessage(alertDetails);
        }

        boolean isAlertNeeded = (0 < countFailedOperations);
        handlerCtx.setOutputValue ("isAlertNeeded", Boolean.toString(isAlertNeeded));
        handlerCtx.setOutputValue ("alertSummary", alertSummary);
        handlerCtx.setOutputValue ("alertDetails", alertDetails);

        sLog.fine("OperationHandlers.jbiOperateSelectedComponentOrDeploymentRows(...), " +
                               " isAlertNeeded=" + isAlertNeeded +
                               ", alertSummary=" + alertSummary +
                               ", alertDetails=" + alertDetails);
      }


    /**
     * <p> Delegates JBI LifeCycle operation requests (start, stop, or shut down) for each selected Target row.
     * <p> Input  value: "tableRowGroup" -- Type: <code> com.sun.webui.jsf.component.TableRowGroup</code></p>
     * <p> Input  value: "targetName" -- Type: <code> java.lang.String</code>
     * <p> Input  value: "operation" -- Type: <code> java.lang.String</code>
     * Valid operations: 'start,' 'stop,' or 'shutDown.'
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiOperateSelectedSingleTargetRows",
             input={
                 @HandlerInput(name="tableRowGroup", type=TableRowGroup.class, required=true),
                 @HandlerInput(name="targetName", type=String.class, required=true),
                 @HandlerInput(name="operation", type=String.class, required=true),
                 @HandlerInput(name="componentOrDeploymentType", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
                 @HandlerOutput (name="alertSummary", type=String.class),
                 @HandlerOutput (name="alertDetails", type=String.class)})

    public static void jbiOperateSelectedSingleTargetRows(HandlerContext handlerCtx)
    {
        TableRowGroup trg = (TableRowGroup)
                            handlerCtx.getInputValue("tableRowGroup");
        String targetName = (String)
                            handlerCtx.getInputValue("targetName");
        String operation = (String)
                           handlerCtx.getInputValue("operation");
        String componentOrDeploymentType = (String) 
	    handlerCtx.getInputValue("componentOrDeploymentType");

        sLog.fine("OperationHandlers.jbiOperateSelectedSingleTargetRows(...), trg=" +
		  trg + ", targetName=" + targetName +
		  ", operation=" + operation + 
		  ", componentOrDeploymentType=" + componentOrDeploymentType);

        List targetRows = TableUtilities.getSelectedRowProperties(trg);

	boolean failureFlag = false;
        boolean isAlertNeeded = false;
        String alertSummary = "";
        String alertDetails = "";
        String successResult = "";
        String failureResult = "";

        int countSuccessfulOperations  = 0;
        int countFailedOperations      = 0;

        if (0 < targetRows.size())
        {
            Properties singleTargetProperties = new Properties();
            singleTargetProperties.setProperty(SharedConstants.KEY_NAME, targetName);

            List singleTargetList = new ArrayList();
            singleTargetList.add(singleTargetProperties);

            Iterator rows = targetRows.iterator();
            while (rows.hasNext())
            {
                Properties compOrSaRow = (Properties) rows.next();

                if (OperationBean.OPERATION_START.equals(operation))
                {
                    compOrSaRow = start(compOrSaRow, singleTargetList);
                }
                else if (OperationBean.OPERATION_SHUT_DOWN.equals(operation))
                {
                    //compOrSaRow = stop(compOrSaRow, singleTargetList); -- done by framework
                    compOrSaRow = shutDown(compOrSaRow, singleTargetList);
                }
                else
                {
                   sLog.fine("OperationHandlers.jbiOperateSelectedRows invalid operation=" + operation + " ignored");
                }

		successResult = compOrSaRow.getProperty(SharedConstants.SUCCESS_RESULT);
		if (null != successResult)
		    {
			++countSuccessfulOperations;
		    }
		else
		    {
			failureFlag = true;
			++countFailedOperations;
			failureResult = compOrSaRow.getProperty(SharedConstants.FAILURE_RESULT);
			alertDetails += failureResult + BREAK;
		    }

		if (failureFlag)
		    {
			isAlertNeeded = true;
			if (SharedConstants.COMPONENT_TABLE_TYPE.equalsIgnoreCase(componentOrDeploymentType))
			    {
				alertSummary = I18nUtilities.getResourceString ("jbi.operation.component.error.summary");
			    }
			else
			    {
				alertSummary = I18nUtilities.getResourceString ("jbi.operation.deployment.error.summary");
			    }
			alertDetails = BeanUtilities.addAlertFooterMessage(alertDetails);
		    }

		// TBD determine warning or failure setAlertType
		String alertType = "warning";
		AlertBean alertBean = BeanUtilities.getAlertBean();
		alertBean.setAlertType(alertType);
            }
        }
        else
        {
           sLog.fine("OperationHandlers.jbiOperateSelectedRows nothing selected");
        }


        handlerCtx.setOutputValue ("isAlertNeeded", Boolean.toString(isAlertNeeded));
        handlerCtx.setOutputValue ("alertSummary", alertSummary);
        handlerCtx.setOutputValue ("alertDetails", alertDetails);

        sLog.fine("OperationHandlers.jbiOperateSelectedSingleTargetRows(...), " +
                               " isAlertNeeded=" + isAlertNeeded +
                               ", alertSummary=" + alertSummary +
                               ", alertDetails=" + alertDetails);
     }


    /**
     * <p> Delegates JBI LifeCycle operation requests (start, stop, or shut down) for each selected Target row.
     * <p> Input  value: "tableRowGroup" -- Type: <code> com.sun.webui.jsf.component.TableRowGroup</code></p>
     * <p> Input  value: "componentOrDeploymentType" -- Type: <code> java.lang.String</code>
     * Valid types: 'deployments' or 'bindingsEngines' (Note: 'libraries' do not have LifeCycles)
     * <p> Input  value: "componentOrDeploymentName" -- Type: <code> java.lang.String</code>
     * <p> Input  value: "operation" -- Type: <code> java.lang.String</code>
     * Valid operations: 'start,' 'stop,' or 'shutDown.'
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiOperateSelectedTargetRows",
             input={
                 @HandlerInput(name="tableRowGroup", type=TableRowGroup.class, required=true),
                 @HandlerInput(name="componentOrDeploymentType", type=String.class, required=true),
                 @HandlerInput(name="componentOrDeploymentName", type=String.class, required=true),
                 @HandlerInput(name="operation", type=String.class, required=true)},

             output={
                 @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
                 @HandlerOutput (name="alertSummary", type=String.class),
                 @HandlerOutput (name="alertDetails", type=String.class)})

    public static void jbiOperateSelectedTargetRows(HandlerContext handlerCtx)
    {
        String alertType     = "";
        String alertDetails  = "";
        String alertSummary  = "";
        String successResult = "";
        String failureResult = "";
        String warningResult = "";

        int countSuccessfulOperations  = 0;
        int countFailedOperations      = 0;
        int countWarningOperations     = 0;

        boolean warningFlag       = false;
        boolean failureFlag       = false;
        boolean isAlertNeeded     = false;

        Properties rowProperties = new Properties();

        TableRowGroup trg = (TableRowGroup) handlerCtx.getInputValue("tableRowGroup");
        String componentOrDeploymentType = (String) handlerCtx.getInputValue("componentOrDeploymentType");
        String componentOrDeploymentName = (String) handlerCtx.getInputValue("componentOrDeploymentName");
        String operation = (String) handlerCtx.getInputValue("operation");

        sLog.fine("OperationHandlers.jbiOperateSelectedTargetRows(...), trg=" +
                               trg + ", componentOrDeploymentType=" + componentOrDeploymentType +
                               ", componentOrDeploymentName=" + componentOrDeploymentName +
                               ", operation=" + operation);

        List targetRows = getSelectedTargetRowProperties(trg);

        if (0 < targetRows.size())
        {
            rowProperties.setProperty(SharedConstants.KEY_TYPE, componentOrDeploymentType);
            rowProperties.setProperty(SharedConstants.KEY_NAME, componentOrDeploymentName);

            if (OperationBean.OPERATION_START.equals(operation))
            {
                rowProperties = start(rowProperties, targetRows);
            }
            else if (OperationBean.OPERATION_STOP.equals(operation))
            {
                rowProperties = stop(rowProperties, targetRows);
            }
            else if (OperationBean.OPERATION_SHUT_DOWN.equals(operation))
            {
                rowProperties = shutDown(rowProperties, targetRows);
            }
            else
            {
               sLog.fine("OperationHandlers.jbiOperateSelectedRows invalid operation=" + operation + " ignored");
            }

            successResult = rowProperties.getProperty(SharedConstants.SUCCESS_RESULT);
            warningResult = rowProperties.getProperty(SharedConstants.WARNING_RESULT);

            if (null != successResult)
            {
                ++countSuccessfulOperations;
            }
            else
            {
                failureFlag = true;
                ++countFailedOperations;
                failureResult = rowProperties.getProperty(SharedConstants.FAILURE_RESULT);
                alertDetails += failureResult + BREAK;
            }

            if (null != warningResult)
            {
                warningFlag = true;
                ++countWarningOperations;
                warningResult = rowProperties.getProperty(SharedConstants.WARNING_RESULT);
                alertDetails += warningResult + BREAK;
            }
        }
        else
        {
            sLog.fine("OperationHandlers.jbiOperateSelectedRows nothing selected");
        }

        if (failureFlag)
        {
            isAlertNeeded = true;
            if (SharedConstants.COMPONENT_TABLE_TYPE.equalsIgnoreCase(componentOrDeploymentType))
            {
                alertSummary = I18nUtilities.getResourceString ("jbi.operation.component.error.summary");
            }
            else
            {
                alertSummary = I18nUtilities.getResourceString ("jbi.operation.deployment.error.summary");
            }
            alertDetails = BeanUtilities.addAlertFooterMessage(alertDetails);
        }

        else if (warningFlag)
        {
            isAlertNeeded = true;
            alertType = "warning";
            alertSummary = rowProperties.getProperty(SharedConstants.WARNING_SUMMARY);
            alertDetails = BeanUtilities.addAlertFooterMessage(alertDetails);
        }

        // Set the alert type
        AlertBean alertBean = BeanUtilities.getAlertBean();
        alertBean.setAlertType(alertType);

        sLog.fine("OperationHandlers.jbiOperateSelectedTargetRows(...), " +
                               " isAlertNeeded=" + isAlertNeeded +
                               ", alertSummary=" + alertSummary +
                               ", alertDetails=" + alertDetails);

        handlerCtx.setOutputValue ("isAlertNeeded", Boolean.toString(isAlertNeeded));
        handlerCtx.setOutputValue ("alertSummary", alertSummary);
        handlerCtx.setOutputValue ("alertDetails", alertDetails);
    }


    /**
     * Returns a list of component or deployment names and types for the selected rows.
     * <p> Will set the filter type in the ListBean.
     * <p> Input  value: "operation" -- Type: <code> java.lang.String</code>
     * Valid operations: The filter type
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="filterTableType",
             input={
                 @HandlerInput(name="operation", type=String.class, required=true)} )

    public static void filterTableType(HandlerContext handlerCtx)
    {
        String operation = (String)handlerCtx.getInputValue("operation");
        ListBean listBean = BeanUtilities.getListBean();
        listBean.setFilterType(operation);
    }


    /**
     * <p> Will set the filter type in the ListBean.
     * <p> Input  value: "operation" -- Type: <code> java.lang.String</code>
     * Valid operations: The filter type
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiFilterTableComponentState",
             input={
                 @HandlerInput(name="value", type=String.class, required=true),
                 @HandlerInput(name="values", type=String.class, required=true)})

    public static void jbiFilterTableComponentState(HandlerContext handlerCtx)
    {
        String value  = (String)handlerCtx.getInputValue("value");
        String values = (String)handlerCtx.getInputValue("values");
        ListBean listBean = BeanUtilities.getListBean();
        listBean.setFilterComponentState(value,values);
    }


    /**
     * <p> Will set the filter type in the ListBean.
     * <p> Input  value: "operation" -- Type: <code> java.lang.String</code>
     * Valid operations: The filter type
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiFilterTableAssemblyState",
             input={
                 @HandlerInput(name="value", type=String.class, required=true),
                 @HandlerInput(name="values", type=String.class, required=true)})

    public static void jbiFilterTableAssemblyState(HandlerContext handlerCtx)
    {
        String value  = (String)handlerCtx.getInputValue("value");
        String values = (String)handlerCtx.getInputValue("values");
        ListBean listBean = BeanUtilities.getListBean();
        listBean.setFilterAssemblyState(value,values);
    }


    /**
     * <p> Will set the filter type in the ListBean.
     * <p> Input  value: "operation" -- Type: <code> java.lang.String</code>
     * Valid operations: The filter type
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiFilterTargetTableComponentState",
             input={
                 @HandlerInput(name="value", type=String.class, required=true),
                 @HandlerInput(name="values", type=String.class, required=true)})

    public static void jbiFilterTargetTableComponentState(HandlerContext handlerCtx)
    {
        String value  = (String)handlerCtx.getInputValue("value");
        String values = (String)handlerCtx.getInputValue("values");
        ShowBean showBean = BeanUtilities.getShowBean();
        showBean.setFilterTargetComponentState(value,values);
    }


    /**
     * <p> Will set the filter type in the ListBean.
     * <p> Input  value: "operation" -- Type: <code> java.lang.String</code>
     * Valid operations: The filter type
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiFilterTargetTableAssemblyState",
             input={
                 @HandlerInput(name="value", type=String.class, required=true),
                 @HandlerInput(name="values", type=String.class, required=true)})

    public static void jbiFilterTargetTableAssemblyState(HandlerContext handlerCtx)
    {
        String value  = (String)handlerCtx.getInputValue("value");
        String values = (String)handlerCtx.getInputValue("values");
        ShowBean showBean = BeanUtilities.getShowBean();
        showBean.setFilterTargetAssemblyState(value,values);
    }


    /**
     *  <p> This handler saves the values for all the attributes in the
     *      Server Logging Levels Page.</p>
     *  <p> Input value: "propertySheetParentId" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "propertySheetId" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "propertySheetSectionIdTag" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "propertySheetIdTag" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "propertyIdTag" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "hiddenFieldIdTag" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "componentName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "targetName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "instanceName" -- Type: <code>java.lang.String</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler(id="jbiSaveComponentLogLevels",
             input={
                 @HandlerInput(name="propertySheetParentId", type=String.class, required=true),
                 @HandlerInput(name="propertySheetId", type=String.class, required=true),
                 @HandlerInput(name="propertySheetSectionIdTag", type=String.class, required=true),
                 @HandlerInput(name="propertySheetIdTag", type=String.class, required=true),
                 @HandlerInput(name="propertyIdTag", type=String.class, required=true),
                 @HandlerInput(name="dropDownIdTag", type=String.class, required=true),
                 @HandlerInput(name="hiddenFieldIdTag", type=String.class, required=true),
                 @HandlerInput(name="componentName", type=String.class, required=true),
                 @HandlerInput(name="targetName", type=String.class, required=true),
                 @HandlerInput(name="instanceName", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
                 @HandlerOutput (name="alertSummary", type=String.class),
                 @HandlerOutput (name="alertDetails", type=String.class)}
             )
    public static void jbiSaveComponentLogLevels(HandlerContext handlerCtx) {

        String propertySheetParentId     = (String)handlerCtx.getInputValue("propertySheetParentId");
        String propertySheetId           = (String)handlerCtx.getInputValue("propertySheetId");
        String propertySheetSectionIdTag = (String)handlerCtx.getInputValue("propertySheetSectionIdTag");
        String propertyIdTag             = (String)handlerCtx.getInputValue("propertyIdTag");
        String dropDownIdTag             = (String)handlerCtx.getInputValue("dropDownIdTag");
        String hiddenFieldIdTag          = (String)handlerCtx.getInputValue("hiddenFieldIdTag");
        String componentName             = (String)handlerCtx.getInputValue("componentName");
        String targetName                = (String)handlerCtx.getInputValue("targetName");
        String instanceName              = (String)handlerCtx.getInputValue("instanceName");

        String alertType          = "";
        String alertDetails       = "";
        String alertSummary       = "";
        boolean isAlertNeeded     = true;

        Properties result = jbiSetComponentLogLevels (propertySheetParentId,
                                                      propertySheetId,
                                                      propertySheetSectionIdTag,
                                                      propertyIdTag,
                                                      dropDownIdTag,
                                                      hiddenFieldIdTag,
                                                      componentName,
                                                      targetName,
                                                      instanceName,
                                                      handlerCtx);

        String failureResult = result.getProperty(SharedConstants.FAILURE_RESULT);
        if ((null != failureResult) && (failureResult.length() > 0))
        {
            alertType = "error";
            alertSummary = I18nUtilities.getResourceString ("jbi.configure.loggers.failure.summary.message");
            alertDetails = failureResult;
        }
        else
        {
            alertType     = "success";
            alertSummary  = I18nUtilities.getResourceString ("jbi.configure.loggers.success.summary.message");
            alertDetails = "";
        }

        // Set the alert type
        AlertBean alertBean = BeanUtilities.getAlertBean();
        alertBean.setAlertType(alertType);

        handlerCtx.setOutputValue ("isAlertNeeded", isAlertNeeded);
        handlerCtx.setOutputValue ("alertSummary", alertSummary);
        handlerCtx.setOutputValue ("alertDetails", alertDetails);
    }


    /**
    * Saves the log levels for the specified instance.  Called from cluster profile.
    * <p> Will set the filter type in the ListBean.
    * <p> Input  value: "operation" -- Type: <code> java.lang.String</code>
    * Valid operations: The filter type
    * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
    */
    @Handler(id="jbiSaveInstancesLogLevels",
             input={
                 @HandlerInput(name="propertySheetParentId", type=String.class, required=true),
                 @HandlerInput(name="propertySheetId", type=String.class, required=true),
                 @HandlerInput(name="propertySheetSectionIdTag", type=String.class, required=true),
                 @HandlerInput(name="propertyIdTag", type=String.class, required=true),
                 @HandlerInput(name="dropDownIdTag", type=String.class, required=true),
                 @HandlerInput(name="hiddenFieldIdTag", type=String.class, required=true),
                 @HandlerInput(name="componentName", type=String.class, required=true),
                 @HandlerInput(name="targetNames", type=String[].class, required=true),
                 @HandlerInput(name="instanceNames", type=String[].class, required=true)},
             output={
                 @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
                 @HandlerOutput (name="alertSummary", type=String.class),
                 @HandlerOutput (name="alertDetails", type=String.class)}
             )
    public static void jbiSaveInstancesLogLevels(HandlerContext handlerCtx)
    {
        String propertySheetParentId     = (String)handlerCtx.getInputValue("propertySheetParentId");
        String propertySheetId           = (String)handlerCtx.getInputValue("propertySheetId");
        String propertySheetSectionIdTag = (String)handlerCtx.getInputValue("propertySheetSectionIdTag");
        String propertyIdTag             = (String)handlerCtx.getInputValue("propertyIdTag");
        String dropDownIdTag             = (String)handlerCtx.getInputValue("dropDownIdTag");
        String hiddenFieldIdTag          = (String)handlerCtx.getInputValue("hiddenFieldIdTag");
        String componentName             = (String)handlerCtx.getInputValue("componentName");
        String[] instanceNames           = (String[])handlerCtx.getInputValue("instanceNames");
        String[] targetNames             = (String[])handlerCtx.getInputValue("targetNames");

        String alertType     = "";
        String alertSummary  = "";
        String alertDetails  = "";
        String failureResult = "";
        String failureResultDetails = "";

        int countSuccessfulOperations  = 0;
        int countFailedOperations      = 0;

        boolean failureFlag    = false;
        boolean isAlertNeeded  = true;

        for (int i=0; i<instanceNames.length; i++)
        {
            Properties result = jbiSetComponentLogLevels (propertySheetParentId,
                                                          propertySheetId,
                                                          propertySheetSectionIdTag,
                                                          propertyIdTag,
                                                          dropDownIdTag,
                                                          hiddenFieldIdTag,
                                                          componentName,
                                                          targetNames[i],
                                                          instanceNames[i],
                                                          handlerCtx);

            failureResult = result.getProperty(SharedConstants.FAILURE_RESULT);
            if ((null != failureResult) && (failureResult.length() > 0))
            {
                failureFlag = true;
                ++countFailedOperations;
                failureResultDetails += failureResult + BREAK;
            }
        }

        // Set the result type based on the failure and success flags.
        if (failureFlag)
        {
            alertType = "error";
            alertSummary = I18nUtilities.getResourceString ("jbi.configure.loggers.failure.summary.message");
            alertDetails = failureResultDetails;
        }
        else
        {
            alertType = "success";
            alertSummary = I18nUtilities.getResourceString ("jbi.configure.loggers.success.summary.message");
            alertDetails = "";
        }

        // Set the alert type
        AlertBean alertBean = BeanUtilities.getAlertBean();
        alertBean.setAlertType(alertType);

        handlerCtx.setOutputValue ("isAlertNeeded", isAlertNeeded);
        handlerCtx.setOutputValue ("alertSummary",  alertSummary);
        handlerCtx.setOutputValue ("alertDetails",  alertDetails);

        // Update the Instances Update List so the selected values will still be selected.
        ListBean listBean = BeanUtilities.getListBean();
        listBean.setUpdateInstancesList(instanceNames);
    }


    /**
     * Returns a list of component or deployment names and types for the selected rows.
     * <p> Will set the filter type in the ListBean.
     * <p> Input  instanceName:  -- Type: <code> java.lang.String</code>
     * Valid operations: The filter type
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiInitUpdateList",
             input={
                 @HandlerInput(name="instanceName", type=String.class, required=true)} )

    public static void jbiInitUpdateList(HandlerContext handlerCtx)
    {

        String value = (String)handlerCtx.getInputValue("instanceName");
        String[] values = {value};
        ListBean listBean = BeanUtilities.getListBean();
        listBean.setUpdateInstancesList(values);
    }


    /**
     * Will set the runtime configuration values for the specified target.
     * <p> Input   target:  -- Type: <code> java.lang.String</code>
     * <p> Input   heartBeatInterval:  -- Type: <code> java.lang.String</code>
     * <p> Input   autoInstallEnabled:  -- Type: <code> java.lang.String</code>
     * <p> Input   autoInstallDir:  -- Type: <code> java.lang.String</code>
     * <p> Input   startOnDeploy:  -- Type: <code> java.lang.String</code>
     * <p> Input   installationTimeout:  -- Type: <code> java.lang.String</code>
     * <p> Input   componentTimeout:  -- Type: <code> java.lang.String</code>
     * <p> Input   serviceUnitTimeout:  -- Type: <code> java.lang.String</code>
     * Valid operations: The filter type
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiSetRuntimeConfigurationParameters",
             input={
                 @HandlerInput(name="target", type=String.class, required=true),
                 @HandlerInput(name="heartBeatInterval", type=String.class, required=true),
                 @HandlerInput(name="autoInstallEnabled", type=String.class, required=true),
                 @HandlerInput(name="startOnDeploy", type=String.class, required=true),
                 @HandlerInput(name="installationTimeout", type=String.class, required=true),
                 @HandlerInput(name="componentTimeout", type=String.class, required=true),
                 @HandlerInput(name="serviceUnitTimeout", type=String.class, required=true)}
             )
    public static void jbiSetRuntimeConfigurationParameters(HandlerContext handlerCtx)
    {
        String alertType      = "";
        String alertSummary   = "";
        String alertDetails   = "";

        String target              = (String)handlerCtx.getInputValue("target");
        String heartBeatInterval   = (String)handlerCtx.getInputValue("heartBeatInterval");
        String autoInstallEnabled  = (String)handlerCtx.getInputValue("autoInstallEnabled");
        String startOnDeploy       = (String)handlerCtx.getInputValue("startOnDeploy");
        String installationTimeout = (String)handlerCtx.getInputValue("installationTimeout");
        String componentTimeout    = (String)handlerCtx.getInputValue("componentTimeout");
        String serviceUnitTimeout  = (String)handlerCtx.getInputValue("serviceUnitTimeout");
	
        Properties configurationProperties = new Properties();

	// note: these values have already been validated by the OperationBean
	configurationProperties = setPropertyIfNotNull(configurationProperties, "heartBeatInterval",   heartBeatInterval);
	configurationProperties = setPropertyIfNotNull(configurationProperties, "autoInstallEnabled",  autoInstallEnabled.toString());
	configurationProperties = setPropertyIfNotNull(configurationProperties, "startOnDeploy",       startOnDeploy.toString());
	configurationProperties = setPropertyIfNotNull(configurationProperties, "installationTimeout", installationTimeout);
	configurationProperties = setPropertyIfNotNull(configurationProperties, "componentTimeout",    componentTimeout);
	configurationProperties = setPropertyIfNotNull(configurationProperties, "serviceUnitTimeout",  serviceUnitTimeout);
		
	Properties changedConfigurationProperties = findChangedRuntimeConfigurationProperties(target, configurationProperties);
	
	JBIAdminCommands mJac = BeanUtilities.getClient();
	try 
	    {
		boolean restartRequired = mJac.setRuntimeConfiguration(changedConfigurationProperties, target);
		sLog.fine("OperationHandlers.jbiSetRuntimeConfigurationParameters changedConfigurationProperties=" + changedConfigurationProperties +
			  ", target=" + target + ", restartRequired=" + restartRequired);
		if (null != configurationProperties)
		    {
			for(Object object : configurationProperties.keySet())
			    {
				String k = (String) object;
				String v = (String) configurationProperties.getProperty(k);
				sLog.fine("OperationHandlers.jbiSetRuntimeConfigurationParameters k=" + k + ", v=" + v);
			    }
		    }
		
		if (restartRequired)
		    {
			alertType    = "info";
			alertSummary = I18nUtilities.getResourceString ("jbi.root.configuration.restart.required.summary");
			if (target.equalsIgnoreCase("domain"))
			    {
				alertDetails = I18nUtilities.getResourceString ("jbi.root.configuration.restart.required.all.targets.details");
			    }
			else
			    {
				Object[] args = {target};
				alertDetails = GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.root.configuration.restart.required.a.target.details"), args);
			    }
		    }
		else
		    {
			alertType     = "success";
			alertSummary  = I18nUtilities.getResourceString ("jbi.configure.loggers.success.summary.message");
		    }
	    }
	catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx) 
	    {
		JBIManagementMessage mgmtMsg = null;
		mgmtMsg = BeanUtilities.extractJBIManagementMessage(jbiRemoteEx);
		alertType     = "error";
		alertSummary  = I18nUtilities.getResourceString("jbi.configure.runtime.alert.summary ");
		alertDetails  = mgmtMsg.getMessage();
		alertDetails  = BeanUtilities.addAlertFooterMessage(alertDetails);
	    }
	
        AlertBean alertBean = BeanUtilities.getAlertBean();
        alertBean.setAlertType(alertType);
	alertBean.setAlertSummary(alertSummary);
	alertBean.setAlertDetail(alertDetails);
	
	
	sLog.fine("OperationHandler.jbiSetRuntimeConfigurationParameters, alertSummary=" + alertSummary +
		  ", alertDetails=" + alertDetails);
    }


    /**
     * Will retrieve the runtime configuration values for a given target.
     * <p> Input  target:  -- Type: <code> java.lang.String</code>
     * <p> Output  heartBeatInterval:  -- Type: <code> java.lang.String</code>
     * <p> Output  jbiHome:  -- Type: <code> java.lang.String</code>
     * <p> Output  autoInstallEnabled:  -- Type: <code> java.lang.Boolean</code>
     * <p> Output  autoInstallDir:  -- Type: <code> java.lang.String</code>
     * <p> Output  startOnDeploy:  -- Type: <code> java.lang.String</code>
     * <p> Output  installationTimeout:  -- Type: <code> java.lang.String</code>
     * <p> Output  componentTimeout:  -- Type: <code> java.lang.String</code>
     * <p> Output  serviceUnitTimeout:  -- Type: <code> java.lang.String</code>
     * Valid operations: The filter type
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiGetRuntimeConfigurationParameters",
             input={
                 @HandlerInput(name="target", type=String.class, required=true)},
             output={
                 @HandlerOutput(name="heartBeatInterval", type=String.class),
                 @HandlerOutput(name="jbiHome", type=String.class),
                 @HandlerOutput(name="autoInstallEnabled", type=Boolean.class),
                 @HandlerOutput(name="autoInstallDir", type=String.class),
                 @HandlerOutput(name="startOnDeploy", type=Boolean.class),
                 @HandlerOutput(name="installationTimeout", type=String.class),
                 @HandlerOutput(name="componentTimeout", type=String.class),
                 @HandlerOutput(name="serviceUnitTimeout", type=String.class)} 
             )
    public static void jbiGetRuntimeConfigurationParameters(HandlerContext handlerCtx)
    {
	// Get all properties for the specified target
        String target = (String)handlerCtx.getInputValue("target");
	
	Properties properties = getCurrentRuntimeConfigurationProperties(target);
	String jbiHome = properties.getProperty("jbiHome");
	sLog.fine("OperationHandlers.jbiGetRuntimeConfigurationParameters(" + target +
		  ") target jbiHome=" + jbiHome);

	// if jbiHome is not set, handle as a special case:
	if ((null == jbiHome)
	    ||("".equals(jbiHome)))
	    {
		JBIAdminCommands mJac = BeanUtilities.getClient();
		
		try 
		    {
			Properties defaultProperties = null;
			// domain target does not provide a jbiHome, use the factory-default instead
			if (target.equalsIgnoreCase("domain"))
			    {
				defaultProperties = mJac.getDefaultRuntimeConfiguration();
				jbiHome = defaultProperties.getProperty("jbiHome");
				sLog.fine("OperationHandlers.jbiGetRuntimeConfigurationParameters(" + target +
					  ") factory-default jbiHome=" + jbiHome);
			    }
			// non-domain targets may not provide a jbiHome, use the domain-default instead
			else
			    {
				defaultProperties = mJac.getRuntimeConfiguration("domain");
				jbiHome = defaultProperties.getProperty("jbiHome");
				sLog.fine("OperationHandlers.jbiGetRuntimeConfigurationParameters(" + target +
					  ") domain-default jbiHome=" + jbiHome);
			    }
		    }
		catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx) 
		    {
			jbiRemoteEx.printStackTrace();
		    }
	    }
	
	handlerCtx.setOutputValue ("heartBeatInterval", properties.getProperty("heartBeatInterval"));
	handlerCtx.setOutputValue ("jbiHome", properties.getProperty("jbiHome"));
	handlerCtx.setOutputValue ("autoInstallEnabled", properties.getProperty("autoInstallEnabled"));
	handlerCtx.setOutputValue ("autoInstallDir", properties.getProperty("autoInstallDir"));
	handlerCtx.setOutputValue ("startOnDeploy", properties.getProperty("startOnDeploy"));
	handlerCtx.setOutputValue ("installationTimeout", properties.getProperty("installationTimeout"));
	handlerCtx.setOutputValue ("componentTimeout", properties.getProperty("componentTimeout"));
	handlerCtx.setOutputValue ("serviceUnitTimeout", properties.getProperty("serviceUnitTimeout"));
    }


    /**
     * Will retrieve the runtime configuration values for a given target.
     * <p> Input  target:  -- Type: <code> java.lang.String</code>
     * <p> Output  heartBeatInterval:  -- Type: <code> java.lang.String</code>
     * <p> Output  jbiHome:  -- Type: <code> java.lang.String</code>
     * <p> Output  autoInstallEnabled:  -- Type: <code> java.lang.Boolean</code>
     * <p> Output  autoInstallDir:  -- Type: <code> java.lang.String</code>
     * <p> Output  startOnDeploy:  -- Type: <code> java.lang.String</code>
     * <p> Output  installationTimeout:  -- Type: <code> java.lang.String</code>
     * <p> Output  componentTimeout:  -- Type: <code> java.lang.String</code>
     * <p> Output  serviceUnitTimeout:  -- Type: <code> java.lang.String</code>
     * Valid operations: The filter type
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiGetRuntimeConfigurationDefaultParameters",
             input={
                 @HandlerInput(name="target", type=String.class, required=true)},
             output={
                 @HandlerOutput(name="heartBeatInterval", type=String.class),
                 @HandlerOutput(name="jbiHome", type=String.class),
                 @HandlerOutput(name="autoInstallEnabled", type=Boolean.class),
                 @HandlerOutput(name="autoInstallDir", type=String.class),
                 @HandlerOutput(name="startOnDeploy", type=Boolean.class),
                 @HandlerOutput(name="installationTimeout", type=String.class),
                 @HandlerOutput(name="componentTimeout", type=String.class),
                 @HandlerOutput(name="serviceUnitTimeout", type=String.class)} 
             )
    public static void jbiGetRuntimeConfigurationDefaultParameters(HandlerContext handlerCtx)
    {
        String target = (String)handlerCtx.getInputValue("target");

        JBIAdminCommands mJac = BeanUtilities.getClient();
        try {

            // A target of domain says we should retrieve the Factory Default values. This
            // is done by calling getDefaultRuntimeConfiguration.
            Properties properties = null;
            if (target.equalsIgnoreCase("domain"))
            {
                properties = mJac.getDefaultRuntimeConfiguration();
            }

            // If the target value is NOT domain, then we should retrieve the default
            // values that are set for the domain.  This is done by retrieving the
            // runtime configuration values using the domain as the target.
            else
            {
                properties = mJac.getRuntimeConfiguration("domain");
            }
            handlerCtx.setOutputValue ("heartBeatInterval", properties.getProperty("heartBeatInterval"));
            handlerCtx.setOutputValue ("jbiHome", properties.getProperty("jbiHome"));
            handlerCtx.setOutputValue ("autoInstallEnabled", properties.getProperty("autoInstallEnabled"));
            handlerCtx.setOutputValue ("autoInstallDir", properties.getProperty("autoInstallDir"));
            handlerCtx.setOutputValue ("startOnDeploy", properties.getProperty("startOnDeploy"));
            handlerCtx.setOutputValue ("installationTimeout", properties.getProperty("installationTimeout"));
            handlerCtx.setOutputValue ("componentTimeout", properties.getProperty("componentTimeout"));
            handlerCtx.setOutputValue ("serviceUnitTimeout", properties.getProperty("serviceUnitTimeout"));
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx) {
            jbiRemoteEx.printStackTrace();
        }
    }


    /**
     * Returns a list of component or deployment names and types for the selected rows.
     * <p> Will set the filter type in the ListBean.
     * <p> Input  key:  -- Type: <code> java.lang.String</code>
     * <p> Input  value:  -- Type: <code> java.lang.String</code>
     * Valid operations: The filter type
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiSetComponentConfigurationParameter",
             input={
                 @HandlerInput(name="key", type=String.class, required=true),
                 @HandlerInput(name="value", type=String.class, required=true)} 
             )

    public static void jbiSetComponentConfigurationParameter(HandlerContext handlerCtx)
    {
        String key = (String)handlerCtx.getInputValue("key");
        String value = (String)handlerCtx.getInputValue("value");
        ConfigurationBean configurationBean = BeanUtilities.getConfigurationBean();
        configurationBean.setParameterValue(key,value);
    }


    /**
     * Returns a the total count of installed binding components.
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiGetInstalledComponentCount",
             input={
                 @HandlerInput(name="target", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="totalCount", type=String.class)}
             )
    public static void jbiGetInstalledComponentCount(HandlerContext handlerCtx)
    {
        String target = (String)handlerCtx.getInputValue("target");
        int totalCount = 0;
        JBIAdminCommands mJac = BeanUtilities.getClient();

        String result = "";
        try {
            result = mJac.listBindingComponents(null, null, null, target);
            List list = JBIComponentInfo.readFromXmlText(result);
            totalCount = list.size();
            result = mJac.listServiceEngines(null, null, null, target);
            list = JBIComponentInfo.readFromXmlText(result);
            totalCount += list.size();
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx) {
        }
        handlerCtx.setOutputValue ("totalCount", totalCount);
    }


    /**
     * Returns a the total count of installed binding components.
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiGetInstalledBindingCount",
             input={
                 @HandlerInput(name="target", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="totalCount", type=String.class)}
             )
    public static void jbiGetInstalledBindingCount(HandlerContext handlerCtx)
    {
        String target = (String)handlerCtx.getInputValue("target");
        int totalCount = 0;
        JBIAdminCommands mJac = BeanUtilities.getClient();

        String result = "";
        try {
            result = mJac.listBindingComponents(null, null, null, target);
            List list = JBIComponentInfo.readFromXmlText(result);
            totalCount = list.size();
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx) {
        }
        handlerCtx.setOutputValue ("totalCount", totalCount);
    }

    /**
     * Returns a the total count of installed binding components.
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiGetInstalledEngineCount",
             input={
                 @HandlerInput(name="target", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="totalCount", type=String.class)}
             )
    public static void jbiGetInstalledEngineCount(HandlerContext handlerCtx)
    {
        String target = (String)handlerCtx.getInputValue("target");
        int totalCount = 0;
        JBIAdminCommands mJac = BeanUtilities.getClient();

        String result = "";
        try {
            result = mJac.listServiceEngines(null, null, null, target);
            List list = JBIComponentInfo.readFromXmlText(result);
            totalCount = list.size();
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx) {
        }
        handlerCtx.setOutputValue ("totalCount", totalCount);
    }


    /**
     * Returns a the total count of installed binding components.
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiGetInstalledSharedLibraryCount",
             input={
                 @HandlerInput(name="target", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="totalCount", type=String.class)}
             )
    public static void jbiGetInstalledSharedLibraryCount(HandlerContext handlerCtx)
    {
        String target = (String)handlerCtx.getInputValue("target");
        int totalCount = 0;
        JBIAdminCommands mJac = BeanUtilities.getClient();

        String result = "";
        try {
            result = mJac.listSharedLibraries(null, target);
            List list = JBIComponentInfo.readFromXmlText(result);
            totalCount = list.size();
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx) {
        }
        handlerCtx.setOutputValue ("totalCount", totalCount);
    }


    /**
     * Returns a the total count of installed binding components.
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiGetDeployedServiceAssemblyCount",
             input={
                 @HandlerInput(name="target", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="totalCount", type=String.class)}
             )
    public static void jbiGetDeployedServiceAssemblyCount(HandlerContext handlerCtx)
    {
        String target = (String)handlerCtx.getInputValue("target");
        int totalCount = 0;
        JBIAdminCommands mJac = BeanUtilities.getClient();

        String result = "";
        try {
            result = mJac.listServiceAssemblies(null, null, target);
            List list = ServiceAssemblyInfo.readFromXmlTextWithProlog(result);
            totalCount = list.size();
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx) {
        }
        handlerCtx.setOutputValue ("totalCount", totalCount);
    }


    /**
     * Returns a the total count of installed binding components.
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiGetSystemProperty",
             input={
                 @HandlerInput(name="key", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="value", type=String.class)}
             )
    public static void jbiGetSystemProperty(HandlerContext handlerCtx)
    {
        String key = (String)handlerCtx.getInputValue("key");
        Properties pr = System.getProperties(); 
        String value = pr.getProperty(key);
        String newValue = "";
        for (int i=0; i<value.length(); i++)
        {
            if (value.charAt(i) == '\\')
            {
                newValue += "/";
            }
            else {
                newValue += value.charAt(i);
            }
        }
        handlerCtx.setOutputValue ("value", newValue);
    }


    /**
     * Presets a validation alert summary and clears details before a save.
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiPresetValidationAlert")
    public static void jbiPresetValidationAlert(HandlerContext handlerCtx)
    {
        AlertBean alertBean = BeanUtilities.getAlertBean();
        alertBean.setAlertType("error");
        alertBean.setAlertSummary("Validation of required fields failed.");
        alertBean.setAlertDetail(""); // must be empty, for revalidation to add only latest errors
	sLog.fine("OperationHandlers.jbiPresetValidationAlert(), alert type=" + alertBean.getAlertType() +
		  ", alertSummary=" + alertBean.getAlertSummary() +
		  ", alertDetail=" + alertBean.getAlertDetail());
    }

    /**
     * Sets a field validator for a textField and its JBI RT configuration property
     * <p> Input  textField:  -- Type: <code>com.sun.webui.jsf.component.TextField</code>
     * <p> Input  propertyName:  -- Type: <code>String</code>
     * <p> Input  label:  -- Type: <code>String</code>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiSetFieldValidator",
             input={
                 @HandlerInput(name="textField", type=com.sun.webui.jsf.component.TextField.class, required=true),
                 @HandlerInput(name="propertyName", type=String.class, required=true),
                 @HandlerInput(name="label", type=String.class, required=true)}
             )
    public static void jbiSetFieldValidator(HandlerContext handlerCtx)
    {
        TextField textField    = (TextField)handlerCtx.getInputValue("textField");
        String    propertyName = (String)handlerCtx.getInputValue("propertyName");
        String    label = (String)handlerCtx.getInputValue("label");
	sLog.fine("OperationHandlers.jbiSetFieldValidator textField=" + textField +
		  ", propertyName=" + propertyName + 
		  ", label=" + label);

	FacesContext facesContext = FacesContext.getCurrentInstance();
	String textFieldId = textField.getClientId(facesContext);

	// add UIComponent to propertyName mapping

	// get map from session 
        Map sessionMap = 
	    (Map) facesContext.getApplication().createValueBinding("#{sessionScope}").getValue(facesContext);
	sLog.finer("OperationHandlers.jbiSetFieldValidator before sessionMap=" + sessionMap); 

	// add/replace map entry
	sessionMap.put(textFieldId, propertyName);
	sLog.fine("OperationHandlers.jbiSetFieldValidator updated mapping of textFieldId=" + textFieldId +
		  " to propertyName=" + propertyName);

	Properties alertPropertyLabels = 
	    (Properties) sessionMap.get("jbiAlertPropertyLabels");
	if (null == alertPropertyLabels)
	    {
		alertPropertyLabels = new Properties();
	    }
	alertPropertyLabels.put(propertyName, label);
	sessionMap.put("jbiAlertPropertyLabels",
		       alertPropertyLabels);

	// add validator to field
	ELContext elcontext = facesContext.getELContext();
	MethodExpression methodExpression = facesContext.getApplication().getExpressionFactory()
	    .createMethodExpression(
				    elcontext,
				    "#{OperationBean.validateField}",                
				    null,
				    new Class[] { FacesContext.class,
						  UIComponent.class, 
						  Object.class });
	textField.setValidatorExpression(methodExpression);
	sLog.fine("OperationHandlers.jbiSetFieldValidator validatorExpression=" + methodExpression);

	// Preset alert details for this property to assume validation failure 
	// (this handles the case for a blank input field where the validator is not called)
	// Each validator will remove its corresponding alert detail after validation passes
	// or replace the detail if validation fails.
	// 1. get session map
	// 2. get alertDetail properties from sessionMap (create if missing)
	// 3. add proptertyName to alertDetail mapping to alertDetail properties
	// 4. put alertDetail properties back into sessionMap
	// If all validation passes, this map becomes empty, and no validation alert is needed.
	Properties alertDetailProperties = 
	    (Properties) sessionMap.get("jbiAlertDetailProperties");
	if (null == alertDetailProperties)
	    {
		alertDetailProperties = new Properties();
	    }

	alertDetailProperties.put(propertyName, ""); // clear out prior validation errors
	sessionMap.put("jbiAlertDetailProperties", alertDetailProperties);

	sLog.finer("OperationHandlers.jbiSetFieldValidator after sessionMap=" + sessionMap); 
    }

    /**
     * Sets a validaiton alert type and summary.
     * <p> Input  alertType:  -- Type: <code>String</code>
     * <p> Input  alertSummary:  -- Type: <code>String</code>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiSetValidationAlert",
             input={
                 @HandlerInput(name="alertType", type=String.class, required=true),
                 @HandlerInput(name="alertSummary", type=String.class, required=true)}
             )
    public static void jbiSetValidationAlert(HandlerContext handlerCtx)
    {
        String    alertType   = (String)handlerCtx.getInputValue("alertType");
        String    alertSummary   = (String)handlerCtx.getInputValue("alertSummary");
	sLog.fine("OperationHandlers.jbiSetValidationAlert(), alertType=" + alertType +
		  ", alertSummary=" + alertSummary);
        AlertBean alertBean = BeanUtilities.getAlertBean();
        alertBean.setAlertType(alertType);
        alertBean.setAlertSummary(alertSummary);
    }

    /**
     *  <p> This method saves the values for all the attributes in the
     *      Server Logging Levels Page.</p>
     *  @param "propertySheetParentId" -- Type: <code>java.lang.String</code></p>
     *  @param "propertySheetId" -- Type: <code>java.lang.String</code></p>
     *  @param "propertySheetSectionIdTag" -- Type: <code>java.lang.String</code></p>
     *  @param "propertySheetIdTag" -- Type: <code>java.lang.String</code></p>
     *  @param "propertyIdTag" -- Type: <code>java.lang.String</code></p>
     *  @param "hiddenFieldIdTag" -- Type: <code>java.lang.String</code></p>
     *  @param "componentName" -- Type: <code>java.lang.String</code></p>
     *  @param "targetName" -- Type: <code>java.lang.String</code></p>
     *  @param "instanceName" -- Type: <code>java.lang.String</code></p>
     *  @return <code>Properties</code>
     */
    private static Properties jbiSetComponentLogLevels (String propertySheetParentId,
                                                        String propertySheetId,
                                                        String propertySheetSectionIdTag,
                                                        String propertyIdTag,
                                                        String dropDownIdTag,
                                                        String hiddenFieldIdTag,
                                                        String componentName,
                                                        String targetName,
                                                        String instanceName,
                                                        HandlerContext handlerCtx)
    {

        int loggerFailureCount = 0;

        String propertySheetSectionId = "";
        String propertyId             = "";
        String dropDownId             = "";
        String hiddenFieldId          = "";

        int propertySheetSectionCount = 1;
        int propertyCount             = 1;
        int dropDownCount             = 1;
        int hiddenFieldCount          = 1;

        int loopLevel = 1;

        int testCounter = 0;

        JBIAdminCommands mJac = BeanUtilities.getClient();

        // Retrieve the original logger levels so we can only save the log 
        // level values for the loggers that have changed.
        Map originalLoggerLevels = null;
        try
        {
            originalLoggerLevels = mJac.getComponentLoggerLevels(componentName,
                                                                 targetName,
                                                                 instanceName);
        } catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
        {
            sLog.fine("jbiSetComponentLogLevels(): caught jbiRemoteEx=" + jbiRemoteEx);
            sLog.fine("jbiSetComponentLogLevels(): unable to retrieve the original log level values");
            loggerFailureCount++;
        }

        if (loggerFailureCount == 0)
        {
            boolean propertySheetSectionLoop = true;
            while (propertySheetSectionLoop)
            {
                propertySheetSectionId = propertySheetSectionIdTag + Integer.toString(propertySheetSectionCount);
                boolean propertyLoop = true;
                while (propertyLoop)
                {
                    propertyId = propertyIdTag + Integer.toString(propertyCount);
                    boolean dropDownLoop = true;
                    while (dropDownLoop)
                    {
                        String key = propertySheetParentId + ":" +
                                     propertySheetId + ":" +
                                     propertySheetSectionId + ":" +
                                     propertyId;

                        // First try to find the dropdown component using the new woodstock naming
                        // convention (no "_list" appended to the end of the id).  If the component
                        // was not found, then try to find it using the old naming convention
                        // ("_list" appended to the end of the id)
                        dropDownId = dropDownIdTag + Integer.toString(dropDownCount);
                        String dropDownKey = key + ":" + dropDownId;
                        String dropDownValue = (String)handlerCtx.getFacesContext().getExternalContext().getRequestParameterMap().get(dropDownKey);
                        if (dropDownValue == null)
                        {
                            dropDownKey += "_list";
                            dropDownValue = (String)handlerCtx.getFacesContext().getExternalContext().getRequestParameterMap().get(dropDownKey);
                        }

                        if (dropDownValue == null)
                        {
                            loopLevel++;
                            if (loopLevel == 2)
                            {
                                dropDownLoop = false;
                                propertyCount++;
                            }
                            else
                            {
                                if (loopLevel == 3)
                                {
                                    propertyLoop = false;
                                    dropDownLoop = false;
                                    propertySheetSectionCount++;
                                }
                                else
                                {
                                    propertySheetSectionLoop = false;
                                    propertyLoop = false;
                                    dropDownLoop = false;
                                }
                            }
                        }
                        else
                        {
                            hiddenFieldId = hiddenFieldIdTag + Integer.toString(hiddenFieldCount);
                            String hiddenFieldKey = key + ":" + hiddenFieldId;
                            Object hiddenFieldValue = handlerCtx.getFacesContext().getExternalContext().getRequestParameterMap().get(hiddenFieldKey);

                            if (hiddenFieldValue != null)
                            {
                                String loggerCustomName = hiddenFieldValue.toString();
                                try
                                {
                                    String loggerTagName = (String)SystemLoggerUtilities.loggerNames.get(loggerCustomName);
                                    if (loggerTagName != null)
                                    {
                                        String logLevelStr = dropDownValue;
                                        String targetConfig = targetName + "-config";
                                        String originalLogLevelStr = SystemLoggerUtilities.getLogLevelValue(loggerTagName,
                                                                                                            targetConfig);
                                        if (!(dropDownValue.equalsIgnoreCase(originalLogLevelStr)))
                                        {
                                            SystemLoggerUtilities.setLogLevelValue(loggerCustomName,
                                                                                   targetName,
                                                                                   dropDownValue);
                                            sLog.fine("OperationHandlers - jbiSaveComponentLogLevels() " +
                                                      "Updated System Logger: " +
                                                      ", loggerCustomName=" + loggerCustomName +
                                                      ", dropDownValue=" + dropDownValue +
                                                      ", targetName=" + targetName);
                                        }
                                    }
                                    else
                                    {
                                        if (null != mJac)
                                        {
                                            Level logLevel = null;
                                            if (!(dropDownValue.equalsIgnoreCase(
                                                GuiUtil.getMessage(I18nUtilities.getResourceString("loglevel.DEFAULT")))))
                                            {
                                                logLevel = Level.parse(dropDownValue);
                                            }
                                            Level originalLevel = (Level)originalLoggerLevels.get(loggerCustomName);

                                            if ((logLevel == null) ||
						((originalLevel != null) &&
						 (!(logLevel.getName().equalsIgnoreCase(originalLevel.getName())))))
                                            {
                                                mJac.setComponentLoggerLevel(componentName,
                                                                             loggerCustomName,
                                                                             logLevel,
                                                                             targetName,
                                                                             instanceName);
                                                sLog.fine("OperationHandlers - jbiSaveComponentLogLevels() " +
                                                          "Updated JBI Logger: " +
                                                          "componentName=" + componentName +
                                                          ", loggerCustomName=" + loggerCustomName +
                                                          ", logLevel=" + logLevel +
                                                          ", originalLevel=" + originalLevel +
                                                          ", targetName=" + targetName +
                                                          ", instanceName=" + instanceName);
                                            }
                                        }
                                    }
                                }
                                catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
                                {
                                    sLog.fine("JBILogLevelsPropertySheetAdaptor(): caught jbiRemoteEx=" + jbiRemoteEx);
                                    loggerFailureCount++;
                                }
                            }

                            loopLevel = 1;
                            dropDownCount++;
                            hiddenFieldCount++;
                        }
                    }
                }
            }
        }

        // Place the messages in the properties result.  Note, only an error
        // message will be returned.
        Properties result = new Properties();
        result.setProperty(SharedConstants.KEY_NAME, instanceName);
        result.setProperty(SharedConstants.FAILURE_RESULT,"");
        if (loggerFailureCount > 0)
        {
            String strCount = loggerFailureCount + "";
            Object[] args = {strCount, instanceName};
            String msg = GuiUtil.getMessage(I18nUtilities.getResourceString(
                "jbi.configure.loggers.failure.message"), args);
            result.setProperty(SharedConstants.FAILURE_RESULT, msg);
        }

        return result;

    }


    /**
     * Returns a list of names and types for the selected rows.
     * @param aGroup <code>TableRowGroup</code> the table data with some rows selected.
     * @return <code>List</code> of <code>Properties</code> objects
     * <p> Each properties object has 'name' and 'type' keys and values.
     */
    private static List getSelectedComponentOrDeploymentRowProperties(TableRowGroup aGroup)
    {
        ArrayList result = new ArrayList();

        ObjectListDataProvider dp = (ObjectListDataProvider)
                                    aGroup.getSourceData();

        if (null != dp)
        {
            try
            {
                FieldKey fkName = dp.getFieldKey(SharedConstants.KEY_NAME);
                FieldKey fkType = dp.getFieldKey(SharedConstants.KEY_TYPE);

                RowKey[] rowKeys = aGroup.getSelectedRowKeys();

                for (int cnt = 0; cnt < rowKeys.length; cnt++)
                {
                    Properties selectedRowProperties =
                    new Properties();


                    String compName = (String)
                                      dp.getValue(fkName, rowKeys[cnt]);

                    selectedRowProperties.setProperty(SharedConstants.KEY_NAME, compName);

                    String compType = (String)
                                      dp.getValue(fkType, rowKeys[cnt]);

                    sLog.fine("OperationHandlers.getSelectedComponentOrDeploymentRowProperties(...), compType=" +
                                           compType);

                    if ((JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(compType))
                        ||(JBIConstants.JBI_SERVICE_ENGINE_TYPE.equals(compType)))
                    {
                        selectedRowProperties.setProperty(SharedConstants.KEY_TYPE, compType);
                    }
                    else
                    {
                        selectedRowProperties.setProperty(SharedConstants.KEY_TYPE, JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE);
                    }

                    result.add(selectedRowProperties);
                }
            }
            catch (Exception ex)
            {
                sLog.fine("OperationHandlers.getSelectedComponentOrDeploymentRowProperties(), caught ex=" + ex);
                ex.printStackTrace(System.err);
            }
        }
        else
        {
            sLog.fine("OperationHandlers.getSelectedComponentOrDeploymentRowProperties(), cannot process dp=" + dp);
        }

        sLog.fine("OperationHandlers.getSelectedComponentOrDeploymentRowProperties(), result=" + result);
        return result;
    }


    /**
     * Returns a list of target cluster or stand-alone instances for the selected rows.
     * @param aGroup <code>TableRowGroup</code> the table data with some rows selected.
     * @return <code>List</code> of <code>Properties</code> objects
     * <p> Each properties object has 'target' key and value.
     */
    private static List getSelectedTargetRowProperties(TableRowGroup aGroup)
    {
        if (true)
        {
            return TableUtilities.getSelectedRowProperties(aGroup);
        }
        else
        {
            ArrayList result = new ArrayList();

            sLog.fine("OperationHandlers.getSelectedTargetRowProperties(" + aGroup + ")");

            ObjectListDataProvider dp = (ObjectListDataProvider)
                                        aGroup.getSourceData();

            if (null != dp)
            {
                try
                {
                    FieldKey fkName = dp.getFieldKey(SharedConstants.KEY_NAME);
                    FieldKey fkType = dp.getFieldKey(SharedConstants.KEY_TYPE);

                    RowKey[] rowKeys = aGroup.getSelectedRowKeys();

                    for (int cnt = 0; cnt < rowKeys.length; cnt++)
                    {
                        Properties selectedRowProperties =
                        new Properties();

                        String targetName = (String)
                                            dp.getValue(fkName, rowKeys[cnt]);

                        selectedRowProperties.setProperty(SharedConstants.KEY_NAME, targetName);

                        String targetType = (String)
                                            dp.getValue(fkType, rowKeys[cnt]);

                        selectedRowProperties.setProperty(SharedConstants.KEY_TYPE, targetType);

                        result.add(selectedRowProperties);
                    }
                }
                catch (Exception ex)
                {
                   sLog.fine("OperationHandlers.getSelectedTargetRowProperties(), caught ex=" + ex);
                    ex.printStackTrace(System.err);
                }
            }
            else
            {
                sLog.fine("OperationHandlers.getSelectedTargetRowProperties(), cannot process dp=" + dp);
            }

            sLog.fine("OperationHandlers.getSelectedTargetRowProperties(), result=" + result);
            return result;
        }
    }

    private static Properties shutDown(Properties aRowProperties, List aTargetsList)
    {
        sLog.fine("OperationHandlers.shutDown(" + aRowProperties +
                               ", " + aTargetsList + ")");
        OperationBean operationBean = BeanUtilities.getOperationBean();
        aRowProperties = operationBean.shutDown(aRowProperties, aTargetsList);
        aRowProperties = adjustResults(aRowProperties);
        return aRowProperties;
    }

    private static Properties start(Properties aRowProperties, List aTargetsList)
    {
        sLog.fine("OperationHandlers.start(" + aRowProperties +
                               ", " + aTargetsList + ")");
        OperationBean operationBean = BeanUtilities.getOperationBean();
        aRowProperties = operationBean.start(aRowProperties, aTargetsList);
        aRowProperties = adjustResults(aRowProperties);
        return aRowProperties;
    }

    private static Properties stop(Properties aRowProperties, List aTargetsList)
    {
        sLog.fine("OperationHandlers.stop(" + aRowProperties +
                               ", " + aTargetsList + ")");
        OperationBean operationBean = BeanUtilities.getOperationBean();
        aRowProperties = operationBean.stop(aRowProperties, aTargetsList);
        aRowProperties = adjustResults(aRowProperties);
        return aRowProperties;

        // TBD accumulate results
    }


    /**
     * ensures that there is only one success or failure
     * i.e. takes warnings from success results and turns
     * them into failure results, to force alert details to be
     * displayed
     * @param aRowProperties containing zero or more success and
     * zero or more failure results
     * @returns Propeties with failure results if any, otherwise
     * success results, if any
     */
    private static Properties adjustResults(Properties aRowProperties)
    {
        sLog.fine("OperationHandlers.adjustResults(" + aRowProperties + ")");
        JBIManagementMessage mgmtMsg = null;
        String failureResult =
            aRowProperties.getProperty(SharedConstants.FAILURE_RESULT);
        if (null == failureResult)
        {
            String failureResult2 =
                aRowProperties.getProperty(SharedConstants.SUCCESS_RESULT);
            if (null == failureResult2)
            {
                sLog.fine("OperationHandlers.adjustResults(...) no failure or success (?!) --nothing to adjust");
            }
            else
            {
                mgmtMsg =
                    BeanUtilities.extractJBIManagementMessage(failureResult2);
		sLog.fine("OperationHandlers.adjustResults(...) mgmtMsg=" + mgmtMsg);
                if ((null != mgmtMsg)
                    &&((mgmtMsg.isFailedMsg()
			||mgmtMsg.isWarningMsg())))
                {
                    sLog.fine("OperationHandlers.adjustResults(...) jbiXmlResults has failure(s) -- adjust failureResult2=" +
                              failureResult2);
                    aRowProperties.remove(SharedConstants.SUCCESS_RESULT);
		    String msg = mgmtMsg.getMessage();
		    msg = msg.replaceAll(NEWLINE, BREAK);
		    sLog.fine("OperationHandlers.adjustResults(...) msg=" + msg);
                    aRowProperties.setProperty(SharedConstants.FAILURE_RESULT, msg);
                }
                else
                {
                    sLog.fine("OperationHandlers.adjustResults(...) jbiXmlResult success --nothing to adjust");
                }
            }
        }
        else
        {
            sLog.fine("OperationHandlers.accumulateResults(...) found failure--accumulate failureResult=" + failureResult);
            mgmtMsg =
                BeanUtilities.extractJBIManagementMessage(failureResult);
            if (null != mgmtMsg)
            {
                aRowProperties.remove(SharedConstants.SUCCESS_RESULT);
		String msg = mgmtMsg.getMessage();
		msg = msg.replaceAll(NEWLINE, BREAK);
                aRowProperties.setProperty(SharedConstants.FAILURE_RESULT, msg);
            }
            else
            {
                sLog.fine("OperationHandlers.accumulateResults(...) no management message");
            }
        }
        return aRowProperties;
    }

    /**
     * gets the current properties for a target
     * @param aTarget String  (domain, server, cluster1, server2, etc.)
     * @returns Properties
     */
    private static Properties getCurrentRuntimeConfigurationProperties(String aTarget)
    {
	Properties result = new Properties();
	JBIAdminCommands mJac = BeanUtilities.getClient();
	
	try 
	    {
		result = mJac.getRuntimeConfiguration(aTarget);
		sLog.fine("OperationHandlers.jbiGetRuntimeConfigurationParameters heartBeatInterval metaData = " +
			  mJac.getRuntimeConfigurationMetaData("heartBeatInterval"));
		sLog.fine("OperationHandlers.jbiGetRuntimeConfigurationParameters jbiHome metaData = " +
			  mJac.getRuntimeConfigurationMetaData("jbiHome"));
		sLog.fine("OperationHandlers.jbiGetRuntimeConfigurationParameters autoInstallEnabled metaData = " +
			  mJac.getRuntimeConfigurationMetaData("autoInstallEnabled"));
		sLog.fine("OperationHandlers.jbiGetRuntimeConfigurationParameters autoInstallDir metaData = " +
			  mJac.getRuntimeConfigurationMetaData("autoInstallDir"));
		sLog.fine("OperationHandlers.jbiGetRuntimeConfigurationParameters startOnDeploy metaData = " +
			  mJac.getRuntimeConfigurationMetaData("startOnDeploy"));
		sLog.fine("OperationHandlers.jbiGetRuntimeConfigurationParameters installationTimeout metaData = " +
			  mJac.getRuntimeConfigurationMetaData("installationTimeout"));
		sLog.fine("OperationHandlers.jbiGetRuntimeConfigurationParameters componentTimeout metaData = " +
			  mJac.getRuntimeConfigurationMetaData("componentTimeout"));
		sLog.fine("OperationHandlers.jbiGetRuntimeConfigurationParameters serviceUnitTimeout metaData = " +
			  mJac.getRuntimeConfigurationMetaData("serviceUnitTimeout"));
		
		if (null != result)
		    {
			for(Object object : result.keySet())
			    {
				String k = (String) object;
				String v = (String) result.getProperty(k);
				sLog.finer("OperationHandlers.getCurrentRuntimeConfigurationProperties " + k + " metaData = " +
					  mJac.getRuntimeConfigurationMetaData(k)); 
				sLog.finer("OperationHandlers.getCurrentRuntimeConfigurationProperties(" + aTarget + "), key=" + k + 
					  ", value=" + v);
			    }
		    }
	    }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx) {
            jbiRemoteEx.printStackTrace();
        }
	sLog.fine("OperationHandlers.getCurrentRuntimeConfigurationProperties(" + aTarget + ")=" + result);
	return result;
    }

    /**
     * finds the changed properties for a target
     * @param aTarget String  (domain, server, cluster1, server2, etc.)
     * @param aTarget String  (domain, server, cluster1, server2, etc.)
     * @returns Properties
     */
    private static Properties findChangedRuntimeConfigurationProperties(String aTarget, Properties aListOfAllProperties)
    {
	Properties result = new Properties();
	sLog.fine("OperationHandlers.findCurrentRuntimeConfigurationProperties(" + aTarget + ")");
	Properties currentProperties = getCurrentRuntimeConfigurationProperties(aTarget);
	// for each property, omit those unchanged from the current configuration
	if (null != aListOfAllProperties)
	    {
		for(Object object : aListOfAllProperties.keySet())
		    {
			String k = (String) object;
			String newValue = (String) aListOfAllProperties.getProperty(k);
			String currentValue = currentProperties.getProperty(k);
			sLog.finer("OperationHandlers.findCurrentRuntimeConfigurationProperties(" + aTarget + "), k=" + k + 
				  ", newValue=" + newValue + ", currentValue=" + currentValue); 
			if ((null != newValue)
			    &&(!newValue.equals(currentValue)))
			    {
				result.setProperty(k, newValue);
				sLog.finer("OperationHandlers.findCurrentRuntimeConfigurationProperties(" + aTarget + "), k=" + k + 
					  ", newValue=" + newValue + " added to result"); 
			    }
			    
		    }
	    }
	
	sLog.fine("OperationHandlers.findCurrentRuntimeConfigurationProperties(" + aTarget + "), result=" + result);
	return result;
    }

    private static Properties setPropertyIfNotNull(Properties aProperties, String aKey, String aValue)
    {
	Properties result = aProperties;
	sLog.fine("OperationalHandlers.setPropertyIfNotNull(...), key=" + aKey + ", value=" + aValue);
	if (null != aValue)
	    {
		result.setProperty(aKey, aValue);
	    }
	return result;
    }

}

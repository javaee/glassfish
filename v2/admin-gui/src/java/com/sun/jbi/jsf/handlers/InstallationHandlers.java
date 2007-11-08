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

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.enterprise.tools.admingui.util.FileUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.jbi.jsf.bean.AlertBean;
import com.sun.jbi.jsf.bean.DeletionBean;
import com.sun.jbi.jsf.bean.InstallationBean;
import com.sun.jbi.jsf.bean.InstallationTarget;
import com.sun.jbi.jsf.bean.JBIComponentConfigBean;
import com.sun.jbi.jsf.bean.OperationBean;
import com.sun.jbi.jsf.bean.ShowBean;
import com.sun.jbi.jsf.util.AlertUtilities;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.ClusterUtilities;
import com.sun.jbi.jsf.util.I18nUtilities;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.SharedConstants;
import com.sun.jbi.jsf.util.TableUtilities;
import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.model.Option;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.Properties;

/**
 * Provides jsftemplating handlers for installation, deployment, and table actions on selected rows,
 * such as undeployment and uninstallation
 */
public class InstallationHandlers
{
    public final static String KEY_ARCHIVE_NAME    = "archiveName";
    public final static String KEY_PATH            = "path";
    public final static String KEY_NAME            = "name";

    private final static boolean IS_CLUSTER_PROFILE = ClusterUtilities.isClusterProfile();

    //Get Logger to log fine mesages for debugging
    private static Logger sLog = JBILogger.getInstance();


    /**
     * <p> Delegates JBI deletion requests for each selected row; accumulates failures into alert
     * <p> Input  value: "tableRowGroup" -- Type: <code> com.sun.webui.jsf.component.TableRowGroup</code></p>
     * <p> Input  value: "tableType" -- Type: <code> java.lang.String</code>
     * <p> Output value: "isAlertNeeded" -- Type: <code>java.lang.Boolean</code></p>
     * <p> Output value: "alertSummary" -- Type: <code>String</code>/</p>
     * <p> Output value: "alertDetails" -- Type: <code>String</code>/</p>
     * Valid types: 'deployments,' 'bindingsEngines,' or 'libraries.'
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiDeleteSelectedRows",
             input={
                 @HandlerInput (name="tableRowGroup", type=TableRowGroup.class, required=true),
                 @HandlerInput (name="tableType", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
                 @HandlerOutput (name="alertSummary", type=String.class),
                 @HandlerOutput (name="alertDetails", type=String.class)})

    public static void jbiDeleteSelectedRows(HandlerContext handlerCtx)
    {

        TableRowGroup trg = (TableRowGroup)handlerCtx.getInputValue("tableRowGroup");
        String tableType = (String)handlerCtx.getInputValue("tableType");

        sLog.fine("InstallationHandlers.jbiDeleteSelectedRows(" +
                               trg + ", " + tableType + ")");

        List deletionRows = TableUtilities.getSelectedRowProperties(trg);
        Iterator rowIt = deletionRows.iterator();

        int countFailedDeletions = 0;
        int countSuccessfulDeletions = 0;
        int countWarningDeletions = 0;

        boolean isAlertNeeded = false;

        String alertDetails = "";
        String alertType = "";

        while (rowIt.hasNext())
        {
            Properties rowProperties = (Properties)rowIt.next();
            String rowName = (String)rowProperties.getProperty(SharedConstants.KEY_NAME);
            String rowType = (String)rowProperties.getProperty(SharedConstants.KEY_TYPE);

            List targets = new ArrayList();
            List listOfTargets = new ArrayList();

            if (IS_CLUSTER_PROFILE)
            {
                listOfTargets = ClusterUtilities.findTargetsForNameByType(rowName, rowType);
                if (listOfTargets.size() == 0)
                {
                    targets.add(JBIAdminCommands.DOMAIN_TARGET_KEY);
                }
                else
                {
                    for (Iterator it = listOfTargets.iterator(); it.hasNext();)
                    {
                        Properties targetProperties = (Properties)it.next();
                        String tgt = (String)targetProperties.getProperty(SharedConstants.KEY_NAME);
                        targets.add(tgt);
                    }
                }
            }
            else // developer-profile
            {
                targets.add(JBIAdminCommands.SERVER_TARGET_KEY);
            }

            sLog.fine("InstallationHandlers.jbiDeleteSelectedRows(), targets=" +
                                   targets + ")");

            // When deleting a row from the table, (via a button) we do not want to
            // retain the the retain the component/assembly/library in the domain.
            boolean retainFlag = false;

            delete (rowProperties, retainFlag, targets);

            String successResult = (String)rowProperties.getProperty(SharedConstants.SUCCESS_RESULT);

            if (null != successResult)
            {
                ++ countSuccessfulDeletions;
                sLog.fine("InstallationHandlers.jbiDeleteSelectedRows(...), " +
                                       " success for " + rowProperties +
                                       ", countFailedDeletions=" + countFailedDeletions +
                                       ", countSuccessfulDeletions=" + countSuccessfulDeletions);
            }
            else
            {
                String failureResult = (String)
                       rowProperties.getProperty(SharedConstants.FAILURE_RESULT);

                if (failureResult.trim().startsWith("WARNING")) {
                    ++ countWarningDeletions;
                }
                else {
                    ++ countFailedDeletions;
                }

                sLog.fine("InstallationHandlers.jbiDeleteSelectedRows(...), " +
                                       " failure for " + rowProperties +
                                       ", countFailedDeletions=" + countFailedDeletions +
                                       ", countSuccessfulDeletions=" + countSuccessfulDeletions);

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

                Object[] args = {rowName,details};
                alertDetails += GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.deletion.failed.for.row"), args) + "<br />";
            }
        }

        String alertSummary = "";
        if (0 < countFailedDeletions)
        {
            if ((0 < countSuccessfulDeletions) || (0 < countWarningDeletions))
            {
                if (1 == countFailedDeletions)
                {
                    alertSummary =
                    I18nUtilities.getResourceString ("jbi.delete.one.failed.alert.summary.text");
                }
                else
                {
                    alertSummary =
                    I18nUtilities.getResourceString ("jbi.delete.some.failed.alert.summary.text");
                }
            }
            else
            {
                alertSummary =
                I18nUtilities.getResourceString ("jbi.delete.all.failed.alert.summary.text");
            }
            alertDetails = BeanUtilities.addAlertFooterMessage(alertDetails);
        }
        else if (0 < countWarningDeletions)
        {
            alertSummary = I18nUtilities.getResourceString ("jbi.deletion.warning.summary");
            alertType     = "warning";
            AlertBean alertBean = BeanUtilities.getAlertBean();
            alertBean.setAlertType(alertType);
        }

        if ((countFailedDeletions > 0) || (countWarningDeletions > 0))
        {
            isAlertNeeded = true;
        }
        handlerCtx.setOutputValue ("isAlertNeeded", Boolean.toString(isAlertNeeded));
        handlerCtx.setOutputValue ("alertSummary", alertSummary);
        handlerCtx.setOutputValue ("alertDetails", alertDetails);

        sLog.fine("InstallationHandlers.jbiDeleteSelectedRows(...), " +
                               " isAlertNeeded=" + isAlertNeeded +
                               ", alertSummary=" + alertSummary +
                               ", alertDetails=" + alertDetails);
    }

    /**
     * <p> Delegates installation request for table type
     * <p> Input  value: "archivePath" -- Type: <code> java.lang.String</code> path to validated archive
     * <p> Input  value: "jbiName" -- Type: <code> java.lang.String</code> JBI name from validated archive
     * <p> Input  value: "jbiType" -- Type: <code> java.lang.String</code> JBI type from validated archive
     * <p> Input  value: "isEnabledAfterInstallOrDeploy" -- Type: <code> java.lang.Boolean</code> start if true
     * <p> Input  value: "redirectOnFailure" -- Type: <code> java.lang.String</code> what to show if it fails
     * <p> Input  value: "redirectOnSuccess" -- Type: <code> java.lang.String</code> what to show if it works
     * <p> Input  value: "installList" -- Type: <code> java.util.List</code> Where to install (if cluster-profile)
     * <p> Output value: "isAlertNeeded" -- Type: <code>java.lang.Boolean</code>show alert or not</p>
     * <p> Output value: "alertSummary" -- Type: <code>String</code>summary, if failure</p>
     * <p> Output value: "alertDetails" -- Type: <code>String</code>details, if failure</p>
     * <p> Output value: "redirectTo" -- Type: <code>String</code> Where to go next, based on success/failure</p>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiInstallValidatedArchive",
             input={
                 @HandlerInput(name="archivePath", type=String.class, required=true),
                 @HandlerInput(name="jbiName", type=String.class, required=true),
                 @HandlerInput(name="jbiType", type=String.class, required=true),
                 @HandlerInput(name="isEnabledAfterInstallOrDeploy", type=Boolean.class, required=true),
                 @HandlerInput (name="redirectOnFailure", type=String.class, required=true),
                 @HandlerInput (name="redirectOnSuccess", type=String.class, required=true),
                 @HandlerInput (name="installList", type=java.util.List.class, required=false)},
             output={
                 @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
                 @HandlerOutput (name="alertSummary", type=String.class),
                 @HandlerOutput (name="alertDetails", type=String.class),
                 @HandlerOutput (name="redirectTo", type=String.class)})

    public static void jbiInstallValidatedArchive(HandlerContext handlerCtx)
    {
        String archivePath = (String)handlerCtx.getInputValue("archivePath");
        String jbiName     = (String)handlerCtx.getInputValue("jbiName");
        String jbiType     = (String)handlerCtx.getInputValue("jbiType");
        String redirectOnFailure = (String)handlerCtx.getInputValue("redirectOnFailure");
        String redirectOnSuccess = (String)handlerCtx.getInputValue("redirectOnSuccess");
        String redirectTo = redirectOnSuccess;

        Boolean isEnabledAfterInstallOrDeploy = (Boolean)
                handlerCtx.getInputValue("isEnabledAfterInstallOrDeploy");

        sLog.fine("InstallationHandlers.jbiInstallValidatedArchive(), archivePath=" + archivePath +
                               ", jbiName=" + jbiName + ", jbiType=" + jbiType +
                               ", isEnabledAfterInstallOrDeploy=" + isEnabledAfterInstallOrDeploy +
                               ", redirectOnFailure=" + redirectOnFailure +
                               ", redirectOnSuccess=" + redirectOnSuccess);

        String alertDetails  = "";
        String alertSummary  = "";
        String internalError = "";
        String successResult = "";
        String failureResult = "";

        boolean failureFlag       = false;
        boolean internalErrorFlag = false;
        boolean isAlertNeeded     = false;
        boolean noTargetFlag      = false;

        List successTargets = new ArrayList();
        List targets        = new ArrayList();

        int countSuccessfulStart     = 0;
        int countFailedStart         = 0;
        int countSuccessfulInstalls  = 0;
        int countFailedInstalls      = 0;

        Properties installProperties = new Properties();
        installProperties.setProperty(KEY_PATH, archivePath);
        installProperties.setProperty(SharedConstants.KEY_NAME, jbiName);
        installProperties.setProperty(SharedConstants.KEY_TYPE, jbiType);

        if (IS_CLUSTER_PROFILE)
        {
//            InstallationBean installationBean = BeanUtilities.getInstallationBean();
//            String[] selectedNames = installationBean.getTargetNames();
//            targets = Arrays.asList(selectedNames);
            targets = (List) handlerCtx.getInputValue("installList");

            if (targets.size() == 0)
            {
                targets = new ArrayList();
                targets.add(JBIAdminCommands.DOMAIN_TARGET_KEY);
                noTargetFlag = true;
            }
        }
        else
        {
            targets.add(JBIAdminCommands.SERVER_TARGET_KEY);
        }

        for (Iterator it = targets.iterator(); it.hasNext();)
        {
            String target = (String)it.next();
            installProperties = install(installProperties,target);

            successResult = (String)installProperties.getProperty(SharedConstants.SUCCESS_RESULT);
            internalError = (String)installProperties.getProperty(SharedConstants.INTERNAL_ERROR);

            if (null != internalError)
            {
                internalErrorFlag = true;
                break;
            }

            if (null != successResult)
            {
                ++countSuccessfulInstalls;
                Properties succcessProperties = new Properties();
                succcessProperties.setProperty(SharedConstants.KEY_NAME, target);
                successTargets.add(succcessProperties);
            }
            else
            {
                failureFlag = true;
                ++countFailedInstalls;
                failureResult = (String)installProperties.getProperty(SharedConstants.FAILURE_RESULT);
                alertDetails += failureResult + "<br>";
            }
        }

        // to allow reinstallation in the same session with
        // the original jbi.xml setting invalidate the currently used config. provider
        JBIComponentConfigBean configBean =
            BeanUtilities.getJBIComponentConfigBean();
        configBean.invalidateCommittedDataProvider();

        // Setup the alert message if a failure was encountered during install
        if (failureFlag || internalErrorFlag)
        {
            isAlertNeeded = true;
            if (internalErrorFlag)
            {
                alertSummary = I18nUtilities.getResourceString ("jbi.internal.error.summary");
                alertDetails = internalError;
            }
            else
            {
                if (JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE.equals(jbiType))
                {
                    alertSummary = I18nUtilities.getResourceString ("jbi.deployment.failed.alert.summary.text");
                }
                else
                {
                    alertSummary = I18nUtilities.getResourceString ("jbi.installation.failed.alert.summary.text");
                }
            }
        }

        else
        {
            // Check to see if the enable check box was selected
            if ((null != isEnabledAfterInstallOrDeploy) && (isEnabledAfterInstallOrDeploy))
            {
                // If the component/assembly was successfully installed on at least
                // one target, then go ahead and try to start it.  Note, it will
                // only try to start it on the targets that install was successful
                if (countSuccessfulInstalls > 0)
                {
                    Properties startProperties = new Properties();

                    // This will be true if the component was installed only to the DAS
                    if (noTargetFlag)
                    {
                        sLog.fine("*** noTargetFlag set");
                        isAlertNeeded = true;
                        String typeText = "";
                        if (jbiType.equalsIgnoreCase(SharedConstants.COMPONENT_TABLE_TYPE))
                        {
                            typeText = I18nUtilities.getResourceString ("jbi.failed.no.target.component");
                        }
                        else
                        {
                            typeText = I18nUtilities.getResourceString ("jbi.failed.no.target.deployment");
                        }
                        ++countFailedStart;

                        Object[] args = {typeText, jbiName};
                        failureResult =
                            GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.failed.no.target"), args);

                        alertDetails += failureResult + "<br>";
                        alertSummary = I18nUtilities.getResourceString("jbi.failed.start.after.install");
                    }

                    else
                    {
                        startProperties.setProperty(SharedConstants.KEY_NAME, jbiName);
                        startProperties.setProperty(SharedConstants.KEY_TYPE, jbiType);
                        OperationBean operationBean = BeanUtilities.getOperationBean();
                        startProperties = operationBean.start (startProperties, successTargets);

                        successResult = (String)startProperties.getProperty(SharedConstants.SUCCESS_RESULT);
                        if (null != successResult)
                        {
                            ++countSuccessfulStart;
                        }
                        else
                        {
                            isAlertNeeded = true;
                            ++countFailedStart;
                            failureResult = startProperties.getProperty(SharedConstants.FAILURE_RESULT);
                            alertDetails += failureResult + "<br>";
                            alertSummary = I18nUtilities.getResourceString("jbi.failed.start.after.install");
                        }
                    }
                   sLog.fine("InstallationHandlers.jbiInstallValidatedArchive(...), startProperties=" +
                                           startProperties);
               }
            }
            //Sucessful installation done, now delete the temporarily uploaded file
            deleteTempFile(archivePath);
        }

        if  (countSuccessfulInstalls > 0)//Success 
        {
            redirectTo = redirectOnSuccess;
            //Partial Success should navigate back to List View/Originating Page and the alert
            //Should be displayed in  List View/Originating page
            if (isAlertNeeded)//Success with warnings
            {
                alertDetails = BeanUtilities.addAlertFooterMessage(alertDetails);
            }
        }
        else if (isAlertNeeded) //Total Failure
        {
            alertDetails = BeanUtilities.addAlertFooterMessage(alertDetails);
            redirectTo = redirectOnFailure;
        }

        handlerCtx.setOutputValue ("isAlertNeeded", Boolean.toString(isAlertNeeded));
        handlerCtx.setOutputValue ("alertSummary", alertSummary);
        handlerCtx.setOutputValue ("alertDetails", alertDetails);
        handlerCtx.setOutputValue ("redirectTo", redirectTo);

        sLog.fine("InstallationHandlers.jbiInstallValidatedArchive(...), " +
                               " isAlertNeeded=" + isAlertNeeded +
                               ", alertSummary=" + alertSummary +
                               ", alertDetails=" + alertDetails +
                               ", redirectTo=" + redirectTo);
    }

    /**
     * <p> Delegates update request for table type
     * <p> Input  value: "archivePath" -- Type: <code> java.lang.String</code> path to validated archive
     * <p> Input  value: "jbiName" -- Type: <code> java.lang.String</code> JBI name from validated archive
     * <p> Input  value: "jbiType" -- Type: <code> java.lang.String</code> JBI type from validated archive
     * <p> Input  value: "isEnabledAfterInstallOrDeploy" -- Type: <code> java.lang.Boolean</code> start if true
     * <p> Input  value: "redirectOnFailure" -- Type: <code> java.lang.String</code> what to show if it fails
     * <p> Input  value: "redirectOnSuccess" -- Type: <code> java.lang.String</code> what to show if it works
     * <p> Input  value: "installList" -- Type: <code> java.util.List</code> Where to install (if cluster-profile)
     * <p> Output value: "isAlertNeeded" -- Type: <code>java.lang.Boolean</code>show alert or not</p>
     * <p> Output value: "alertSummary" -- Type: <code>String</code>summary, if failure</p>
     * <p> Output value: "alertDetails" -- Type: <code>String</code>details, if failure</p>
     * <p> Output value: "redirectTo" -- Type: <code>String</code> Where to go next, based on success/failure</p>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiUpdateValidatedArchive",
             input={
                 @HandlerInput(name="archivePath", type=String.class, required=true),
                 @HandlerInput(name="jbiName", type=String.class, required=true),
                 @HandlerInput(name="jbiType", type=String.class, required=true),
                 @HandlerInput(name="isEnabledAfterInstallOrDeploy", type=Boolean.class, required=true),
                 @HandlerInput (name="redirectOnFailure", type=String.class, required=true),
                 @HandlerInput (name="redirectOnSuccess", type=String.class, required=true),
                 @HandlerInput (name="installList", type=java.util.List.class, required=false)},
             output={
                 @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
                 @HandlerOutput (name="alertSummary", type=String.class),
                 @HandlerOutput (name="alertDetails", type=String.class),
                 @HandlerOutput (name="redirectTo", type=String.class)})

    public static void jbiUpdateValidatedArchive(HandlerContext handlerCtx)
    {
        String archivePath = (String)handlerCtx.getInputValue("archivePath");
        String jbiName     = (String)handlerCtx.getInputValue("jbiName");
        String jbiType     = (String)handlerCtx.getInputValue("jbiType");
        String redirectOnFailure = (String)handlerCtx.getInputValue("redirectOnFailure");
        String redirectOnSuccess = (String)handlerCtx.getInputValue("redirectOnSuccess");
        String redirectTo = redirectOnSuccess;

        Boolean isEnabledAfterInstallOrDeploy = (Boolean)
                handlerCtx.getInputValue("isEnabledAfterInstallOrDeploy");

        sLog.fine("InstallationHandlers.jbiUpdateValidatedArchive(), archivePath=" + archivePath +
                               ", jbiName=" + jbiName + ", jbiType=" + jbiType +
                               ", isEnabledAfterInstallOrDeploy=" + isEnabledAfterInstallOrDeploy +
                               ", redirectOnFailure=" + redirectOnFailure +
                               ", redirectOnSuccess=" + redirectOnSuccess);

        String alertDetails  = "";
        String alertSummary  = "";
        String internalError = "";
        String successResult = "";
        String failureResult = "";

        boolean failureFlag       = false;
        boolean internalErrorFlag = false;
        boolean isAlertNeeded     = false;
        boolean noTargetFlag      = false;

        int countSuccessfulStart     = 0;
        int countFailedStart         = 0;
        int countSuccessfulInstalls  = 0;
        int countFailedInstalls      = 0;

        Properties updateProperties = new Properties();
        updateProperties.setProperty(KEY_PATH, archivePath);
        updateProperties.setProperty(SharedConstants.KEY_NAME, jbiName);
        updateProperties.setProperty(SharedConstants.KEY_TYPE, jbiType);

        // TBD implement update component use-case when common client and runtime supports it
        // updateProperties = update(updateProperties)

        successResult = (String)updateProperties.getProperty(SharedConstants.SUCCESS_RESULT);
        internalError = (String)updateProperties.getProperty(SharedConstants.INTERNAL_ERROR);

        if (null != internalError)
        {
             internalErrorFlag = true;
        }

        //Sucessful update done, now delete the temporarily uploaded file
        deleteTempFile(archivePath);

        if (false)
        {
             redirectTo = redirectOnSuccess;
        } 
        else 
        {
              isAlertNeeded = true;
              alertSummary = "Update Failed";
              alertDetails = "Not Yet Implemented";
              redirectTo = redirectOnFailure;
        }

        handlerCtx.setOutputValue ("isAlertNeeded", Boolean.toString(isAlertNeeded));
        handlerCtx.setOutputValue ("alertSummary", alertSummary);
        handlerCtx.setOutputValue ("alertDetails", alertDetails);
        handlerCtx.setOutputValue ("redirectTo", redirectTo);

        sLog.fine("InstallationHandlers.jbiUpdateValidatedArchive(...), " +
                               " isAlertNeeded=" + isAlertNeeded +
                               ", alertSummary=" + alertSummary +
                               ", alertDetails=" + alertDetails +
                               ", redirectTo=" + redirectTo);
    }


    /**
    * <p> Deletes the uploaded/installed archives from temporary location on disk
     * <p> Input  value: "archiveStatus" -- Type: <code>Boolean</code></p>
     ** <p> Input  value: "archivePath" -- Type: <code>String</code></p>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="deleteUploadedArchiveFromTmpDir",
             input={
                 @HandlerInput(name="archivePath", type=String.class, required=true)}
             )

    public static void  deleteUploadedArchiveFromTmpDir(HandlerContext handlerContext)
    {
        String archivePath  = (String) handlerContext.getInputValue ("archivePath");
        deleteTempFile(archivePath);
    }
     /**
     *	 This method deletes the uploaded file from the temporary location on local hard 
     *  disk once the install is completed successfully or cancel button is clicked
     *  @param  Full path/location of temporary uploaded file
     */
    private static void deleteTempFile(String aTempFile)
    {
        InstallationBean installationBean = BeanUtilities.getInstallationBean();
        if (installationBean.getUploadSelected())
        {
            sLog.fine("InstallationHandlers.deleteTempFile(...), delete archive..." + aTempFile);
            Boolean deletedUploadedFileInTmpDir =  FileUtil.delete(aTempFile);
            sLog.fine("InstallationHandlers.deleteUploadedArchiveFromTmpDir(...), deletedUploadedFileInTmpDir=" + deletedUploadedFileInTmpDir);
        }
    }

    /**
     * <p> Will set the filter type in the ListBean.
     * <p> Input  value: "operation" -- Type: <code> java.lang.String</code>
     * Valid operations: The filter type
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="initializeTargetShowList")
    public static void initializeTargetShowList(HandlerContext handlerCtx)
    {
        ShowBean showBean = BeanUtilities.getShowBean();
        showBean.initTargetNames();
    }


    /**
     *  <p> This handler returns a list of Standalone server in sorted order </p>
     *  <p> Input  value: "list1" -- Type: <code> java.util.List</code> list one
     *  <p> Input  value: "list2" -- Type: <code> java.util.List</code> list two
     *  <p> Output value: "optionList" -- Type: <code>java.util.List</code>/</p>
     *  @param  context The HandlerContext.
     */
    @Handler(id="addListToOptions",
             input={
                 @HandlerInput(name="inputList", type=java.util.List.class, required=true),
                 @HandlerInput(name="inputOptions", type=java.util.List.class, required=true)},
             output={
                 @HandlerOutput(name="outputOptions", type=java.util.List.class)})
    public static void addListToOptions(HandlerContext handlerCtx)
    {
        List inputList    = (List)handlerCtx.getInputValue("inputList");
        List inputOptions = (List)handlerCtx.getInputValue("inputOptions");
        ArrayList outputOptions = new ArrayList();
        if (inputOptions != null)
        {
            outputOptions.addAll(inputOptions);
        }
        for (int i=0; i<inputList.size(); i++)
        {
            Option o = new Option ((String)inputList.get(i),(String)inputList.get(i));
            outputOptions.add(o);
        }
        handlerCtx.setOutputValue("outputOptions", outputOptions);
    }


    /**
     * <p> will installled and uninstall the current component based on the targets in the input lists </p>
     * Valid operations: The filter type
     * <p> Input  value: "installList" -- Type: <code> java.util.List</code> list of target to install to </p>
     * <p> Input  value: "uninstallList" -- Type: <code> java.util.List</code> list of target to uninstall from </p>
     * <p> Output value: "isAlertNeeded" -- Type: <code>java.lang.Boolean</code></p>
     * <p> Output value: "alertSummary" -- Type: <code>String</code>/</p>
     * <p> Output value: "alertDetails" -- Type: <code>String</code>/</p>
     * Valid types: 'deployments,' 'bindingsEngines,' or 'libraries.'
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiManageTargets",
            input={
            @HandlerInput (name="installList", type=java.util.List.class, required=true),
            @HandlerInput (name="uninstallList", type=java.util.List.class, required=true)},
        output={
            @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
            @HandlerOutput (name="alertSummary", type=String.class),
            @HandlerOutput (name="alertDetails", type=String.class)})
    public static void jbiManageTargets(HandlerContext handlerCtx)
    {

        List installList = (List)handlerCtx.getInputValue("installList");
        List uninstallList = (List)handlerCtx.getInputValue("uninstallList");
        // Retrieve the needed information from the show bean
        ShowBean showBean = BeanUtilities.getShowBean();
//        String[] selectedNames = showBean.getTargetNames();
//        String[] originalNames = showBean.getOriginalTargetNames();
        String name = showBean.getName();
        String type = showBean.getType();

        String alertDetails  = "";
        String alertSummary  = "";
        String internalError = "";
        String successResult = "";
        String failureResult = "";

        // Convert the arrays to list, so we can easily determine what has
        // been added or removed from the selected entries.
//        List selected = Arrays.asList(selectedNames);
//        List original = Arrays.asList(originalNames);

        Properties prop = new Properties();
        prop.setProperty(SharedConstants.KEY_NAME, name);
        prop.setProperty(SharedConstants.KEY_TYPE, type);
        prop.setProperty(KEY_ARCHIVE_NAME, name);

        int countFailedDeletions     = 0;
        int countSuccessfulDeletions = 0;
        int countFailedInstalls      = 0;
        int countSuccessfulInstalls  = 0;

        boolean failureFlag       = false;
        boolean internalErrorFlag = false;
        boolean isAlertNeeded     = false;


        // First we will uninstall any component/assembly that was in the original list,
        // but is not in the new selected list.
/*        for (Iterator it=original.iterator(); it.hasNext(); )
        {
            String target = (String)it.next();
            if (!(selected.contains(target)))
            {
                ArrayList targetList = new ArrayList();
                targetList.add(target);
*/
           for (Iterator it = uninstallList.iterator(); it.hasNext();) {
               String target = (String)it.next();

               ArrayList<String> targetList = new ArrayList<String>();
               targetList.add(target);


                // When managing targets, when we remove a component/assembly/library
                // we want to make sure it is retained in the domain.
                boolean retainFlag = true;

                delete (prop, retainFlag, targetList);

                successResult = (String)prop.getProperty(SharedConstants.SUCCESS_RESULT);
                internalError = (String)prop.getProperty(SharedConstants.INTERNAL_ERROR);

                if (null != internalError)
                {
                    internalErrorFlag = true;
                    break;
                }

                if (null != successResult)
                {
                    ++countSuccessfulDeletions;
                }

                else
                {
                    failureFlag = true;
                    ++countFailedDeletions;
                    failureResult = (String)prop.getProperty(SharedConstants.FAILURE_RESULT);
                    alertDetails += failureResult + "<br>";
                }
            }
        //}

        prop = new Properties();
        prop.setProperty(SharedConstants.KEY_NAME, name);
        prop.setProperty(SharedConstants.KEY_TYPE, type);
        prop.setProperty(KEY_ARCHIVE_NAME, name);

        // Next we will install any component that is in the new selected list, but
        // was not in the original list.
        if (!(internalErrorFlag))
        {
            for (Iterator it=installList.iterator(); it.hasNext(); )
            {
                String target = (String)it.next();
//                if (!(original.contains(target)))
//                {
                    prop = install(prop,target);

                    successResult = (String)prop.getProperty(SharedConstants.SUCCESS_RESULT);
                    internalError = (String)prop.getProperty(SharedConstants.INTERNAL_ERROR);

                    if (null != internalError)
                    {
                        internalErrorFlag = true;
                        break;
                    }

                    if (null != successResult)
                    {
                        ++countSuccessfulInstalls;
                    }

                    else
                    {
                        failureFlag = true;
                        ++countFailedInstalls;
                        failureResult = (String)prop.getProperty(SharedConstants.FAILURE_RESULT);
                        alertDetails += failureResult + "<br>";
                    }
                }
            }
//        }

        // Display the alert message if a failure was encountered during manage targets
        if (failureFlag || internalErrorFlag)
        {
            isAlertNeeded = true;
            if (internalErrorFlag)
            {
                alertSummary = I18nUtilities.getResourceString ("jbi.internal.error.summary");
                alertDetails = internalError;
            }
            else
            {
                //if ((countFailedInstalls > 0) && (countFailedDeletions > 0))
                //{
                //    alertSummary = I18nUtilities.getResourceString ("jbi.manage.targets.install.and.delete.failure");
                //}
                //else if (countFailedInstalls > 0)
                //{
                //    alertSummary = I18nUtilities.getResourceString ("jbi.manage.targets.install.failure");
                //}
                //else if (countFailedDeletions > 0)
                //{
                //    alertSummary = I18nUtilities.getResourceString ("jbi.manage.targets.install.failure");
                //}
            }
            alertSummary = I18nUtilities.getResourceString ("jbi.manage.targets.failure");
            alertDetails = BeanUtilities.addAlertFooterMessage(alertDetails);
        }
        handlerCtx.setOutputValue ("isAlertNeeded", Boolean.toString(isAlertNeeded));
        handlerCtx.setOutputValue ("alertSummary", alertSummary);
        handlerCtx.setOutputValue ("alertDetails", alertDetails);

        sLog.fine("InstallationHandlers.manageTargets(...), " +
                               " isAlertNeeded=" + isAlertNeeded +
                               ", alertSummary=" + alertSummary +
                               ", alertDetails=" + alertDetails);

        sLog.fine("InstallationHandlers.manageTargets(...), " +
                               " countSuccessfulDeletions=" + countSuccessfulDeletions +
                               ", countSuccessfulInstalls=" + countSuccessfulInstalls +
                               ", countFailedInstalls=" + countFailedInstalls +
                               ", countFailedDeletions=" + countFailedDeletions);

    }


    /**
     * <p> Delegates JBI removal requests for each selected row; accumulates failures into alert
     * <p> Input  value: "tableRowGroup" -- Type: <code> com.sun.webui.jsf.component.TableRowGroup</code></p>
     * <p> Input  value: "targetName" -- Type: <code> java.lang.String</code>
     * <p> Output value: "isAlertNeeded" -- Type: <code>java.lang.Boolean</code></p>
     * <p> Output value: "alertSummary" -- Type: <code>String</code>/</p>
     * <p> Output value: "alertDetails" -- Type: <code>String</code>/</p>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiRemoveSelectedSingleTargetRows",
             input={
                 @HandlerInput(name="tableRowGroup", type=TableRowGroup.class, required=true),
                 @HandlerInput(name="targetName", type=String.class, required=true)},
             output={
                 @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
                 @HandlerOutput (name="alertSummary", type=String.class),
                 @HandlerOutput (name="alertDetails", type=String.class)})

    public static void jbiRemoveSelectedSingleTargetRows(HandlerContext handlerCtx)
    {

        TableRowGroup trg = (TableRowGroup)
                            handlerCtx.getInputValue("tableRowGroup");
        String targetName = (String)
                            handlerCtx.getInputValue("targetName");
        sLog.fine("InstallationHandlers.jbiRemoveSelectedSingleTargetRows(" +
                               trg + ", " + targetName + ")");

        // TBD logic goes here...
        List deletionRows = TableUtilities.getSelectedRowProperties(trg);
        Iterator rowIt = deletionRows.iterator();

        int countFailedDeletions = 0;
        int countSuccessfulDeletions = 0;

        String alertDetails = "";
        String alertSummary = "";
        boolean isAlertNeeded = false;

        while (rowIt.hasNext())
        {
            Properties rowProperties = (Properties)rowIt.next();
            String rowName = (String)rowProperties.getProperty(SharedConstants.KEY_NAME);
            String rowType = (String)rowProperties.getProperty(SharedConstants.KEY_TYPE);

        List targets = new ArrayList();
        targets.add(targetName);

            // When deleting a row (component, deployment, or library) from the
        // shared single target (cluster or stand-alone instance) table, (via a button)
        // we always want to retain the component, deployment, or library in the domain.
            boolean retainFlag = true;

            delete (rowProperties, retainFlag, targets);

            String successResult = (String)rowProperties.getProperty(SharedConstants.SUCCESS_RESULT);

            if (null != successResult)
            {
                ++ countSuccessfulDeletions;
                sLog.fine("InstallationHandlers.jbiRemoveSelectedSingleTargetRows(...), " +
                                       " success for " + rowProperties +
                                       ", countFailedDeletions=" + countFailedDeletions +
                                       ", countSuccessfulDeletions=" + countSuccessfulDeletions);
            }
            else
            {
        isAlertNeeded = true; // at least one failure
        alertSummary =
            I18nUtilities.getResourceString ("jbi.remove.from.target.failed");

                ++ countFailedDeletions;
                sLog.fine("InstallationHandlers.jbiRemoveSelectedSingleTargetRows(...), " +
                                       " failure for " + rowProperties +
                                       ", countFailedDeletions=" + countFailedDeletions +
                                       ", countSuccessfulDeletions=" + countSuccessfulDeletions);
                String failureResult = (String)
                                       rowProperties.getProperty(SharedConstants.FAILURE_RESULT);

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
                Object[] args = {rowName, details};
                String alertDet =
                    GuiUtil.getMessage(I18nUtilities.getResourceString("jbi.deletion.failed.for.row"), args);
                alertDetails += alertDet + "<br />";
            }

    }

        handlerCtx.setOutputValue ("isAlertNeeded", Boolean.toString(isAlertNeeded));
        handlerCtx.setOutputValue ("alertSummary", alertSummary);
        handlerCtx.setOutputValue ("alertDetails", alertDetails);

       sLog.fine("InstallationHandlers.jbiInstallValidatedArchive(...), " +
                               " isAlertNeeded=" + isAlertNeeded +
                               ", alertSummary=" + alertSummary +
                               ", alertDetails=" + alertDetails);
    }

    /*
    *  <p> This handler returns a 2 list of Installation target classes. One
    *      for avialable installation targets for the current component and
    *      second the targets list where the current component currently installed </p>
    *  <p> Input  value: "list1" -- Type: <code> java.util.List</code> list one
    *  <p> Input  value: "list2" -- Type: <code> java.util.List</code> list two
    *  <p> Input  value: "isJBIArcheiveAvailable" -- Type: <code> java.lang.Boolean </code>
    *  <p> Output value: "avialableTargetList" -- Type: <code>java.util.List</code>/</p>
    *  <p> Output value: "installedTargetList" -- Type: <code>java.util.List</code>/</p>
    *  @param  context The HandlerContext.
    */
   @Handler(id="jbiGetTargetsLists",
            input={
                @HandlerInput(name="inputList", type=java.util.List.class, required=true),
                @HandlerInput(name="inputOptions", type=java.util.List.class, required=true),
                @HandlerInput(name="isJBIArchiveAvailable", type=Boolean.class, required=true)},
            output={
               @HandlerOutput(name="availableTargetList", type=java.util.List.class),
               @HandlerOutput(name="installedTargetList", type=java.util.List.class)})
   public static void jbiGetTargetsLists(HandlerContext handlerCtx)
   {
       List inputList    = (List)handlerCtx.getInputValue("inputList");
       List inputOptions = (List)handlerCtx.getInputValue("inputOptions");
       Boolean isJBIArcheiveAvailable = (Boolean) handlerCtx.getInputValue("isJBIArchiveAvailable");
       ArrayList<InstallationTarget> targetsList = new ArrayList<InstallationTarget>();
       ArrayList<InstallationTarget> availableTargetList = new ArrayList<InstallationTarget>();
       ArrayList<InstallationTarget> installedTargetList = new ArrayList<InstallationTarget>();
       if (inputOptions != null)
       {
           for (Iterator iter = inputOptions.iterator(); iter.hasNext();) {
              Option targetOption = (Option) iter.next();
              String targetName = (String)targetOption.getValue();
              InstallationTarget it =  new InstallationTarget(targetName);
              if(!isJBIArcheiveAvailable && targetName.equals(InstallationTarget.SERVER)) {
                  // clear the preselected "server"
                  it.setSelected(false);
              }
              targetsList.add(it);
           }
       }
       if (inputList != null)
       {
           for (Iterator iter = inputList.iterator(); iter.hasNext();) {
               String targetOption = (String) iter.next();
               InstallationTarget it =  new InstallationTarget(targetOption);
               targetsList.add(it);
            }
       }
       if(!isJBIArcheiveAvailable) {
           // identify the installed and avialable targets
           // will be used by manage targets screen.
           ShowBean showBean = BeanUtilities.getShowBean();
           for (InstallationTarget target : targetsList) {
               String targetName = target.getName();
               String status = showBean.check(targetName);
               if (null != status)
               {
                   installedTargetList.add(target);
               }else {
                   availableTargetList.add(target);
               }
           }

       }else {
           availableTargetList.addAll(targetsList);
       }

      handlerCtx.setOutputValue("availableTargetList", availableTargetList);
      handlerCtx.setOutputValue("installedTargetList", installedTargetList);
   }




   /**
    * <p> return a list of selected targets name from the avialable table
    * <p> Input  value: "availableTargetTableRowGroup" -- Type: <code> com.sun.webui.jsf.component.TableRowGroup</code></p>
    * <p> Output value: "selectedAvailableTargetsList" -- Type: <code>String</code>/</p>
    * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
    */
   @Handler(id="jbiGetSelectedTargetsFromAvailableTable",
            input={
                @HandlerInput(name="availableTableRowGroup", type=TableRowGroup.class, required=true)},
            output={
                @HandlerOutput (name="selectedAvailableTargetsList", type=java.util.List.class)})

   public static void jbiGetSelectedTargetsFromAvailableTable(HandlerContext handlerCtx)  {

       ArrayList<String> selectedList = new ArrayList<String>();
       String[] targetsName = null;
       TableRowGroup trg = (TableRowGroup)
                           handlerCtx.getInputValue("availableTableRowGroup");


       ObjectListDataProvider dp = (ObjectListDataProvider) trg.getSourceData();
       dp.commitChanges();

       if (null != dp && dp.getRowCount() > 0)  {
            FieldKey targetname = dp.getFieldKey(KEY_NAME);
            RowKey[] rowKeys = trg.getSelectedRowKeys();
            targetsName = new String[rowKeys.length];
            for(int index = 0; index < rowKeys.length; index++)  {
                 String name = (String) dp.getValue(targetname, rowKeys[index]);
                 selectedList.add(name);
                 targetsName[index] = name;
            }
       }
       InstallationBean installationBean = BeanUtilities.getInstallationBean();
       installationBean.setTargetNames(targetsName);

       handlerCtx.setOutputValue ("selectedAvailableTargetsList", selectedList);
    }

   /**
    * <p> return a list of selected targets name from the installed table
    * <p> Input  value: "availableTargetTableRowGroup" -- Type: <code> com.sun.webui.jsf.component.TableRowGroup</code></p>
    * <p> Output value: "selectedInstalledTargetsList" -- Type: <code>String</code>/</p>
    * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
    */
   @Handler(id="jbiGetSelectedTargetsFromInstalledTable",
            input={
                @HandlerInput(name="installedTableRowGroup", type=TableRowGroup.class, required=true)},
            output={
                @HandlerOutput (name="selectedInstalledTargetsList", type=java.util.List.class)})

   public static void jbiGetSelectedTargetsFromInstalledTable(HandlerContext handlerCtx)  {

       ArrayList<String> selectedList = new ArrayList<String>();
       String[] targetsName = null;
       TableRowGroup trg = (TableRowGroup)
                           handlerCtx.getInputValue("installedTableRowGroup");


       ObjectListDataProvider dp = (ObjectListDataProvider) trg.getSourceData();
       dp.commitChanges();

       if (null != dp && dp.getRowCount() > 0)  {
            FieldKey targetname = dp.getFieldKey(KEY_NAME);
            RowKey[] rowKeys = trg.getSelectedRowKeys();
            targetsName = new String[rowKeys.length];
            for(int index = 0; index < rowKeys.length; index++)  {
                 String name = (String) dp.getValue(targetname, rowKeys[index]);
                 selectedList.add(name);
                 targetsName[index] = name;
            }
       }
       InstallationBean installationBean = BeanUtilities.getInstallationBean();
       installationBean.setTargetNames(targetsName);

       handlerCtx.setOutputValue ("selectedInstalledTargetsList", selectedList);
    }




   /**
    * <p> return a list of targets built from a single target
    * <p> Input  value: "singleTarget" -- Type: <code>String</code></p>
    * <p> Output value: "installTargetsList" -- Type: <code>java.util.List</code>/</p>
    * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
    */
   @Handler(id="jbiCreateSingleTargetList",
            input={
                @HandlerInput(name="singleTarget", type=String.class, required=true)},
            output={
                @HandlerOutput (name="installTargetsList", type=java.util.List.class)})

   public static void jbiCreateSingleTargetList(HandlerContext handlerCtx)  {

       ArrayList<String> installTargetsList = new ArrayList<String>();
       String singleTarget = (String)
           handlerCtx.getInputValue("singleTarget");

       installTargetsList.add(singleTarget);
       sLog.fine("InstallationHandlers.jbiCreateSingleTargetList(" + singleTarget +
                               "): " + installTargetsList);

       handlerCtx.setOutputValue ("installTargetsList", installTargetsList);
    }


    private static void delete (Properties aRowProperties,
                                boolean aRetainFlag,
                                List aTargetsList)
    {
        sLog.fine("InstallationHandlers.delete(" + aRowProperties +
                               ", " + aTargetsList + ")");
        DeletionBean deletionBean = BeanUtilities.getDeletionBean();
        // aRowProperties = 
	deletionBean.delete(aRowProperties, aRetainFlag, aTargetsList);

        // TBD accumulate results
    }


    /**
     * Install using the information from the specified in the properties variable
     * to the specified target.
     * @param aSource Properties containing the parameters
     * @param aTarget the name of the target to install to
     * @return result Properteis contain the result information
     */
    private static Properties install (Properties aProp, String aTarget)
    {
        InstallationBean installationBean = BeanUtilities.getInstallationBean();
        aProp = installationBean.installValidatedArchive (aProp,aTarget);
        return aProp;
    }


    private static Properties install(Properties anInstallationProperties)
    {
        Properties result = null;
        sLog.fine("InstallationHandlers.install()");
        InstallationBean installationBean = BeanUtilities.getInstallationBean();
        anInstallationProperties =
        installationBean.installValidatedArchive(anInstallationProperties);

        // TBD show result
        result = anInstallationProperties;
        sLog.fine("InstallationHandlers.install(), result=" + result);
        return result;
    }

}


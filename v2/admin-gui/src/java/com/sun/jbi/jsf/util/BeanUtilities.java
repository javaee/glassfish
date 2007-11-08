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
 *  BeanUtilities.java
 *
 */

package com.sun.jbi.jsf.util;

import com.sun.jbi.jsf.bean.LoggingBean;
import com.sun.jbi.jsf.bean.AlertBean;
import com.sun.jbi.jsf.bean.ArchiveBean;
import com.sun.jbi.jsf.bean.DeletionBean;
import com.sun.jbi.jsf.bean.InstallationBean;
import com.sun.jbi.jsf.bean.JBIComponentConfigBean;
import com.sun.jbi.jsf.bean.ListBean;
import com.sun.jbi.jsf.bean.RuntimeConfigurationBean;
import com.sun.jbi.jsf.bean.OperationBean;
import com.sun.jbi.jsf.bean.ShowBean;
import com.sun.jbi.jsf.bean.UploadCopyRadioBean;
import com.sun.jbi.jsf.bean.TargetBean;
import com.sun.jbi.jsf.configuration.beans.ConfigurationBean;

import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jbi.ui.common.JBIManagementMessage;
import com.sun.jbi.ui.common.JBIRemoteException;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

/**
 *
 * This class is used to provide utilities function for JBI related tasks
 *
 **/

public final class BeanUtilities
{
    public static final String ADMIN_CONSOLE_BUNDLE = "com.sun.enterprise.tools.admingui.resources.Strings";

     /**
     * Controls printing of diagnostic messages to the log
     */
    private static Logger sLog = JBILogger.getInstance();




    public static AlertBean getAlertBean()
    {
        AlertBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{AlertBean}", AlertBean.class);
        result = (AlertBean) ve.getValue(elCtx);
        return result;
    }

    public static DeletionBean getDeletionBean()
    {
        DeletionBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{DeletionBean}", DeletionBean.class);
        result = (DeletionBean) ve.getValue(elCtx);
        return result;
    }

    public static InstallationBean getInstallationBean()
    {
        InstallationBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{InstallationBean}", InstallationBean.class);
        result = (InstallationBean) ve.getValue(elCtx);
        return result;
    }

    public static OperationBean getOperationBean()
    {
        OperationBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{OperationBean}", OperationBean.class);
        result = (OperationBean) ve.getValue(elCtx);
        return result;
    }

     /*
     *@return static copy of UploadCopyRadioBean Bean's instance
     */

    public static UploadCopyRadioBean getUploadCopyRadioBean()
    {
        UploadCopyRadioBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{UploadCopyRadioBean}", UploadCopyRadioBean.class);
        result = (UploadCopyRadioBean) ve.getValue(elCtx);
        return result;
    }

    public static ArchiveBean getArchiveBean()
    {
        ArchiveBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{ArchiveBean}", ArchiveBean.class);
        result = (ArchiveBean) ve.getValue(elCtx);
        return result;
    }

    public static LoggingBean getLoggingBean()
    {
        LoggingBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve = ef.createValueExpression(elCtx, "#{LoggingBean}", LoggingBean.class);
        result = (LoggingBean) ve.getValue(elCtx);
        return result;
    }

    public static ConfigurationBean getConfigurationBean()
    {
        ConfigurationBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{ConfigurationBean}", ConfigurationBean.class);
        result = (ConfigurationBean) ve.getValue(elCtx);
        return result;
    }

    public static RuntimeConfigurationBean getRuntimeConfigurationBean()
    {
        RuntimeConfigurationBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{RuntimeConfigurationBean}", RuntimeConfigurationBean.class);
        result = (RuntimeConfigurationBean) ve.getValue(elCtx);
        return result;
    }


    public static ListBean getListBean()
    {
        ListBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{ListBean}", ListBean.class);
        result = (ListBean) ve.getValue(elCtx);
        return result;
    }

    public static ShowBean getShowBean()
    {
        ShowBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{ShowBean}", ShowBean.class);
        result = (ShowBean) ve.getValue(elCtx);
        return result;
    }

    public static TargetBean getTargetBean()
    {
        TargetBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{TargetBean}", TargetBean.class);
        result = (TargetBean) ve.getValue(elCtx);
        return result;
    }

    public static JBIAdminCommands getClient()
    {
        JBIAdminCommands result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{JBIConfigBean.client}", JBIAdminCommands.class);
        result = (JBIAdminCommands) ve.getValue(elCtx);
        return result;
    }

    public static String getStringPropertyUsingExpression(String anExpression)
    {
        String result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve = ef.createValueExpression(elCtx, anExpression, String.class);
        result = (String) ve.getValue(elCtx);
        return result;
    }

    public static void setStringPropertyUsingExpression(String aStringValue, String anExpression)
    {
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve = ef.createValueExpression(elCtx, anExpression, String.class);
        ve.setValue(elCtx, aStringValue);
    }


    public static boolean checkFilterOptions (String aComponentState,
                                              String aFilterState,
                                              ArrayList optionList)
    {
        boolean flag = true;
        if ( null !=aFilterState)
    {
            if (SharedConstants.DROP_DOWN_TYPE_SHOW_ALL.equals(aFilterState))
        {
            //no-op --include all states
        }
        else //filter state is not all
            {
                    flag = false;
            if ( null != optionList)
            {
                    int index = optionList.indexOf(aFilterState);
                if (index != -1)
                        {
                             flag = containsIgnoreCase (aComponentState,(String)optionList.get(index));
                        }
            }
        }
        }
        return flag;
    }


    /**
     * Will extract the JBIManagementMessgae from the Remote exception.
     * @param ex the exception to process
     */
    public static JBIManagementMessage extractJBIManagementMessage (Exception ex)
    {
        JBIManagementMessage mgmtMsg = null;
        if (ex instanceof JBIRemoteException)
        {
            JBIRemoteException rEx = (JBIRemoteException)ex;
            mgmtMsg = rEx.extractJBIManagementMessage();
        }
        else
        {
            String exMessage = ex.getMessage();
            mgmtMsg = JBIManagementMessage.createJBIManagementMessage(exMessage);
        }
        sLog.fine("BeanUtitlies.extractJBIManagementMessage(...), ex.getClass()=" + ex.getClass() + ", mgmtMsg=" + mgmtMsg);
        return mgmtMsg;
    }


    /**
     * @param jbiResultXml - a common client result that may contain zero or more warnings
     * @returns JBIManagementMessage - null if no warnings, otherwise a message to be displayed
     */
    public static JBIManagementMessage extractJBIManagementMessage (String jbiResultXml)
    {
        JBIManagementMessage mgmtMsg =
            JBIManagementMessage.createJBIManagementMessage(jbiResultXml);

        sLog.fine("BeanUtilities.extractJBIManagementMessage(String jbiResultXml), mgmtMsg=" + mgmtMsg);
        if ( mgmtMsg == null )
        {
            // normal case: no-op (return null)
        }
        else
        {
            // now format the jbi result xml according to the esb result format
            String formattedJbiResult = jbiResultXml; // actual formatting TBD

            // check the mgmt msg is a success --or-- warning or error
            if ( !mgmtMsg.isFailedMsg() )
            {
                sLog.fine("BeanUtilities.extractJBIManagementMessage(String jbiResultXml), happy: isFailedMsg()=false");
            }
            else
            {
                sLog.fine("BeanUtilities.extractJBIManagementMessage(String jbiResultXml), error: isFailedMsg()=true");
            }
        }
        return mgmtMsg;
    }

    /**
     * Will add the alert footer message to the provided alert message string
     * @param aMsg alert message string
     * @return the alert message with the appended footer message
     */
    public static String addAlertFooterMessage (String aAlertMsg)
    {
        String footerMsg = I18nUtilities.getResourceString ("jbi.alert.footer.see.log.message");
        aAlertMsg += "<br>" + footerMsg;
        return aAlertMsg;
    }


    private static boolean containsIgnoreCase (String str1, String str2)
    {
        String str1Upper = str1.toUpperCase();
        String str2Upper = str2.toUpperCase();
        return (str1Upper.contains(str2Upper));
    }



    public static JBIComponentConfigBean getJBIComponentConfigBean()
    {
        JBIComponentConfigBean result = null;
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve =
        ef.createValueExpression(elCtx, "#{JBIComponentConfigBean}",
                JBIComponentConfigBean.class);
        result = (JBIComponentConfigBean) ve.getValue(elCtx);
        return result;
    }

}



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
 *  InstallationBean.java
 */


package com.sun.jbi.jsf.bean;

import com.sun.webui.jsf.model.Option;
import java.util.List;
import java.util.ArrayList;

import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jbi.ui.common.JBIManagementMessage;
import com.sun.jbi.ui.common.JBIRemoteException;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.SharedConstants;
import com.sun.jbi.jsf.util.I18nUtilities;
import java.util.Properties;
import java.util.logging.Logger;

import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.ObjectListDataProvider;

public class InstallationBean
{

    public final static String DEFAULT_PE_TARGET = JBIAdminCommands.SERVER_TARGET_KEY;

   	//Get Logger to log fine mesages for debugging
	private static Logger sLog = JBILogger.getInstance();

    /**
     * Set the mUploadSelected variable true for cleaning up temporary uploaded files
     * @param aSource Properties containing the parameters
     * @param aTarget the name of the target to install to
     * @return result Properteis contain the result information
     */
    public Properties installValidatedArchive (Properties aSource, String aTarget)
    {
        Properties result = aSource;
        String jbiType     = aSource.getProperty("type");
        String archiveName = aSource.getProperty("archiveName");
        String archivePath = aSource.getProperty("path");

        result = tryInstallToOneTarget(archiveName, 
                                       archivePath, 
                                       jbiType, 
                                       aTarget, 
                                       result);
        return result;
    }


    public Properties installValidatedArchive(Properties aSource)
    {
        final String TARGET_DOMAIN = JBIAdminCommands.DOMAIN_TARGET_KEY;
        
        Properties result = aSource;

        sLog.fine("InstallationBean.installValidatedArchive(" + aSource + "), mTargetNames=" + mTargetNames);

        String archivePath = aSource.getProperty("path");
        String jbiType     = aSource.getProperty("type");
        String archiveName = aSource.getProperty("archiveName");

        if (null != mTargetNames)
        {
            /*
             * For PE with clusters (or EE) this list could have zero targets, implying domain only
             */
            if (0 == mTargetNames.length) // DOMAIN-only
            {
				sLog.fine("InstallationBean.installValidatedArchive(" + aSource + ") install to \"domain\"");
                result = tryInstallToOneTarget(null,
                                               archivePath, 
                                               jbiType, 
                                               TARGET_DOMAIN, 
                                               result);
            }
            /*
             * For PE without clusters this list will have one "server" target only
             * For PE with clusters (or EE) this list will have one or more named targets only
             */
            else
            {
                for (int i = 0; i < mTargetNames.length; ++i)
                {
					sLog.fine("InstallationBean.installValidatedArchive(" + aSource + ") install to \"" + mTargetNames[i] + "\"");
                    result = tryInstallToOneTarget(archiveName, 
                                                   archivePath, 
                                                   jbiType, 
                                                   mTargetNames[i], 
                                                   result);
                }
            }
        }

       sLog.fine("InstallationBean.installValidatedArchive(), result=" + result);
       return result;
    }


    private Properties tryInstallToOneTarget (String anArchiveName, 
                                              String anArchivePath, 
                                              String aJbiType, 
                                              String aTargetName, 
                                              Properties aSource)
    {
        Properties result = aSource;

        mIsAlertNeeded = false;
        String installResult = "unknown";

        try
        {
            installResult = installValidatedArchiveToTarget(anArchiveName, 
                                                            anArchivePath, 
                                                            aJbiType, 
                                                            aTargetName);
            result.setProperty(SharedConstants.SUCCESS_RESULT, installResult);
           sLog.fine("InstallationBean.tryInstallToOneTarget(...), result=" + result);
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
        }
        sLog.fine("InstallationBean.tryInstallToOneTarget(" + aTargetName +
                               ", " + aJbiType + ", <properties>), result=" + result);
        return result;
    }


    private String installValidatedArchiveToTarget (String anArchiveName, 
                                                    String anArchivePath, 
                                                    String aJbiType, 
                                                    String aTarget)
    throws com.sun.jbi.ui.common.JBIRemoteException
    {
        String result = "";
	    sLog.fine("InstallationBean.installValidatedArihiveToTarget(" + anArchivePath + ", " + aJbiType + ", " + aTarget);
    
        JBIAdminCommands jac = BeanUtilities.getClient();

        if (null != jac)
        {
            if ((JBIConstants.JBI_BINDING_COMPONENT_TYPE.equals(aJbiType))
                || (JBIConstants.JBI_SERVICE_ENGINE_TYPE.equals(aJbiType)))
            {
                JBIComponentConfigBean configBean =
                    BeanUtilities.getJBIComponentConfigBean();
               
                Properties configProps =  configBean.getConfigurationProperties();
                if(configProps == null || configProps.size()== 0) {
                    if (anArchiveName != null)
                    {
                        result = jac.installComponentFromDomain(anArchiveName, aTarget);
                    }
                    else
                    {
                        result = jac.installComponent(anArchivePath, aTarget);
                    }
                    
                } else {
                    if (anArchiveName != null)
                    {
                        result = jac.installComponentFromDomain(anArchiveName,
                                configProps,aTarget);
                    }
                    else
                    {
                        result = jac.installComponent(anArchivePath, configProps, 
                                aTarget);
                    }
                }
                    
            }
            else if (JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE.equals(aJbiType))
            {
                if (anArchiveName != null)
                {
                    result = jac.deployServiceAssemblyFromDomain(anArchiveName, aTarget);
                }
                else
                {
                    result = jac.deployServiceAssembly(anArchivePath, aTarget);
                }                
            }
            else if (JBIConstants.JBI_SHARED_LIBRARY_TYPE.equals(aJbiType))
            {
                if (anArchiveName != null)
                {
                    result = jac.installSharedLibraryFromDomain(anArchiveName, aTarget);
                }
                else
                {
                    result = jac.installSharedLibrary(anArchivePath, aTarget);
                }
            }
        }
        else
        {
            sLog.fine("InstallationBean.installValidatedArchiveToTarget() TBD generate internal error alert, and log");
            mIsAlertNeeded = true;
            mResultSummary = I18nUtilities.getResourceString ("jbi.ee.install.impossible");
            mResultDetails = I18nUtilities.getResourceString ("jbi.ee.install.impossible.details");
        }

	    sLog.fine("InstallationBean.installValdidatedArchiveToTargetToTarget(...), result=" + result);
        return result;
    }


    public String getResultDetails()
    {
        // only once
        mIsAlertNeeded = false;
        sLog.fine("InstallationBean.getResultDetails()=" + mResultDetails + ")");
        return mResultDetails;
    }
    public String getResultSummary()
    {
        sLog.fine("InstallationBean.getResultSummary()=" + mResultSummary + ")");
        return mResultSummary;
    }

    public Boolean getIsAlertNeeded()
    {
        Boolean result = null;
        result = new Boolean(mIsAlertNeeded);
        sLog.fine("InstallationBean.isAlertNeeded()=" + result + ")");
        return result;
    }


    /**
     * Get the mUploadSelected variable for cleaning up temporary uploaded files
     * @return mUploadSelected boolean false if copy path was selected
     */

    public boolean getUploadSelected()
    {
        sLog.fine("InstallationBean.getUploadSelected()=" + mUploadSelected + ")");
        return mUploadSelected;
    }

    /**
     * Set the mUploadSelected variable true for cleaning up temporary uploaded files
     * @param uploadSelected boolean false if copy path was selected
     */
    public void setUploadPathSelected(boolean uploadSelected)
    {
        sLog.fine("InstallationBean.setUploadSelected(" + uploadSelected + ")");
        mUploadSelected = uploadSelected;
    }


    public String[] getTargetNames ()
    {
        return mTargetNames;
    }


    public void setTargetNames (String[] aTargetNames)
    {
        mTargetNames = aTargetNames;
    }

//-------------------------------

    public List getTargetNameList () throws com.sun.jbi.ui.common.JBIRemoteException
    {
        ArrayList targetNameList = null;
/*
        JBIAdminCommands jac = BeanUtilities.getClient();
        if (null != jac)
        {
            String targetName = null;
            esbClient = getEsbAdminClient ();
            QueryResult targetListRes = jac.listTargets ();
            List allTargets = instanceListRes.getTargetInfoList ();
            if (allTargets != null)
            {
                targetNameList = new ArrayList ();
                for (int i=0; i<allInstances.size (); i++)
                {
                    targetNameList.add (((TargetInfo)allTargets.get (i)).getName ());
                }
            }
        }
        else
        {
            if (DEBUG)
            { System.err.println("InstallationBean.getTargetNameList() TBD generate internal error alert, and log"); }
            mIsAlertNeeded = true;
            mResultSummary = "Installation not possible";
            mResultDetails = "Internal Error - unable to get client/connection";
        }
*/

        targetNameList = new ArrayList();
        targetNameList.add("Target 1");
        targetNameList.add("Target 2");
        targetNameList.add("Target 3");

        // if (DEBUG)
        // { System.err.println("InstallationBean..getTargetNameList(), result=" + result); }
        return targetNameList;

    }


    public void setAllTargetOptionList ()
    {
        String targetName;
        try
        {
            List targetList = getTargetNameList ();
            if ((targetList != null) && (targetList.size () != 0))
            {
                mAllTargets  = new Option[targetList.size ()];
                for (int i=0; i<targetList.size (); i++)
                {
                    targetName = targetList.get (i).toString ();
                    mAllTargets[i] = new Option (targetList.get (i).toString (),targetList.get (i).toString ());
                }
            }
        }
        catch (com.sun.jbi.ui.common.JBIRemoteException jbiRemoteEx)
        {
            // TBD use logging warning
            sLog.fine("Unable to obtain Target option list.");
            jbiRemoteEx.printStackTrace(System.err);
            mIsAlertNeeded = true;
            mResultSummary = I18nUtilities.getResourceString("jbi.ee.domain.list.not.found"); //TBD refine
            mResultDetails = jbiRemoteEx.getMessage();
        }
    }

    public Option[] getAllTargetOptionList ()
    {
        if (mAllTargets == null)
        {
            setAllTargetOptionList ();
        }
        return mAllTargets;
    }
    private Option[] mAllTargets = null;
//-------------------------------------------------------------------

    private String[] mTargetNames = new String[] { DEFAULT_PE_TARGET};

    private boolean mIsAlertNeeded;
    private String mResultDetails;
    private String mResultSummary;
    private boolean mUploadSelected;
}



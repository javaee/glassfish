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
 * JbiArchiveValidationHandler.java
 *
 */

package com.sun.jbi.jsf.handlers;

import com.sun.enterprise.tools.admingui.util.FileUtil;
import com.sun.jbi.jsf.bean.InstallationBean;
import com.sun.jbi.jsf.bean.ArchiveBean;
import com.sun.jbi.jsf.bean.UploadCopyRadioBean;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.I18nUtilities;
import com.sun.jbi.jsf.util.JBIConstants;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.ValidationUtilities;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.webui.jsf.model.UploadedFile;
import java.io.File;
import java.util.logging.Logger;

public class JbiArchiveValidationHandler
{
    public final static String JBI_TYPE_COMP    = "component"; //not i18n

    //Get Logger to log fine mesages for debugging
	private static Logger sLog = JBILogger.getInstance();


    /**
     *  <p> This method return the temporary location of the uploaded file </p>
     *  <p> after uploading the file to local disk</p>
     *  <p> Input value: "file" -- Type: <code>com.sun.webui.jsf.model.UploadedFile</code></p>
     *  <p> Output value: "uploadedLoc" -- Type: <code>java.lang.String</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler (id="getUploadedFileLocation",
            input={
        @HandlerInput (name="file",     type=UploadedFile.class, required=true)},
            output={
        @HandlerOutput (name="uploadedLoc",     type=String.class)})
        public static void  getUploadedFileLocation (HandlerContext handlerCtx)
    {
	    final String WIN_CLIENT_SEPARATOR = "\\"; // cannot use File.separator when server not on Windows

        ArchiveBean archiveBean = BeanUtilities.getArchiveBean ();
        UploadedFile uploadedFile = (UploadedFile)handlerCtx.getInputValue ("file");
        String suffix = null;
        String prefix = null;

        if(uploadedFile != null)
        {
            String name = uploadedFile.getOriginalName ();
        sLog.fine("JbiArchiveValidationHandler.getUploadedFileLocation()" +
                       " original name=" + name);

         // fix for  bug# 6498910, 6520070, 6520475
         // in IE, getOriginalName() returns the full path, including the drive.
         //for any other browser, it just returns the file name.

        int lastIndex = name.lastIndexOf(WIN_CLIENT_SEPARATOR);
        if (lastIndex != -1){
             name = name.substring(lastIndex+1, name.length());
            sLog.fine("JbiArchiveValidationHandler.getUploadedFileLocation()" +
                           " truncated name=" + name);
            }
            archiveBean.setArchiveDisplayName (name);
            if (name.length () != 0 )
            {
                suffix = name.substring (name.indexOf ("."));
                prefix = name.substring (0, name.indexOf ("."));
                try
                {
                    String tmpFolder = FileUtil.getTempDirPath ();
                    File tmpFile = File.createTempFile (prefix, suffix, new File (tmpFolder));
                    uploadedFile.write (tmpFile);
                    sLog.fine("JbiArchiveValidationHandler.getUploadedFileLocation()" +
                                           "tmpFile=" + tmpFile.getAbsolutePath());
                    handlerCtx.setOutputValue ("uploadedLoc" , tmpFile.getCanonicalPath ());
                    archiveBean.setArchiveAbsolutePath (tmpFile.getAbsolutePath());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace ();
                    handlerCtx.setOutputValue ("uploadedLoc" , "");
                    archiveBean.setArchiveAbsolutePath ("");
                }
            }
            else
            {
                handlerCtx.setOutputValue ("uploadedLoc" , "");
                archiveBean.setArchiveAbsolutePath("");
            }

        }
    }

    /**
     *  <p> This method returns the value of user selected file path </p>
     *  <p> checks for non-null values form upload and copy paths and returns that</p>
     *  <p> Input value: "copyFilePath" -- Type: <code>java.lang.String</code></p>
     ** <p> Input value: "uploadFilePath" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "archiveFilePath" -- Type: <code>java.lang.String</code></p>
     *  @param  context The HandlerContext.
     */
    @Handler (id="getArchivePath",
            input={
        @HandlerInput (name="filePath",  type=String.class, required=true),
        @HandlerInput (name="uploadRadio",  type=boolean.class, required=true)},
            output={
        @HandlerOutput (name="archiveFilePath",     type=String.class)})
        public static void  getArchivePath (HandlerContext handlerCtx)
       {
        InstallationBean installBean =
                    BeanUtilities.getInstallationBean ();
        String filePath = (String)handlerCtx.getInputValue ("filePath");
        boolean uploadChecked = (Boolean) handlerCtx.getInputValue ("uploadRadio");
        if (uploadChecked)
        {
                //set to enable deleting temporary upploaded files
                //upload path selected
                installBean.setUploadPathSelected (true);
        }
        else
        {
                //copy path selected
                installBean.setUploadPathSelected (false);
                ArchiveBean archiveBean = BeanUtilities.getArchiveBean ();
                archiveBean.setArchiveDisplayName (filePath);

        }
        handlerCtx.setOutputValue ("archiveFilePath", filePath);
    }
    /**
     *  <p> This handler returns true if archive is valid  </p>
     *
     *
     * <p> Input value: "archivePath" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "isValid" -- Type: <code>Boolean</code>/</p>
     *  <p> Output value: "alertSummaryMsg" -- Type: <code>String</code>/</p>
     **  <p> Output value: "alertDetailMsg" -- Type: <code>String</code>/</p>
     *  <p> Output value: "navLoc" -- Type: <code>String</code>/</p>
     *  @param  context The HandlerContext.
     */
    @Handler (id="isValidArchive",
            input={
        @HandlerInput (name="archivePath", type=String.class, required=true),
        @HandlerInput (name="compType", type=String.class, required=true),
        @HandlerInput (name="navValid", type=String.class, required=true),
        @HandlerInput (name="navInvalid", type=String.class, required=true)},
            output={
        @HandlerOutput (name="isValid", type=Boolean.class),
        @HandlerOutput (name="isAlertNeeded", type=Boolean.class),
        @HandlerOutput (name="alertSummaryMsg", type=String.class),
        @HandlerOutput (name="alertDetailMsg", type=String.class),
        @HandlerOutput (name="navLoc", type=String.class)})
        public static void isValidArchive (HandlerContext handlerContext)
        {

        ArchiveBean archiveBean
                = BeanUtilities.getArchiveBean ();
        UploadCopyRadioBean uploadCpBean
                = BeanUtilities.getUploadCopyRadioBean ();
        String jbiType = "";

        String pathToFile = (String) handlerContext.getInputValue ("archivePath");
        String compType   = (String) handlerContext.getInputValue ("compType");

        uploadCpBean.setNavDestValid ((String) handlerContext.getInputValue ("navValid"));
        uploadCpBean.setNavDestInvalid ((String) handlerContext.getInputValue ("navInvalid"));

        archiveBean.setArchiveAbsolutePath (pathToFile);


        resetValidationParameters ();

        String alertDetailNotWellFormed = I18nUtilities.getResourceString ("jbi.install.wizard.invalid.notwellformed.text");
        String alertDetailNotFound = I18nUtilities.getResourceString ("jbi.install.wizard.invalid.filenotfound.text");
        String alertDetailMissingJbi = I18nUtilities.getResourceString ("jbi.install.wizard.invalid.missingjbixml.text");
        String alertDetailNotSchemaValid = I18nUtilities.getResourceString ("jbi.install.wizard.invalid.notschemavalid.text");
        String alertDetailMismatchArchiveWizard = I18nUtilities.getResourceString ("jbi.install.wizard.invalid.mismatcharchivewizard.text");

        int count = 0;

        boolean zipError     = archiveBean.getZipFileReadError ();
        boolean fileError    = archiveBean.getFileReadError ();
        boolean emptyInvalid = ValidationUtilities.isArchiveEmptyOrInValid ();

        if ( emptyInvalid || zipError || fileError )
        {
            setOutputInvalidAlertValue (alertDetailNotFound, handlerContext);
        }
        else
        {
            //also will set if jbi xml present or not
            ValidationUtilities.getMetaDataEntry ();

            if ( !archiveBean.getHasJbiXml () )
            {
                setOutputInvalidAlertValue (alertDetailMissingJbi, handlerContext);
            }
            else if ( !ValidationUtilities.isJbiXmlWellformed () )
            {
                setOutputInvalidAlertValue (alertDetailNotWellFormed, handlerContext);
            }
            /*
             * Disabling schema validation check :fix for CR 6504700
             * Schema validation apis at console and management level
             * differ in handling validation of xml files with elements
             * having namespace prefixes from several namespaces.
             * JBI Runtime uses JAXB for schema validation and console used javax.xml.validation apis
             *
             else if ( !ValidationUtilities.isJbiXmlSchemaValid () )
            {
                setOutputInvalidAlertValue (alertDetailNotSchemaValid);
            }*/
            else
            {
                String componentType = ValidationUtilities.getJbiType(
                                    ValidationUtilities.getJbiDocument ());
                if (componentType.equals (JBIConstants.JBI_BINDING_COMPONENT_TYPE)
                || componentType.equals (JBIConstants.JBI_SERVICE_ENGINE_TYPE))
                {
                    jbiType = JBI_TYPE_COMP;
                }
                else
                {
                    jbiType = componentType;
                }

                if ( !jbiType.equals (compType) )
                {
                    sLog.fine ("isValidArchive(), jbiType=" + jbiType + ", compType=" + compType);
                    setOutputInvalidAlertValue (alertDetailMismatchArchiveWizard, handlerContext);
                }
                else
                {
                    handlerContext.setOutputValue ("isValid", "true");
                    handlerContext.setOutputValue ("isAlertNeeded", "false");
                    handlerContext.setOutputValue ("navLoc", uploadCpBean.getNavDestValid ());
                    archiveBean.setJbiType (componentType);
                }
            }
        }
    }

    
    /**
     * <p> Deletes the invalid archives which were uploaded to temporary location on disk
     * <p> Input  value: "archiveStatus" -- Type: <code>Boolean</code></p>
     ** <p> Input  value: "archivePath" -- Type: <code>String</code></p>
     ** <p> Input  value: "uploadSelected" -- Type: <code>Boolean</code></p>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="deleteInvalidatedUploadedArchive",
             input={
                 @HandlerInput(name="archiveStatus", type=Boolean.class, required=true),
                 @HandlerInput(name="archivePath", type=String.class, required=true),
                 @HandlerInput(name="uploadSelected", type=Boolean.class, required=true)}
             )

    public static void  deleteInvalidatedUploadedArchive(HandlerContext handlerContext)
    {
        try
        {
           if ((Boolean) handlerContext.getInputValue ("uploadSelected"))
           {
               Boolean isValid  = (Boolean) handlerContext.getInputValue ("archiveStatus");
               String uploadedFileLoc  = (String) handlerContext.getInputValue ("archivePath");
               if (!isValid)
               {
                   FileUtil.delete(uploadedFileLoc);
               }
           }
        }
        catch(Exception ex)
        {
           ex.printStackTrace(); 
        }
    }
    
    /**
     * Set the outputValue of the isValidArchive handler
     * @param alertDetail is the alert's detail  message
     */
    private static void setOutputInvalidAlertValue (String aAlertDetail, HandlerContext aHandlerCtxt)
    {
        UploadCopyRadioBean uploadCpBean = BeanUtilities.getUploadCopyRadioBean ();
        String navDestIfInvalid = uploadCpBean.getNavDestInValid ();
        String alertSummary =
                I18nUtilities.getResourceString ("jbi.install.wizard.invalid.alertsummary.text");
        aHandlerCtxt.setOutputValue ("isValid", "false");
        aHandlerCtxt.setOutputValue ("isAlertNeeded", "true");
        aHandlerCtxt.setOutputValue ("alertSummaryMsg", alertSummary);
        aHandlerCtxt.setOutputValue ("alertDetailMsg", aAlertDetail);
        aHandlerCtxt.setOutputValue ("navLoc", navDestIfInvalid);
        //If upload path was chosen cleanup the uploaded file
        //From templocation
        //FileUtil.delete (getArchiveAbsolutePath ());
    }

    /**
     * Reset the validation parameters as set by previous archive
     */

    private static void resetValidationParameters ()
    {
        ArchiveBean archiveBean = BeanUtilities.getArchiveBean ();
        archiveBean.setHasJbiXml (true);
        archiveBean.setZipFileReadError (false);
        archiveBean.setFileReadError (false);
    }
}


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

package com.sun.enterprise.cli.commands;

import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.enterprise.diagnostics.DiagnosticAgent;
import com.sun.enterprise.admin.common.JMXFileTransfer;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.FileNotFoundException;


/**
 *  This command will get the version of the application server
 *  @version  $Revision: 1.11 $ 
 */
public class GenerateReportCommand extends BaseLifeCycleCommand
{
    private static final String LOG_START_DATE_OPTION = "logstartdate";
    private static final String LOG_END_DATE_OPTION = "logenddate";
    private static final String LOCAL_OPTION = "local";
    private static final String OUTPUT_FILE_OPTION = "outputfile";
    private static final String FILE_OPTION = "file";
    private static final String BUGIDS_OPTION = "bugids";
    private static final String TARGET_DIR_OPTION = "targetdir";
    private static final String TARGET ="target";
    private final String CONF_ATTR_MSG = 
                                getLocalizedString("ConfidentialAttrMsg");
    private final String INSTRUCTIONS = 
                                getLocalizedString("ConfidentialAttrInstructions");  
    private final String PROMPT_STRING = 
                                getLocalizedString("PromptToContinue");
    private final String YES_STRING = getLocalizedString("YesCharacter");
    private final String NO_STRING = getLocalizedString("NoCharacter");
    
    private String sOutputFile;
    
    /**
     *  An abstract method that validates the options
     *  on the specification in the xml properties file
     *  This method verifies for the correctness of number of
     *  operands and if all the required options are supplied by the client.
     *  @return boolean returns true if success else returns false
     */
    public boolean validateOptions() throws CommandValidationException
    {
        if (!super.validateOptions()) 
            return false;
        if (getBooleanOption(LOCAL_OPTION) == true)
        {
            if (getOption(TARGET_DIR_OPTION) == null)
                throw new CommandValidationException(
                                getLocalizedString("OptionRequiredInLocalMode",
                                            new Object[] {TARGET_DIR_OPTION}));
        }
        if ((getOption(LOG_END_DATE_OPTION) != null) && 
                (getOption(LOG_START_DATE_OPTION) == null))
        {
            throw new CommandValidationException(
                                getLocalizedString("OptionRequiredWithOption",
                                        new Object[] {LOG_START_DATE_OPTION,
                                                      LOG_END_DATE_OPTION}));
        }
        sOutputFile = getOption(OUTPUT_FILE_OPTION);
        return true;
    }

    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        //if validateOptions is false, then it must  be that --help option
        //is provided and there is no need to execute the command since
        //either manpage or usage text is displayed
        if (!validateOptions())
            return;
        
        try {
            if (!isDateValid(getOption(LOG_START_DATE_OPTION)) || 
                !isDateValid(getOption(LOG_END_DATE_OPTION)) ||
                !compareDates(getOption(LOG_START_DATE_OPTION),
                                getOption(LOG_END_DATE_OPTION)) ||
                !isFileValid(getOption(FILE_OPTION)) || 
                !isOutputFileValid(sOutputFile) ||
                !isDirectoryValid(getOption(TARGET_DIR_OPTION)))
            return;
            
            if (getBooleanOption(LOCAL_OPTION)) {
                executeCommandLocalMode();
            }
                //Start the code for remote version of the command
                //
            else {
                executeCommandRemoteMode();
            }
        
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                       "CommandSuccessful",
                                                       new Object[] {name}));
        }
        catch(Exception e) {
            if (e.getLocalizedMessage() != null)
                displayExceptionMessage(e);
            throw new CommandException(getLocalizedString("CommandUnSuccessful",
                                                     new Object[] {name} ), e);
        }
     }


        /**
         *  This routine will generate the diagnostic report remotely.
         **/
    private void executeCommandRemoteMode() throws Exception
    {
        final String objectName = getObjectName();        
        //use http connector
        MBeanServerConnection mbsc = getMBeanServerConnection(getHost(),
                                                              getPort(),
                                                              getUser(),
                                                              getPassword());

        List<String> confidentialAttrs = (List)mbsc.getAttribute(new ObjectName(objectName),
                                                                 "ConfidentialProperties");
        printAttributes(confidentialAttrs);
        final String input = printPromptToContinue();
        if (input.equalsIgnoreCase(YES_STRING))
            CLILogger.getInstance().printDebugMessage("continue");
        else
        {
            CLILogger.getInstance().printDebugMessage("exiting");
            return;
        }
        final String operationName = getOperationName();
        final Object[] params = new Object[] {createCLIOptionsMap()};
        final String[] types = getTypesInfo();

        final String sServerLocation = (String)mbsc.invoke(new ObjectName(objectName), 
                                                 operationName, params, types);

        final File fOutputFile = new File(sOutputFile);
        getDiagnosticReport(mbsc, sServerLocation,
                            fOutputFile.getAbsoluteFile().getParent(),
                            fOutputFile.getName());
    }
    

    
    /**
     *  retrieve diagnostic report
     */
    private void getDiagnosticReport(MBeanServerConnection mbsc,
                                       String sFileDownload,
                                       String sDownloadPath, String sFileName)
        throws CommandException
    {
        try 
        {
            final String fileName = new JMXFileTransfer(mbsc).downloadFile(sFileDownload,
                                                                           sDownloadPath,
                                                                           sFileName);
            CLILogger.getInstance().printDebugMessage("downloaded from  : " + sFileDownload + " to : " + sDownloadPath);
        }
        catch (Exception e)
        {
            Throwable t = e.getCause();
            while(t!=null && t.getCause()!=null)
                t=t.getCause();
            if(t==null)
                t=e;
            throw new CommandException(t.getLocalizedMessage(),t);
        }
    }


    private void checkDiagnosticFile(final String sDownloadPath) throws Exception
    {
        final File dlDir = new File(sDownloadPath);
        if (!dlDir.exists() )
        {
            dlDir.mkdirs();
        }
        if(!dlDir.exists() || !dlDir.canWrite() || !dlDir.isDirectory() ) {
            throw new CommandValidationException(getLocalizedString(
						 "InvalidDirectory",new Object [] {sDownloadPath}));
        }
    }

    
        /**
         *  This routine will generate the diagnostic report locally.
         **/
    private void executeCommandLocalMode() throws CommandException
    {
            final DiagnosticAgent agent = getFeatureFactory().getDiagnosticAgent();
            final List<String> confidentialAttrs = getConfidentialAttributes(
                getOption(TARGET_DIR_OPTION), 
                (String)operands.firstElement(), agent);
            printAttributes(confidentialAttrs);
            
            final String input = printPromptToContinue();
            
            if (input.equalsIgnoreCase("y"))
                CLILogger.getInstance().printDebugMessage("continue");
            else
            {
                CLILogger.getInstance().printDebugMessage("exiting");
                return;
            }
      
            final String reportFile = generateReport(createCLIOptionsMap(), agent);
            CLILogger.getInstance().printMessage(
                                getLocalizedString("ReportFile",
                                                new Object[] {reportFile}));
    }
    

    /**
     *  This method gets the Version locally
     */
    private boolean isDateValid(String dateStr) throws CommandException
    {
        if (dateStr == null) return true;
        try
        {
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Date date = df.parse(dateStr);
            return true;
        }
        catch (ParseException pe)
        {
            throw new CommandException(
                                getLocalizedString("InvalidDate",
                                                new Object[] {dateStr}));
        }
    }
   

    private boolean compareDates(String startDateStr, String endDateStr) 
            throws CommandException
    {
        if ((startDateStr == null) || (endDateStr == null)) return true;
        try
        {
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Date startDate = df.parse(startDateStr);
            Date endDate = df.parse(endDateStr);
            if (startDate.compareTo(endDate) > 0)
            {
                throw new CommandException(
                                getLocalizedString("InvalidDateBeforeDate"));
            }
        }
        catch (ParseException pe)
        {
            throw new CommandException(
                                getLocalizedString("InvalidDateError"));
        }
        return true;
    }
    
    
    private boolean isFileValid(String fileStr) throws CommandException
    {
        if (fileStr == null) return true;
        final File file = checkForFileExistence(null, fileStr);
        if (file == null) 
            throw new CommandException(getLocalizedString("FileDoesNotExist",
                                                      new Object[] {fileStr}));
        return true;
    }

    
    private boolean isDirectoryValid(String directory) throws CommandException
    {
        if (directory == null) return true;
        File file = new File(directory);
        if (!file.isDirectory() || !file.canRead()) 
            throw new CommandException(getLocalizedString("InvalidDirectory",
                                                new Object[]{directory}));
        return true;
    }

    
    private boolean isOutputFileValid(String sFile) throws Exception
    {
        if (sFile == null) return false;
        final File file = new File(sFile);
        final String sFileName = file.getName();
        
        if (file.getParent()!=null) {
            checkDiagnosticFile(file.getParent());
        }

        if (!sFileName.endsWith(".jar"))
            throw new CommandException(getLocalizedString("InvalidOutputFile"));
        return true;
    }

    
    private String generateReport(Map clioptions, DiagnosticAgent agent) throws CommandException  
    {
        
        try {
            return agent.generateReport(clioptions);
        } catch (DiagnosticException de) {
            de.printStackTrace();
            throw new CommandException(de.getMessage());
        }
         
        //return null;
    }
    
    
    private List<String> getConfidentialAttributes(String targetDir,
            String targetName, DiagnosticAgent agent) throws CommandException 
    {
        
        try {
            return agent.getConfidentialProperties(targetDir + 
                    File.separator + targetName);
        } catch(DiagnosticException e) {
            throw new CommandException(e.getMessage());
        }
         
        //return null;
        
    }
    
    
    private Map createCLIOptionsMap() 
    {
        Map cliOptions = new HashMap();
        String target = (String)operands.firstElement();
        CLILogger.getInstance().printDebugMessage(" Target : " +  target);
        cliOptions.put(LOCAL_OPTION, getOption(LOCAL_OPTION));
        cliOptions.put(TARGET, target);
        if (getOption(LOG_START_DATE_OPTION) != null)
            cliOptions.put(LOG_START_DATE_OPTION, 
                            new Date(getOption(LOG_START_DATE_OPTION)));
        if (getOption(LOG_END_DATE_OPTION) != null)
            cliOptions.put(LOG_END_DATE_OPTION, 
                            new Date(getOption(LOG_END_DATE_OPTION)));
        if (getOption(FILE_OPTION) != null)
            cliOptions.put(FILE_OPTION, getOption(FILE_OPTION));
        if (sOutputFile != null && getBooleanOption(LOCAL_OPTION))
            cliOptions.put(OUTPUT_FILE_OPTION, sOutputFile);
        cliOptions.put(TARGET_DIR_OPTION, getOption(TARGET_DIR_OPTION));
        if (getOption(BUGIDS_OPTION) != null)
            cliOptions.put(BUGIDS_OPTION, getOption(BUGIDS_OPTION));

        return cliOptions;
    }
    
    
    private void printAttributes(List<String> attrs) 
    {
        if(attrs != null) 
        {
            CLILogger.getInstance().printMessage(CONF_ATTR_MSG);
            
            Iterator<String> iterator = attrs.iterator();
            while(iterator.hasNext()) 
            {
                CLILogger.getInstance().printMessage(iterator.next());
            }
        }
    }

    
    private String printPromptToContinue() throws CommandException
    {
        try 
        {
            String line = null;
            while (!isValidInput(line))
            {
                CLILogger.getInstance().printMessage(INSTRUCTIONS);
                InputsAndOutputs.getInstance().getUserOutput().print(PROMPT_STRING);
                line = InputsAndOutputs.getInstance().getUserInput().getLine().trim();
            }
            return line;
        }
        catch (IOException ioe)
        {
            throw new CommandException(getLocalizedString("CouldNotPrintOrRead"), 
                                       ioe);
        }
    }
    
    
    private boolean isValidInput(final String line)
    {
        if ( line == null )
        {
            return false;
        }
        if ( line == null ||
                line.trim().equals("") ||
                line.length() < 1 ||
                (!line.equalsIgnoreCase(YES_STRING) && 
                    !line.equalsIgnoreCase(NO_STRING)))
            return false;
        return true;
    }
}

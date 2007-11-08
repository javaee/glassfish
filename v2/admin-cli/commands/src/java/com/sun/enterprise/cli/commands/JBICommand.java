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

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.cli.framework.CLIDescriptorsReader;
import com.sun.enterprise.cli.framework.ValidCommand;
//import com.sun.enterprise.cli.framework.*;

import com.sun.jbi.ui.client.JBIAdminCommandsClientFactory;
import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jbi.ui.common.JBIRemoteException;
import com.sun.jbi.ui.common.JBIManagementMessage;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import com.sun.jbi.ui.common.ServiceUnitInfo;

import javax.management.MBeanServerConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Vector;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;


/**
 *  Will start the JBI component on the specified target.
 *  @version  $Revision: 1.12 $
 */
public abstract class JBICommand extends S1ASCommand
{
    static final String VERBOSE                = "verbose";
    static final String TERSE                  = "terse";
    static final String TARGET_OPTION          = "target";
    static final String LIBRARY_NAME_OPTION    = "libraryname";
    static final String ASSEMBLY_NAME_OPTION   = "assemblyname";
    static final String COMPONENT_NAME_OPTION  = "componentname";
    static final String UPLOAD_OPTION          = "upload";
    static final String ENABLED_OPTION         = "enabled";
    static final String LIFECYCLE_STATE_OPTION = "lifecyclestate";
    static final String FORCE_OPTION           = "force";
    static final String KEEP_ARCHIVE_OPTION    = "keeparchive";
    static final String COMPONENT_PROPERTIES   = "properties";
    
    static String[] validStates = {"started","stopped","shutdown"};

    JBIAdminCommands mJbiAdminCommands = null;
    MBeanServerConnection mbsc = null;

    /**
     *  An abstract method that validates the options 
     *  on the specification in the xml properties file
     *  This method verifies for the correctness of number of 
     *  operands and if all the required options are supplied by the client.
     *  @return boolean returns true if success else returns false
     *  @throws CommandValidationException
     */
    public boolean validateOptions() throws CommandValidationException
    {
    	return super.validateOptions();
    }


    /**
     * Method that will return the option value or if no option value
     * was specified, the default value will be returned.
     * @param optionName The option name use to retrieve the value
     * @param validOptions An array containing a list of valid options
     * @param defaultValue The default value returned if no option value exists
     * @return The option value
     * @throws CommandValidationException
     */
    protected String getOption(String optionName, String[] validOptions, String defaultValue) throws CommandValidationException
    {
        boolean found = false;
        String option = getOption(optionName);
        if (option == null)
        {
            option = defaultValue;
            found = true;
        }
        else
        {
            for (int i=0; i<validOptions.length; i++)
            {
                if (option.equalsIgnoreCase(validOptions[i]))
                {
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            return option;
        }
        else {
            throw new CommandValidationException(getLocalizedString("InvalidValueInOption",
                                                    new Object[]{optionName}));
        }
    }

    /**
     * Method that will return the option value or if no option value
     * was specified, the default value will be returned.
     * @param optionName The option name use to retrieve the value
     * @param validOptions An array containing a list of valid options
     * @return The option value
     * @throws CommandValidationException
     */
    protected String getOption(String optionName, String[] validOptions) throws CommandValidationException
    {
        String defaultValue = null;
        return getOption(optionName, validOptions, defaultValue);
    }


    /**
     * Method that will return the option value or if no option value
     * was specified, the default value will be returned.
     * @param optionName The option name use to retrieve the value
     * @param defaultValue The default value returned if no option value exists
     * @return The option value
     */
 //   protected boolean getBooleanOption(String optionName, boolean defaultValue)
 //   {
 //       return super.getOption(optionName)==null?defaultValue:getBooleanOption(optionName);
 //   }

    /**
     * Debug Method - For testing only
     * Method that will return the usage values that are specified in the
     * usage text string.
     * @return An ArrayList containing the usage options
     */
    /*
    public ArrayList getUsageValues ()
    {
        String usageText = getUsageText();
        ArrayList list = new ArrayList();
        int indexStart = usageText.indexOf("--",0);
        while (indexStart != -1)
        {
            int indexEnd = usageText.indexOf(" ",indexStart);
            int indexEnd2 = usageText.indexOf("=",indexStart);
            if (indexEnd2 != -1)
            {
                indexEnd = indexEnd2;
            }
            list.add(usageText.substring(indexStart+2,indexEnd));
            indexStart = usageText.indexOf("--",indexEnd+1);
        }
        return list;
    }
    */

    /**
     * Debug Method - For testing only
     * Method that will validate the usage text in the xml file
     */
    /*
    public void validateUsageText() throws CommandValidationException
    {
        // Validate that the name in the usage text is correct
        String usageText = getUsageText();
        int i = usageText.indexOf(name);
        if ((usageText.indexOf(name)) == -1)
        {
            throw new CommandValidationException("Name in \"usage-text\" is NOT " + name);
        }
        int nextIndex = i + name.length();
        String space = usageText.substring(nextIndex,nextIndex+1);
        if (!(space.equals(" ")))
        {
            throw new CommandValidationException("Name in \"usage-text\" is NOT " + name);
        }

        Map options = getOptions();
        ArrayList list = getUsageValues();
        if ((list != null) && (options != null))
        {
            // Make sure each option defined in the <ValidOption> tag has an entry
            // in the usage-text string.
            for (Iterator iter = options.entrySet().iterator(); iter.hasNext();)
            { 
                Map.Entry entry = (Map.Entry)iter.next();
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();
                if (!(list.contains(key)))
                {
                    // Throw  Exception for jUnit Tests
                    throw new CommandValidationException("Usage is missing the option: " + key);
                }
            }

            // Now make sure each entry in the usage-text string has a corresponding
            // <ValidOption> tag.
            ValidCommand validCommand = null;
            CLIDescriptorsReader cliDescriptorsReader = CLIDescriptorsReader.getInstance();
            validCommand = cliDescriptorsReader.getCommand(name);
            for (Iterator iter = list.iterator(); iter.hasNext();)
            { 
                String value = (String)iter.next();
                if (!(validCommand.hasValidOption(value)))
                {
                    throw new CommandValidationException("Option \"" + value + "\" specified in the usage statement is not a valid option.");
                }
            }
        }
    }
    */


    /**
     * Method that will validate that the given file path exists and is not
     * a directory.
     * @param filePath The file path
     * @throws CommandException
     * @return String the absolute file path
     */
    protected String validateFilePath (String filePath) throws CommandException
    {
        return validateFilePath ("FileDoesNotExist",filePath);
    }


    /**
     * Method that will validate that the given file path exists and is not
     * a directory.
     * @param errorKey The property key value used to retrieve the error message text
     * @param filePath The file path
     * @return String the absolute file path
     * @throws CommandException
     */
    protected String validateFilePath (String errorKey, String filePath) throws CommandException
    {
        File file = new File(filePath);
        String absolutePath = file.getAbsolutePath();
        if ((!file.exists()) || (file.isDirectory()))
        {
            throw new CommandException(getLocalizedString(errorKey,
                                                    new Object[]{filePath}));
        }
        return absolutePath;
    }


    /**
     * Perform the pre run initialization.  This will validate the options as
     * well as retrieve the MBean Server Connection.
     * @throws CommandValidationException
     * @throws CommandException
     * @throws JBIRemoteException
     */
    protected boolean preRunInit() throws CommandValidationException, 
                                          CommandException, 
                                          JBIRemoteException
    {
        boolean uploadFlag = false;
        return preRunInit(uploadFlag);
    }


    /**
     * Perform the pre run initialization.  This will validate the options as
     * well as retrieve the MBean Server Connection.
     * @param uploadFlag The upload flag
     * @throws CommandValidationException
     * @throws CommandException
     * @throws JBIRemoteException
     */
    protected boolean preRunInit(boolean uploadFlag) throws CommandValidationException, 
                                                            CommandException, 
                                                            JBIRemoteException
    {
        // Validate the options and opeands
        validateOptions();

        // For Testing only -- Will remove later --
        //validateUsageText();

        // Retrieve the MBean Server Connection and the JBIAdminCommands object.
        mbsc = getMBeanServerConnection(getHost(), 
                                        getPort(), 
                                        getUser(), 
                                        getPassword());
        
        // Retrieve the JBI Admin Command object
        try {
            mJbiAdminCommands = JBIAdminCommandsClientFactory.getInstance(mbsc,uploadFlag);
        }
        catch (Exception e) {
            displayExceptionMessage(e);
        }

        // Make sure we have a valid command object
        if (mJbiAdminCommands == null)
        {
            throw new CommandException(getLocalizedString("CouldNotInvokeCommand",
                                                    new Object[]{name}));
        }
        return true;
    }


    /**
     * Will process the list results for the components (Service Engines, 
     * Binding Components and Shared Libraries).
     * was specified, the default value will be returned.
     * @param result The result xml string
     */
    protected void processJBIAdminComponentListResult (String result) 
    {
        List list = JBIComponentInfo.readFromXmlText(result);
        if (list.size() == 0)
        {
            CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("NoElementsToList",new Object[] {result}));
        }
        else
        {
            Iterator it = list.iterator();
            String listBreak = "";
            int count = 0;
            while (it.hasNext())
            {
                JBIComponentInfo info = ((JBIComponentInfo)it.next());
                String componentName = info.getName();
                CLILogger.getInstance().printDetailMessage (componentName);

                // TBD append global state info to end of name
                //String componentName = info.getName();
                //String state = info.getState();
                //String outputString = componentName + " : " + state;
                //CLILogger.getInstance().printDetailMessage (outputString);
            }
        }
    }


    /**
     * Will process the list results for the Service Assemblies
     * @param result The result xml string
     */
    protected void processJBIAdminAsseblyListResult (String result) 
    {
        List list = ServiceAssemblyInfo.readFromXmlTextWithProlog(result);
        if (list.size() == 0)
        {
            CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("NoElementsToList",new Object[] {result}));
        }
        else
        {
            Iterator it = list.iterator();
            String listBreak = "";
            int count = 0;
            while (it.hasNext())
            {
                ServiceAssemblyInfo info = ((ServiceAssemblyInfo)it.next());
                String assemblyName = info.getName();
                CLILogger.getInstance().printDetailMessage (assemblyName);

                // TBD append global state info to end of name
                //String assemblyName = info.getName();
                //String state = info.getState();
                //String outputString = assemblyName + " : " + state;
                //CLILogger.getInstance().printDetailMessage (outputString);
            }
        }
    }


    /**
     * Will format and display the component (binding or engine) show results.
     * @param result the xml string containing the result information
     * @param aName the name of the component
     */
    protected void processJBIAdminShowComponentResult (String result, String aName) 
    {
        List list = JBIComponentInfo.readFromXmlText(result);
        if (list.size() == 0)
        {
            CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("JBINoComponentToShow",new Object[] {aName}));
        }
        else
        {
            CLILogger.getInstance().printDetailMessage ("");
            String header = getLocalizedString ("JBIComponentShowHeader");
            CLILogger.getInstance().printDetailMessage (header);
            CLILogger.getInstance().printDetailMessage (createFillString('-',header.length()));

            Iterator it = list.iterator();
            JBIComponentInfo info = ((JBIComponentInfo)it.next());
            String componentName = info.getName();
            String componentState = info.getState();
            String componentDescription = info.getDescription();
            String formattedDescription = formatDescription(componentDescription,50,14);
            CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("JBIComponentName",new Object[] {componentName}));
            CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("JBIComponentState",new Object[] {componentState}));
            CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("JBIComponentDescription",new Object[] {formattedDescription}));
       }
    }


    /**
     * Will format and display the Shared Library show results.
     * @param result the xml string containing the result information
     * @param aName the name of the shared library
     */
    protected void processJBIAdminShowLibraryResult (String result, String aName) 
    {
        List list = JBIComponentInfo.readFromXmlText(result);
        if (list.size() == 0)
        {
            CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("JBINoLibraryToShow",new Object[] {aName}));
        }
        else
        {
            CLILogger.getInstance().printDetailMessage ("");
            String header = getLocalizedString ("JBISharedLibraryShowHeader");
            CLILogger.getInstance().printDetailMessage (header);
            CLILogger.getInstance().printDetailMessage (createFillString('-',header.length()));

            Iterator it = list.iterator();
            JBIComponentInfo info = ((JBIComponentInfo)it.next());
            String libraryName = info.getName();
            String libraryDescription = info.getDescription();
            String formattedDescription = formatDescription(libraryDescription,50,14);
            CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("JBISharedLibraryName",new Object[] {libraryName}));
            CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("JBISharedLibraryDescription",new Object[] {formattedDescription}));
        }
    }


    /**
     * Will format and display the Service Assembly show results.
     * @param result the xml string containing the result information
     * @param aName the name of the service assembly
     */
    protected void processJBIAdminShowAssemblyResult (String result, String aName) 
    {
        List list = ServiceAssemblyInfo.readFromXmlTextWithProlog(result);
        
        if ( list.size() <= 0 )
        {
            CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("JBINoServiceAssemblyToShow",new Object[] {aName}));
        }
        else
        {
           Iterator itr = list.iterator();
           ServiceAssemblyInfo saInfo = (ServiceAssemblyInfo) itr.next();
           List suInfoList = saInfo.getServiceUnitInfoList();

           String saName = saInfo.getName();
           String saState = saInfo.getState();
           String saDescription = saInfo.getDescription();
           String saSize = Integer.toString(suInfoList.size());
           String formattedSADescription = formatDescription(saDescription,50,16);
           
           CLILogger.getInstance().printDetailMessage ("");
           String SAHeader = getLocalizedString ("JBIServiceAssemblyShowHeader");
           CLILogger.getInstance().printDetailMessage (SAHeader);
           CLILogger.getInstance().printDetailMessage (createFillString('-',SAHeader.length()));
           CLILogger.getInstance().printDetailMessage (
                   getLocalizedString ("JBIServiceAssemblyName",new Object[] {saName}));
           CLILogger.getInstance().printDetailMessage (
                   getLocalizedString ("JBIServiceAssemblyState",new Object[] {saState}));
           CLILogger.getInstance().printDetailMessage (
                   getLocalizedString ("JBIServiceAssemblyServiceUnits",new Object[] {saSize}));
           CLILogger.getInstance().printDetailMessage (
                   getLocalizedString ("JBIServiceAssemblyDescription",new Object[] {formattedSADescription}));

           String indentString = "    ";
           CLILogger.getInstance().printDetailMessage ("");
           String SUHeader = getLocalizedString ("JBIServiceUnitShowHeader");
           CLILogger.getInstance().printDetailMessage (indentString + SUHeader);
           CLILogger.getInstance().printDetailMessage (indentString + createFillString('-',SUHeader.length()));
           boolean firstTime = true;
           for (Iterator suItr = suInfoList.iterator(); suItr.hasNext();)
           {
               ServiceUnitInfo suInfo = (ServiceUnitInfo ) suItr.next();
               String suState = suInfo.getState();
               String suDepoyedOn = suInfo.getDeployedOn();
               String suName = suInfo.getName();
               String suDescription = suInfo.getDescription();
               String formattedSUDescription = formatDescription(suDescription,50,18);
               if (!(firstTime))
               {
                   CLILogger.getInstance().printDetailMessage ("");
               }
               CLILogger.getInstance().printDetailMessage (indentString +
                       getLocalizedString ("JBIServiceUnitName",new Object[] {suName}));
               CLILogger.getInstance().printDetailMessage (indentString +
                       getLocalizedString ("JBIServiceUnitState",new Object[] {suState}));
               CLILogger.getInstance().printDetailMessage (indentString +
                       getLocalizedString ("JBIServiceUnitDeployedOn",new Object[] {suDepoyedOn}));
               CLILogger.getInstance().printDetailMessage (indentString +
                       getLocalizedString ("JBIServiceUnitDescription",new Object[] {formattedSUDescription}));
               firstTime = false;
           }
        }
    }


    /**
     *   ** Still Under Development **
     * Will process the list results for the Service Assemblies
     * @param result The result xml string
     */
    protected void processJBIAdminResult (String result, String successKey) throws CommandException
    {
        JBIManagementMessage mgmtMsg =
            JBIManagementMessage.createJBIManagementMessage(result);
        if (mgmtMsg == null)
        {
            CLILogger.getInstance().printDetailMessage (
                    getLocalizedString (successKey, new Object[] {result}));
        }
        else
        {
            if (mgmtMsg.isSuccessMsg())
            {
                String msg = mgmtMsg.getMessage();
                int index = msg.indexOf(")");
                if (index != -1)
                {
                    msg = msg.substring(index+1);
                }
                CLILogger.getInstance().printDetailMessage (msg);
            }
            else
            {
                String msg = mgmtMsg.getMessage();
                throw new CommandException(msg);
            }
        }
    }


    /**
     * Will process the task exception to display the error message.
     * @param ex the exception to process
     * @throws CommandException
     */
    protected void processTaskException (Exception ex) throws CommandException
    {
        JBIManagementMessage mgmtMsg = extractJBIManagementMessage(ex);
        if (mgmtMsg == null)
        {
            displayExceptionMessage(ex);
        }
        else
        {
            // Display the exception message which was returned from the runtime.
            String msg = mgmtMsg.getMessage();
            CLILogger.getInstance().printDetailMessage (msg);
            
            // Display the CLI success or failure message.
            if (msg.trim().startsWith("WARNING")) {
                CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("CommandSuccessful",new Object[] {name}));
            }
            else {
                CLILogger.getInstance().printDetailMessage (
                    getLocalizedString ("CommandUnSuccessful",new Object[] {name}));
            }
        }
    }


    /**
     * Will extract the JBIManagementMessgae from the Remote exception.
     * @param ex the exception to process
     */
    protected JBIManagementMessage extractJBIManagementMessage (Exception ex )
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
        return mgmtMsg;
    }


    /**
     * Will create a string of the size specified filled with the fillChar.
     * @param fillChar the character to create the string with
     * @param the size of the string
     */
    private String createFillString (char fillChar, int size)
    {
        String fillString = "";
        for (int i=0; i<size; i++)
        {
            fillString += fillChar;
        }
        return fillString;
    }


    /**
     * Will format the description text that is displayed in the show commands.  The
     * formatting will consist of removing all new lines and extra white space, then
     * adding back in line breaks at the first avaliable location that is less then
     * or equal to the given max line length.
     * @param description the description text to format
     * @param maxLength the maximum line length size.
     * @param indentAmout the amount to indent row 2 - n
     */
    private String formatDescription (String description, int maxLength, int indentAmount)
    {

        // Strip out the leading white spaces in each row, and remove any "\n"
        int endIndex = 0;
        int startIndex = 0;
        String finalString = "";
        String rowString = "";
        String space = "";
        endIndex = description.indexOf("\n",startIndex);
        while (endIndex != -1)
        {   
            rowString = description.substring(startIndex,endIndex).trim();
            finalString += space + rowString;
            startIndex = endIndex + 1;
            endIndex = description.indexOf("\n",startIndex);
            space = " ";
        }
        rowString = description.substring(startIndex).trim();
        finalString += space + rowString;

        // Format the string by adding the line breaks in the correct location and adding
        // the indention amount at that beginning of each row.
        endIndex = 0;
        startIndex = 0;
        int spaceIndex = 0;
        int indentSize = 0;
        String newString = "";
        boolean done = false;
        int totalLength = finalString.length();
        while (!(done))
        {
            endIndex = ((startIndex + maxLength) > totalLength) ? totalLength : (startIndex + maxLength);
            rowString = finalString.substring(startIndex,endIndex);
            spaceIndex = startIndex + rowString.lastIndexOf(" ");
            if (endIndex < totalLength)
            {
                spaceIndex = startIndex + rowString.lastIndexOf(" ");
                if (spaceIndex != -1)
                {
                    endIndex = spaceIndex;
                }
            }
            rowString = finalString.substring(startIndex,endIndex) + "\n";
            startIndex = endIndex + 1;
            newString += createFillString(' ',indentSize) + rowString;
            indentSize = indentAmount;
            if (startIndex >= totalLength)
            {
                done = true;
            }
        }
        finalString = newString.trim();
        return finalString;
    }

}

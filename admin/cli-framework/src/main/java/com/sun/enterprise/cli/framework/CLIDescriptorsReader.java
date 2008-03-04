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
 * CLIDescriptorsReader.java
 *
 * Created on June 9, 2003, 12:19 PM
 */
package com.sun.enterprise.cli.framework;

//imports
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;

//XML imports
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author  pa100654
 */
public final class CLIDescriptorsReader 
{
    public static final int DONT_SERIALIZE = 0;
    public static final int SERIALIZE_COMMANDS_TO_FILES = 1;
    public static final int SERIALIZE_COMMANDS_TO_FILE = 2;
    public static final String SERIALIZED_DESCRIPTOR_FILE = ".CLIDescriptors";
    public static final String SERIALIZED_COMMANDS_DEFAULT_DIR = "cli";
    public static final String ENVIRONMENT_PREFIX = "environment-prefix";
    public static final String ENVIRONMENT_FILENAME = "environment-filename";
    //static instance of itself
    private static CLIDescriptorsReader cliDescriptorsReader;
    
    // The directory where the serialized file has to be saved
    private String serializeDir = SERIALIZED_COMMANDS_DEFAULT_DIR;

    // Attribute which says whether the component descriptors and commands 
    // descriptors have to read from a file or loaded to a file.
    private int serializeDescriptors = DONT_SERIALIZE;
    
    // List of valid commands from all the components
    private ValidCommandsList commandsList = null;
    
    // List of valid options from all the components
    private HashMap<String, ValidOption> validOptions = null;

    // List of options to be replaced by all the commands in the descriptor files
    private HashMap<String, ValidOption> replaceOptions = null;

    // List of descriptors (URLs)
    private Vector<URL> descriptors = null;
    
    // Vector of Properties from all the descriptor files
    private Vector<Properties> properties = null;
    
    // Default command to use
    private String defaultCommand = null;

    // Help class name for the help command 
    private String helpClass = null;
    


    private static final String DESCRIPTOR_FILE_NAME = "CLIDescriptor.xml";
    /** 
        Construct CLIDescriptorsReader object 
    */
    private CLIDescriptorsReader()
    {
        initialize();
    }
    
    
    /** 
        Construct CLIDescriptorsReader object with the given arguments.
        @param descriptor, the URL for the CLI Descriptor file
    */
    private CLIDescriptorsReader( final URL descriptor)
    {
        setDescriptor(descriptor);
        initialize();
    }
    
    
    /** 
        Construct CLIDescriptorsReader object with the given arguments.
	@param descriptors, the URL's for the CLI Descriptor file
    */
    private CLIDescriptorsReader( final Vector<URL> descriptors) 
    {
        this.descriptors = descriptors;
        initialize();
    }
    
    
    /** 
     *Initialize the CLIDescriptorsReader object and read the components
     */
    private void initialize()
    {
        commandsList = new ValidCommandsList();
        validOptions = new HashMap<String, ValidOption>();
        replaceOptions = new HashMap<String, ValidOption>();
        properties = new Vector<Properties>();
    }


    /**
     * returns the instance of the CLIDescriptorsReader
     */
    public static synchronized CLIDescriptorsReader getInstance()
    {
        if (cliDescriptorsReader == null)
        {
            cliDescriptorsReader= new CLIDescriptorsReader();
        }
        return cliDescriptorsReader;
    }

    
    /**
      Returns the command object.  if the commandName is null then the default
      command object is returned.
      @param  commandName the command name
      @return ValidCommand the valid command object
     */
    public ValidCommand getCommand(String commandName)
                           throws CommandValidationException
    {
        ValidCommand command = null;
        if ((commandsList == null) || (commandsList.size() == 0))
        {
            command = getCommandFromFileOrDescriptor(commandName);
        }
        else 
        {
            command = commandsList.getValidCommand(commandName);
        }
        return command;
    }


    /**
     *  returns the default command string
     *  @return defaultCommand
     */
    public String getDefaultCommand()
    {
	    return defaultCommand;
    }


    /**
     *  returns the help class string
     *  @return helpClass
     */
    public String getHelpClass()
    {
    	return helpClass;
    }

    /**
      Returns the commands list
      if not already read, the commands are read either from the serialized 
      command file(s) or the command descriptor(s)
      @return ValidCommandsList the list of valid commands
     */
    public ValidCommandsList getCommandsList() throws CommandValidationException
    {
        if ((commandsList == null) || (commandsList.size() == 0))
        {
            loadAllCommandsFromFilesOrDescriptors();
        }
        return commandsList;
    }

    
    /**
      Returns the properties list of all the commands module's
      @return Iterator the list of Properties
     */
    public Iterator<Properties> getProperties() throws CommandValidationException
    {
        return properties.iterator();
    }

    
    /**
      Returns the command object
      @param  commandName the command name
      @return ValidCommand the valid command object
     */
    private ValidCommand getCommandFromFileOrDescriptor(String commandName) 
                           throws CommandValidationException
    {
        ValidCommand command = null;
        
	/************************************************************************/
	/** how should we handle defaultcommand if using serialized descriptor **/
	/************************************************************************/
        if (serializeDescriptors == SERIALIZE_COMMANDS_TO_FILES)
        {
            command = getSerializedCommand(commandName);
            if (command != null)
            {
                return command;
            }
        }

            //preserve serializedDescriptor setting so commands do not get serialized
            //if command could not be found
        int saveSerializedDescriptor = getSerializeDescriptorsProperty();
        setSerializeDescriptorsProperty(DONT_SERIALIZE);
            //setSerializeDescriptorsProperty(SERIALIZE_COMMANDS_TO_FILE);
        loadAllCommandsFromFilesOrDescriptors();
            //put back setting for serizliedDescriptor
        setSerializeDescriptorsProperty(saveSerializedDescriptor);
        if (commandName != null)
            return commandsList.getValidCommand(commandName);
        else if (defaultCommand != null)
            return commandsList.getValidCommand(defaultCommand);
        else
            throw new CommandValidationException(
                LocalStringsManagerFactory.getFrameworkLocalStringsManager().
                getString("CommandNotSpecified", null));

    }

    
    /**
     * Loads the specified serialized command from the file in the 
        SERIALIZED_COMMANDS_DIR
     * @param commandFile file name of the command to be read
     * @return returns the ValidCommand object
     */
    private ValidCommand getSerializedCommand(String commandName)
                    throws CommandValidationException
    {

        ValidCommand command  = null;
        String encodedCommandName = null;
		try{
		    encodedCommandName = URLEncoder.encode(commandName,"UTF-8");
			}catch(UnsupportedEncodingException ue){
                encodedCommandName = commandName;
			}
        final InputStream in = CLIDescriptorsReader.class.getClassLoader().getResourceAsStream(serializeDir + "/." + encodedCommandName);
        
        if (in!=null)
        {
            command = getSerializedCommand(in);
        }
        else {
            return null;
        }
         
        /*
        File fileName = new File(serializeDir + "/." + commandName);
        if (fileName.exists())
        {
            command = getSerializedCommand(fileName);
        }
        */
        
        return command;
    }
 
    
    /**
     * Loads the specified serialized command from the file in the 
        SERIALIZED_COMMANDS_DIR
     * @param commandFile file name of the command to be read
     */
    private ValidCommand getSerializedCommand(InputStream commandFile)
                            throws CommandValidationException
    {
        try
        {
            ValidCommand command = null;
            ObjectInputStream in = new ObjectInputStream(commandFile);
            
                //ObjectInputStream in = new ObjectInputStream(
                //                        new FileInputStream(commandFile));
            
            /** read CommandProperties to serialized file **/
            defaultCommand = (String) in.readObject();
            helpClass = (String) in.readObject();
            properties = (Vector) in.readObject();

            command = (ValidCommand) in.readObject();
    	    CLILogger.getInstance().printDebugMessage("++++++++++++++++++++++++++++ Command loaded from file and it is " + command);
            return command;
        }
        catch (Exception e)
        {
	    throw new CommandValidationException(e);
        }
    }

    
    /** 
     *Initialize the commandsList based on the serializeDescriptors property
     */
    private void loadAllCommandsFromFilesOrDescriptors() 
                    throws CommandValidationException
    {
        if (serializeDescriptors == DONT_SERIALIZE)
        {
            readDescriptors();
        }
        else if (serializeDescriptors == SERIALIZE_COMMANDS_TO_FILES)
        {
                // currently not supported
            loadCommandsFromMultipleFiles();
        }
        else if (serializeDescriptors == SERIALIZE_COMMANDS_TO_FILE)
        {
            loadCommandsFromSingleFile();
        }
    }


    /**
     *
     */
    private void loadCommandsFromMultipleFiles() throws CommandValidationException
    {
            //currently not supported and may not be a feasible feature
            //since reading all the serialized commands is timing consuming.
            //so for now, read the descriptor file only.
        readDescriptors();
    }
   
    
    /**
     *
     */
    private void loadCommandsFromSingleFile()
                    throws CommandValidationException
    {
        final Vector<URL> serializedDescriptorFile = getSerializedDescriptorFile();

            //if there are no serialized descriptor file, then directly read 
            //and parse the CLIDescriptor.xml file
        if (serializedDescriptorFile.size() > 0) {
            loadSerializedDescriptorFile(serializedDescriptorFile);
        }
        else {
            readDescriptors();
        }
    }
    
    
    /**
        Parse and read the XML Descriptors file and populate it into 
        commandsList
     */
    public void readDescriptors() throws CommandValidationException
    {
        commandsList.removeAllCommands();
        //DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);   
        //factory.setNamespaceAware(true);
        try 
        {
            // setup schema processing
            DOMParser parser = new DOMParser();
            parser.setFeature( "http://apache.org/xml/features/dom/defer-node-expansion", true);
            parser.setFeature( "http://xml.org/sax/features/validation", true );
            parser.setFeature( "http://xml.org/sax/features/namespaces", true );
            parser.setFeature( "http://apache.org/xml/features/validation/schema", true );
            parser.setFeature ("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
            parser.setEntityResolver(new CLIEntityResolver());

            //factory.setValidating(true);
	        // CLILogger.getInstance().printDebugMessage("Validation against DTD is " + factory.isValidating());
            //DocumentBuilder builder = factory.newDocumentBuilder();
            if (descriptors == null)
            {
                getDescriptors();
                if (descriptors == null) 
		{
		    LocalStringsManager lsm = 
			LocalStringsManagerFactory.getFrameworkLocalStringsManager();
		    throw new CommandValidationException(lsm.getString("NoDescriptorsDefined"));
		}
            }
            for (int i = 0; i < descriptors.size(); i++)
            {
                CLILogger.getInstance().printDebugMessage(i + " descriptor = " + descriptors.get(i));
                
                URL descriptorURL = (URL) descriptors.get(i);
                //InputSource is = new InputSource(descriptorURL.toString());
                //Document document = builder.parse(is);
                parser.parse(new InputSource(descriptorURL.toString()));
                Document document = parser.getDocument();
                generateOptionsAndCommands(document);
            }
                //call this last after all descriptors have been read since we want to
                //replace all the options with the options defined in ReplaceOptions
                //element in the last XML in the list.
            replaceOptionsInCommandsList(replaceOptions);
            validateOptionsInCommand();
            
            if (serializeDescriptors == SERIALIZE_COMMANDS_TO_FILES)
            {
                saveCommandsAsMultipleFiles();
            }
            else if (serializeDescriptors == SERIALIZE_COMMANDS_TO_FILE)
            {
                saveCommandsAsSingleFile();
            }

        } 
        catch (SAXException sxe) 
        {
           // Error generated during parsing)
            Exception  x = sxe;
            if (sxe.getException() != null)
               x = sxe.getException();
	    throw new CommandValidationException(x);
        }
        catch (IOException ioe) 
        {
            // I/O error
	    throw new CommandValidationException(ioe);
            //throw new CommandValidationException(ioe.getLocalizedMessage());
        }
    }
   
    
    /**
     *
     */
    private String[] getSerializedCommandsFileList(File commandsDir)
    {
        String[] commandFiles = commandsDir.list(new FilenameFilter() {
            public boolean accept(File parentDir, String name) {
                return (name.startsWith("."));
            }});
        return commandFiles;
    }
    
    /**
     * Returns the Vector contains all the serialized descriptor files. 
     * @return Vector.  an empty vector is returned if there are no
     * serialized descriptor file.
     */
    private Vector<URL> getSerializedDescriptorFile() throws CommandValidationException
    {
        Vector<URL> descriptorFiles = new Vector<URL>();
        try
        {
            final String sDescriptorFile = serializeDir + "/" +
                                           SERIALIZED_DESCRIPTOR_FILE;
            
            Enumeration<URL> urls = CLIDescriptorsReader.class.getClassLoader().getResources(sDescriptorFile);
            if ((urls == null) || (!urls.hasMoreElements()))
            {
                //return an empty vector
                return descriptorFiles;
            }

            while (urls.hasMoreElements())
            {
                URL url = (URL) urls.nextElement();
                descriptorFiles.add(url);
            }
        }
        catch(IOException ioe)
        {
   	       LocalStringsManager lsm = 
		   LocalStringsManagerFactory.getFrameworkLocalStringsManager();
	       CLILogger.getInstance().printMessage(lsm.getString("CouldNotLoadDescriptor"));
        }
        return descriptorFiles;
    }
    
    
    /**
     * Loads the serialized commands from the files in the 
        SERIALIZED_COMMANDS_DIR
     * @return true if loaded succesfully else false
     */
    private boolean loadSerializedCommands()
                    throws CommandValidationException
    {
        File commandsDir = new File(serializeDir);
        String[] commandFiles = getSerializedCommandsFileList(commandsDir);

        if (!((commandsDir != null) && (commandsDir.exists()) && 
              (commandFiles.length > 0)))
        {
            return false;
        }
        
        boolean loaded = true;
        for (int i = 0; (i < commandFiles.length) && loaded; i++)
        {
            //File commandFile = new File(commandFiles[i]);
            final InputStream in = CLIDescriptorsReader.class.getClassLoader().getResourceAsStream(serializeDir + "/." + commandFiles[i] );

            try
            {
                ValidCommand command = getSerializedCommand(in);
                //ValidCommand command = getSerializedCommand(commandFile);
                if (command != null)
                {
                    commandsList.addCommand(command);
                }
                else
                {
                    loaded = false;
                }
            }
            catch (CommandValidationException e)
            {
                loaded = false;
                break;
            }
        }
        return loaded;
    }
    
    
    /**
     * Loads commands from serialized descriptor file
     */
    private void loadSerializedDescriptorFile(final Vector<URL> descriptorFile) throws CommandValidationException
    {
        try
        {
            for (int ii=0; ii<descriptorFile.size(); ii++) {
                final URL descriptorURL = (URL) descriptorFile.get(ii);
                ObjectInputStream ois = new ObjectInputStream(descriptorURL.openStream());
                
                defaultCommand = (String) ois.readObject();
                helpClass = (String) ois.readObject();
                properties = (Vector) ois.readObject();
                
                final ValidCommandsList validCommandsList = (ValidCommandsList)ois.readObject();
                    //if commandsList is not empty then need to load each
                    //command in commandsList.
                if (commandsList != null  && commandsList.size() > 0) {
                    final Iterator<ValidCommand> commands = validCommandsList.getCommands();
                    while (commands.hasNext()) {
                        commandsList.addCommand((ValidCommand)commands.next());
                    }
                }
                else {
                    commandsList = validCommandsList;
                }
                
                CLILogger.getInstance().printDebugMessage("++++++++++++++++++++++++++++ Commands loaded from file");
            }
        }
        catch(FileNotFoundException fnfe)
        {
            throw new CommandValidationException(fnfe);
        }
        catch(IOException ioe)
        {
            throw new CommandValidationException(ioe);
        }
        catch(ClassNotFoundException cnfe)
        {
            throw new CommandValidationException(cnfe);
        }
    }
    
    
    /**
        gets the descriptors 
     */
    private Vector getDescriptors() throws CommandValidationException
    {
        if (descriptors != null)
            return descriptors;
        
        try
        {
            String descriptor_file_name = System.getProperty("DESCRIPTOR_FILE");
            if(descriptor_file_name == null)
                descriptor_file_name = DESCRIPTOR_FILE_NAME;
            Enumeration urls = CLIDescriptorsReader.class.getClassLoader().getResources(
                descriptor_file_name);
            if ((urls == null) || (!urls.hasMoreElements()))
            {
                return descriptors;
            }

            while (urls.hasMoreElements())
            {
                URL url = (URL) urls.nextElement();
                setDescriptor(url);
            }
        }
        catch(IOException ioe)
        {
	    LocalStringsManager lsm = 
		LocalStringsManagerFactory.getFrameworkLocalStringsManager();
	    CLILogger.getInstance().printMessage(lsm.getString("CouldNotLoadDescriptor"));
        }
        return descriptors;
    }
   
    
    /**
        Sets the descriptor file
	@param descriptor the descriptor file containing the command specifications
     */
    public void setDescriptor(URL descriptor) 
    {
        if (descriptor != null)
        {
            if (descriptors == null)
            {
                descriptors = new Vector<URL>();
            }
            descriptors.add(descriptor);
            //reverse the order in the Vector so that the last CLIDescriptor.xml will be parsed first.
            java.util.Collections.reverse(descriptors);
        }
    }

    
    /**
        Sets the descriptor files (components)
	@param descriptors the descriptor files containing the command specifications
     */
    public void setDescriptors(Vector<URL> descriptors) 
    {
        this.descriptors = descriptors;
    }

    
    /**
        Get All the Valid, Required, Deprecated Options and the commands 
        from the descriptor file
     */
    private void generateOptionsAndCommands(Document document)
        throws CommandValidationException
    {
        if (document != null) 
        {
            for (Node nextKid = document.getDocumentElement().getFirstChild();
                    nextKid != null; nextKid = nextKid.getNextSibling()) 
            {
                String nodeName = nextKid.getNodeName();
                if (nodeName.equalsIgnoreCase("CommandProperties"))
                {
                    Properties props = new Properties();
                    for (Node grandKid = nextKid.getFirstChild();
                       grandKid != null; grandKid = grandKid.getNextSibling()) 
                    {
                        String grandKidNodeName = grandKid.getNodeName();
                        if (grandKidNodeName.equalsIgnoreCase("CommandProperty")) 
                        {
                            NamedNodeMap nodeMap = grandKid.getAttributes();
                            String nameAttr = 
                                    nodeMap.getNamedItem("name").getNodeValue();
                            String valueAttr = 
                                    nodeMap.getNamedItem("value").getNodeValue();
                            props.setProperty(nameAttr, valueAttr);
                        }
                    }
		    final NamedNodeMap commandPropertiesAttribute = nextKid.getAttributes();
		    if (commandPropertiesAttribute != null &&
			commandPropertiesAttribute.getNamedItem("defaultcommand") != null )
			defaultCommand = commandPropertiesAttribute.getNamedItem(
					 "defaultcommand").getNodeValue();
		    if (commandPropertiesAttribute != null &&
			commandPropertiesAttribute.getNamedItem("helpclass") != null )
			helpClass = commandPropertiesAttribute.getNamedItem(
					 "helpclass").getNodeValue();
                                      
                    properties.add(props);
                }
                else if (nodeName.equalsIgnoreCase("Options")) 
                {
                    for (Node grandKid = nextKid.getFirstChild();
                       grandKid != null; grandKid = grandKid.getNextSibling()) 
                    {
                        String grandKidNodeName = grandKid.getNodeName();
                        if (grandKidNodeName.equalsIgnoreCase("Option")) 
                        {
                            ValidOption option = generateOption(document, grandKid);
                            final String optionName =  option.getName();
                                //do not allow duplication option name defined
                            if (!validOptions.containsKey(optionName)) {
                                validOptions.put(optionName, option);
                            }
                            else {
                                LocalStringsManager lsm = 
                                                    LocalStringsManagerFactory.getFrameworkLocalStringsManager();
                                throw new CommandValidationException(lsm.getString("DuplicateOptionDeclaration", new Object[]{optionName}) );
                            }
                            
                        }
                        else if (grandKidNodeName.equalsIgnoreCase("ReplaceOption")) 
                        {
                            ValidOption option = generateOption(document, grandKid);
                            replaceOptions.put(option.getName(), option);
                        }

                    }
                }
                //Generates the Valid Commands List from the descriptor file by 
                // reading all the Valid Commands
                else if (nodeName.equalsIgnoreCase("Commands")) 
                {
                    for (Node grandKid = nextKid.getFirstChild();
                       grandKid != null; grandKid = grandKid.getNextSibling()) 
                    {
                        String grandKidNodeName = grandKid.getNodeName();
                        if (grandKidNodeName.equalsIgnoreCase("Command")) 
                        {
                            ValidCommand command = generateCommand(document, grandKid);
                            commandsList.addCommand(command);
                        }
                    }
                }
            }
        }
    }
   
    
    /**
     Generate a Valid Option by populating it with its name, value-required, 
     shortoption, default, help-text fields
     @param document the Document object
     @param grandKid the node in the xml document where the option need to be searched
     @return returns a ValidOption
     */
    private ValidOption generateOption(Document document, Node grandKid)
    {
        ValidOption option = new ValidOption();
        NamedNodeMap nodeMap = grandKid.getAttributes();
        String nameAttr = nodeMap.getNamedItem("name").getNodeValue();
        String typeAttr = nodeMap.getNamedItem("type").getNodeValue();
        boolean valueReqdAttr = Boolean.valueOf(nodeMap.getNamedItem("value-required").getNodeValue()).booleanValue();
        Node defaultAttrNode = nodeMap.getNamedItem("default");
        String defaultAttr = null;
        if (defaultAttrNode != null)
            defaultAttr = defaultAttrNode.getNodeValue();
        option.setName(nameAttr);
        option.setType(typeAttr);
        option.setRequired(valueReqdAttr?ValidOption.REQUIRED:ValidOption.OPTIONAL);
        option.setDefaultValue(defaultAttr);
        for (Node nextGrandKid = grandKid.getFirstChild(); nextGrandKid != null;
                nextGrandKid = nextGrandKid.getNextSibling()) 
        {
            String grandKidName = nextGrandKid.getNodeName();
            if (grandKidName.equalsIgnoreCase("shortoption"))
            {
                String shortOption = nextGrandKid.getFirstChild().getNodeValue();
                shortOption = shortOption.trim();
                if (shortOption != null)
                {
                    option.setShortName(shortOption);
                }
            }
        }
        return option;
    }
    
    
    /**
     Generate a Valid Command by populating it with its name, numberofoperands, 
     classname, validoptions, requiredoptions, usage-text and help-text fields
     @param document the Document object
     @param grandKid, the node in the xml document tree where the command need to be searched
     @return returns a ValidCommand
     */
    private ValidCommand generateCommand(Document document, Node grandKid)
        throws CommandValidationException
    {
        ValidCommand command = new ValidCommand();

        NamedNodeMap nodeMap = grandKid.getAttributes();
        String nameAttr = nodeMap.getNamedItem("name").getNodeValue();
        String classNameAttr = nodeMap.getNamedItem("classname").getNodeValue();
        String numberOfOperandsAttr = nodeMap.getNamedItem("numberofoperands").getNodeValue();

        String defaultOperandAttr = null;
        if (nodeMap.getNamedItem("defaultoperand") != null)
            defaultOperandAttr = nodeMap.getNamedItem("defaultoperand").getNodeValue();

        Node usageTextNode = nodeMap.getNamedItem("usage-text");
        String usageTextAttr = null;
        if (usageTextNode != null)
            usageTextAttr = usageTextNode.getNodeValue();
        command.setName(nameAttr);
        command.setNumberOfOperands(numberOfOperandsAttr);
        command.setDefaultOperand(defaultOperandAttr);
        command.setClassName(classNameAttr);
        command.setUsageText(usageTextAttr);

        for (Node nextGrandKid = grandKid.getFirstChild(); nextGrandKid != null;
                nextGrandKid = nextGrandKid.getNextSibling()) 
        {
            String grandKidName = nextGrandKid.getNodeName();
            LocalStringsManager lsm = LocalStringsManagerFactory.getFrameworkLocalStringsManager();
            if (grandKidName.equalsIgnoreCase("ValidOption"))
            {
                final NamedNodeMap validOptionNodeMap = 
                                nextGrandKid.getAttributes();
                final String validOption = 
                        validOptionNodeMap.getNamedItem("name").getNodeValue();
                if (command.hasValidOption(validOption)) {
                    throw new CommandValidationException(lsm.getString("OptionAlreadyDefined",
                                                                       new Object[] {validOption,
                                                                       command.getName()} ));
                }
                
                String deprecatedOption = null;
                if (validOptionNodeMap.getNamedItem("deprecatedoption") != null)
                {
                    deprecatedOption = validOptionNodeMap.getNamedItem("deprecatedoption").getNodeValue();
                }
                String defaultValue = null;
                if (validOptionNodeMap.getNamedItem("defaultvalue") != null) {
                    defaultValue = validOptionNodeMap.getNamedItem("defaultvalue").getNodeValue();
                }

                final ValidOption option = findOption(validOption);
                if (option == null) {
                    throw new CommandValidationException(lsm.getString("ValidOptionNotDefined",
                                                                       new Object[] {validOption} ));
                }
                ValidOption newOption = new ValidOption(option);
                if (deprecatedOption != null)
                    newOption.setDeprecatedOption(deprecatedOption);
                if (defaultValue != null)
                    newOption.setDefaultValue(defaultValue);
                
                command.addValidOption(newOption);
            }
            else if (grandKidName.equalsIgnoreCase("RequiredOption"))
            {
                final NamedNodeMap reqdOptionNodeMap = 
                                nextGrandKid.getAttributes();
                final String reqdOption = 
                        reqdOptionNodeMap.getNamedItem("name").getNodeValue();
                if (command.hasRequiredOption(reqdOption)) {
                    throw new CommandValidationException(lsm.getString("OptionAlreadyDefined",
                                                                       new Object[] {reqdOption,
                                                                       command.getName()} ));
                }
                
                String deprecatedOption = null;
                if (reqdOptionNodeMap.getNamedItem("deprecatedoption") != null)
                {
                    deprecatedOption = 
                        reqdOptionNodeMap.getNamedItem("deprecatedoption").getNodeValue();
                }
                final ValidOption option = findOption(reqdOption);
                if (option == null)
                {
                    throw new CommandValidationException(lsm.getString("RequiredOptionNotDefined",
                                                                       new Object[] {reqdOption} ));
                }
                if (deprecatedOption != null)
                    option.setDeprecatedOption(deprecatedOption);
                command.addRequiredOption(new ValidOption(option));
            }
            else if (grandKidName.equalsIgnoreCase("DeprecatedOption"))
            {
                final String deprecatedOption = nextGrandKid.getFirstChild().getNodeValue();
                if (command.hasDeprecatedOption(deprecatedOption)) {
                    throw new CommandValidationException(lsm.getString("OptionAlreadyDefined",
                                                                       new Object[] {deprecatedOption,
                                                                       command.getName()} ));
                }
                
                final ValidOption option = findOption(deprecatedOption);
                if (option == null)
                {
                    throw new CommandValidationException(lsm.getString("DeprecatedOptionNotDefined",
                                                                       new Object[] {deprecatedOption} ));
                }
                command.addDeprecatedOption(option);
            }
            else if (grandKidName.equalsIgnoreCase("properties"))
            {
                for (Node nextGreatGrandKid = nextGrandKid.getFirstChild(); 
                        nextGreatGrandKid != null;
                        nextGreatGrandKid = nextGreatGrandKid.getNextSibling()) 
                {
                    String greatGrandKidName = nextGreatGrandKid.getNodeName();
                    if (greatGrandKidName.equalsIgnoreCase("property"))
                    {
                        nodeMap = nextGreatGrandKid.getAttributes();
                        String propertyNameAttr = 
                                    nodeMap.getNamedItem("name").getNodeValue();

                        Vector<String> values = new Vector<String>(); 
                        for (Node nextGreatGreatGrandKid = nextGreatGrandKid.getFirstChild();
                                nextGreatGreatGrandKid != null;
                                nextGreatGreatGrandKid = nextGreatGreatGrandKid.getNextSibling()) 
                        {
                            String greatGreatGrandKidName = nextGreatGreatGrandKid.getNodeName();
                            if (greatGreatGrandKidName.equalsIgnoreCase("value"))
                            {
				String value = null;

				if (nextGreatGreatGrandKid.getFirstChild() != null)
				    value = nextGreatGreatGrandKid.getFirstChild().getNodeValue();
                                values.add(value);
                            }
                        
                        }
                        command.setProperty(propertyNameAttr, values);
                    }
                }
            }
        }
        
        return command;
    }
    
    
    /**
     find the option in the options list given the option name
     @param optionStr the name of the option
     @return ValidOption, the ValidOption if found else return null
     */
    private ValidOption findOption(String optionStr)
    {
        ValidOption option = null;
        option = (ValidOption) validOptions.get(optionStr);
        return option;
    }
    
    
    /**
     serialize the ValidCommandsList object to file named 
     SERIALIZED_COMPONENTS_FILE
     */
    private void saveCommandsAsMultipleFiles() throws CommandValidationException
    {
        Iterator commands = commandsList.getCommands();
        while (commands.hasNext())
        {
            try
            {
                ValidCommand command = (ValidCommand) commands.next();
                File file = new File(serializeDir);
                if (!file.exists())
                {
                    file.mkdir();
                }
                String encodedCommandName = null; 
				try{
                    encodedCommandName = URLEncoder.encode(command.getName(),"UTF-8");
				}catch(UnsupportedEncodingException ue){
                    encodedCommandName = command.getName();
				}
                ObjectOutputStream os = new ObjectOutputStream(
                                           new FileOutputStream(serializeDir + 
                                                    "/."+ encodedCommandName));
                /** write CommandProperties to serialized file **/
                os.writeObject(defaultCommand);
                os.writeObject(helpClass);
                os.writeObject(properties);
                    
                os.writeObject(command);
            }
            catch(Exception e)
            {
		//do we want to escalate this exception or just print a message.
		LocalStringsManager lsm = 
		    LocalStringsManagerFactory.getFrameworkLocalStringsManager();
		CLILogger.getInstance().printWarning(lsm.getString(
						     "CouldNotWriteCommandToFile", 
						     new Object[] {e.getMessage()} ));
            }
        }
    }
    
    /**
     serialize the ValidCommand objects to file with cli/.{command_name}
     */
    private void saveCommandsAsSingleFile() throws CommandValidationException
    {
        try
        {
            ObjectOutputStream os = new ObjectOutputStream(
                                       new FileOutputStream(serializeDir + 
                                            "/"+ SERIALIZED_DESCRIPTOR_FILE)); 
            os.writeObject(defaultCommand);
            os.writeObject(helpClass);
            os.writeObject(properties);
                    
            os.writeObject(commandsList);
        }
        catch(Exception e)
        {
	    //do we want to escalate this exception or just print a message.
	    LocalStringsManager lsm = 
		LocalStringsManagerFactory.getFrameworkLocalStringsManager();
	    CLILogger.getInstance().printWarning(lsm.getString(
						 "CouldNotWriteComponentToFile", 
						 new Object[] {e.getMessage()} ));

        }
    }
    
    
    /**
     * returns the serializeDescriptors property
     */
    private int getSerializeDescriptorsProperty()
    {
        return serializeDescriptors;
    }
    
    /**
     * set the serializeDescriptors property.
     * DONT_SERIALIZE  CLIDescriptor's will not be loaded from the file nor will 
                        be saved (serialized) to a file
     * SERIALIZE_COMMANDS_TO_FILE CLIDescriptor's will be loaded/saved from/to a 
                                single file (cli/.CLIDescriptors) if exists
     * SERIALIZE_COMMANDS_TO_FILES CLIDescriptor's will be loaded/saved from the 
     *                              multiple files (cli/.{command_name's}) if exists
     */
    public void setSerializeDescriptorsProperty(int serializeDescriptors)
    {
        if ((serializeDescriptors <= 2) && (serializeDescriptors >=0))
            this.serializeDescriptors = serializeDescriptors;
    }
    
    /**
     * Returns the File object of the directory containing the multile
       serialized files, each containing a command
     * @return File file object of the directory containing all the command files
     */
    public String getSerializeDir()
    {
        return serializeDir;
    }
    
    
    /**
     *
     */
    private void setSerializeDir(String serializeDir) 
                    throws CommandValidationException
    {
        try
        {
            File file = new File(serializeDir);
            if (file.exists())
            {
                this.serializeDir = serializeDir;
            }
            else
            {
		LocalStringsManager lsm = 
		    LocalStringsManagerFactory.getFrameworkLocalStringsManager();
		CLILogger.getInstance().printWarning(lsm.getString(
						     "InvalidFilePath", 
						     new Object[] {serializeDir}));
            }
        }
        catch (NullPointerException npe)
        {
	    LocalStringsManager lsm = 
		LocalStringsManagerFactory.getFrameworkLocalStringsManager();
            throw new CommandValidationException(lsm.getString("CouldNoSetSerializeDirectory"),
						 npe);
        }
    }


    private void replaceOptionsInCommandsList(HashMap replaceOptions)
    {
        Iterator replaceOptionNames = replaceOptions.keySet().iterator();
        while (replaceOptionNames.hasNext())
        {
            final String replaceOptionName = (String) replaceOptionNames.next();
            Iterator commands = commandsList.getCommands();
            while (commands.hasNext())
            {
                ValidCommand command = (ValidCommand) commands.next();
                if ( command.hasValidOption(replaceOptionName) ||
                     command.hasRequiredOption(replaceOptionName) ||
                     command.hasDeprecatedOption(replaceOptionName) )
                {
                    command.replaceAllOptions((ValidOption)replaceOptions.get(replaceOptionName));
                }
            }
        }
    }


        /**
         *  This method checks for clashing option names and short options in a command.
         **/  
    private void validateOptionsInCommand() throws CommandValidationException
    {
        final Iterator commands = commandsList.getCommands();
        while (commands.hasNext())
        {
            final ValidCommand command = (ValidCommand) commands.next();
            Vector<ValidOption> vvo = command.getOptions();
            final LocalStringsManager lsm = LocalStringsManagerFactory.getFrameworkLocalStringsManager();
            
            
            int ii = 1;
            final int size = vvo.size();
            for (ValidOption vo : vvo)
            {
                final String optionName = vo.getName();
                    //get rest of the options and compare
                if (ii<size) {
                    java.util.List<ValidOption> sub_vvo = vvo.subList(ii,size);
                    for (ValidOption sub_vo : sub_vvo)
                    {
//System.out.println("***** Compare: optionName = " + optionName + " and " + sub_vo.getName() + " for " + command.getName());
                        if (optionName.equals(sub_vo.getName()))
                        {
                            throw new CommandValidationException(lsm.getString("OptionAlreadyDefined",
                                                                 new Object[] {optionName, command.getName()} ));
                        }
                            //check short option clash
                        if (vo.hasShortName()) 
                        {
                            final Vector<String> shortNames = vo.getShortNames();
                            for (String shortName : shortNames)
                            {
                                final Vector<String> sub_shortNames = sub_vo.getShortNames();
                                if (sub_shortNames.contains(shortName))
                                {
                                    throw new CommandValidationException(lsm.getString("ShortOptionAlreadyDefined",
                                                                         new Object[] {shortName,
                                                                         vo.getName(), sub_vo.getName(),
                                                                         command.getName()} ));
                                }
                            }
                        }
                    }
                }
                ii++;
            }
        }
    }
    
    

    public String getEnvironmentPrefix(){
        if(properties != null){
            for(int i = 0 ; i < properties.size(); i++){
                Properties props = (Properties)properties.get(i);
                String prefix = props.getProperty(ENVIRONMENT_PREFIX);
                if(prefix != null)
                {
                    return prefix;
                }
            }
        }
        return null;
    }

    
    public String getEnvironmentFileName(){
        if(properties != null){
        for(int i = 0 ; i < properties.size(); i++){
            Properties props = (Properties)properties.get(i);
            String file = props.getProperty(ENVIRONMENT_FILENAME);
            if(file != null)
            {
                return file;
            }
        }
        }
        return ".cliprefs";
    }

    
    /**
     *main method to test if the functionality works
     */
    public static void main(String[] args)
    {
        try
        {
            //read CLI descriptor
            final CLIDescriptorsReader cliDescriptorsReader = CLIDescriptorsReader.getInstance();
            
            final String SERIALIZE_TYPE = System.getProperty("SERIALIZE_TYPE");

                //default is to serialize the commands to files
            if (SERIALIZE_TYPE == null)
               cliDescriptorsReader.setSerializeDescriptorsProperty(CLIDescriptorsReader.SERIALIZE_COMMANDS_TO_FILES);
            else {
                if (SERIALIZE_TYPE.equals("SINGLE_FILE"))
                    cliDescriptorsReader.setSerializeDescriptorsProperty(CLIDescriptorsReader.SERIALIZE_COMMANDS_TO_FILE);
                if (SERIALIZE_TYPE.equals("MULTIPLE_FILES"))
                    cliDescriptorsReader.setSerializeDescriptorsProperty(CLIDescriptorsReader.SERIALIZE_COMMANDS_TO_FILES);
                if (SERIALIZE_TYPE.equals("DONT_SERIALIZE"))            
                    cliDescriptorsReader.setSerializeDescriptorsProperty(CLIDescriptorsReader.DONT_SERIALIZE);
            }
            

            cliDescriptorsReader.readDescriptors();

                /*
            while(commands.hasNext())
                System.out.println((ValidCommand)commands.next());
            Iterator propertiesList = cliDescriptorsReader.getProperties();
            System.out.println("Properties = ");
            while (propertiesList.hasNext())
            {
                Properties properties = (Properties) propertiesList.next();
                System.out.println(properties.toString());
            }
            System.out.println(cliDescriptorsReader.getCommand("deploy"));
                */

        }
        catch(CommandValidationException cve)
        {
            System.out.println(cve.getLocalizedMessage());
            System.exit(1);
        }
    }
}


class CLIEntityResolver implements EntityResolver {

    /** Creates a new instance of CLIEntityResolver */
    public CLIEntityResolver() {
    }

    public InputSource resolveEntity(String publicId, String systemId)
        throws org.xml.sax.SAXException, java.io.IOException {
        if (systemId.endsWith("CLISpecification.xsd")) {
            // return a special input source
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(
                "com/sun/enterprise/cli/framework/CLISpecification.xsd");
            return new InputSource(in);
        } else {
            // use the default behaviour
            return null;
        }
    }

}


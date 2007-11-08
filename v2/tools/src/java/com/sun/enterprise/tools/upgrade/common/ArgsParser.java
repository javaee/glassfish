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

package com.sun.enterprise.tools.upgrade.common;


import java.util.logging.*;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Vector;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.sun.enterprise.tools.upgrade.common.arguments.ParsedArgument;
import com.sun.enterprise.tools.upgrade.common.arguments.ARG_help;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Parse the arguments for the upgrade tool
 * and invoke the appropriate handler
 *
 * @author Hans Hrasna
 */
public class ArgsParser {
    
    public static final String SOURCE = "source";
    public static final String SOURCE_SHORT = "s";
    public static final String TARGET = "target";
    public static final String TARGET_SHORT = "t";
    public static final String DOMAIN = "domain";
    public static final String DOMAIN_SHORT = "d";
    public static final String NSSPWD = "nsspwd";
    public static final String NSSPWDFILE = "nsspwdfile";
    public static final String NSSPWDFILE_SHORT = "n";
    public static final String TARGETNSSPWD = "targetnsspwd";
    public static final String TARGETNSSPWDFILE = "targetnsspwdfile";
    public static final String TARGETNSSPWDFILE_SHORT = "e";
    public static final String JKSPWD = "jkspwd";
    public static final String JKSPWDFILE = "jkspwdfile";
    public static final String JKSPWDFILE_SHORT = "j";
    public static final String CAPWD = "capwd";
    public static final String CAPWDFILE = "capwdfile";
    public static final String CAPWDFILE_SHORT = "p";
    public static final String ADMINUSER = "adminuser";
    public static final String ADMINUSER_SHORT = "a";
    public static final String ADMINPASSWORD = "adminpassword";
    public static final String ADMINPASSWORD_SHORT = "w";
    public static final String MASTERPASSWORD = "masterpassword";
    public static final String MASTERPASSWORD_SHORT = "m";
    public static final String CLINSTANCEINFO = "clinstancefiles";
    public static final String CLINSTANCEINFO_SHORT = "i";
    
    private static Logger _logger=LogService.getLogger(LogService.UPGRADE_LOGGER);
    private String [] arguments;
    private CommonInfoModel commonInfo;
    private HashMap interactiveMap;
    private StringManager sm;
    
    /** Creates a new instance of ArgsParser */
    public ArgsParser(String [] args, CommonInfoModel infoModel, HashMap hMap) {
        sm = StringManager.getManager(LogService.UPGRADE_CLI_LOGGER);
        commonInfo = infoModel;
        interactiveMap = hMap;
        arguments = args;
    }
    
    public ArgsParser(String [] args, CommonInfoModel infoModel) {
        this(args, infoModel, new HashMap());
    }
    
    public HashMap parse() {
        return parse(null);
    }
    
    /**
     * Method to parse and process the arguments passed through CLI.
     * Method to collect the missing arguments from the user.
     */
    public HashMap parse(InteractiveInput interactiveParser) {
        String cmd = null;
        Vector parameters = new Vector();
        Vector instructions = new Vector();
        Class instructionHandler = null;

        //Iterate through arguments and Parse the instructions 
        for(int i=0;i<arguments.length;i++) {
            if(arguments[i].startsWith(UpgradeConstants.CLI_OPTION_HYPHEN)){
                String rawArg = arguments[i];

                //Strip - or --
                String argument = rawArg.substring(
                        rawArg.lastIndexOf(UpgradeConstants.CLI_OPTION_HYPHEN) + 1, 
                        rawArg.length()); 
                String delimeters = "=";
                StringTokenizer st = new StringTokenizer(argument, delimeters, 
                        false);
                if ( st.hasMoreTokens() ) {
                    cmd = st.nextToken();				
                    instructionHandler = getHandler(cmd);
                    parameters = new Vector();
                }
                if (st.hasMoreTokens()) { //there was an equal sign
                    parameters.add(st.nextToken());
                }
                while(i + 1 < arguments.length && 
                        !arguments[i + 1].startsWith(
                        UpgradeConstants.CLI_OPTION_HYPHEN)) {
                    i++;
                    parameters.add(arguments[i]);
                }
                if ( instructionHandler != null ) {
                    interactiveMap.put(cmd, parameters);
                    instructions.addElement(new ParsedArgument(instructionHandler, 
                            cmd, parameters, commonInfo, interactiveMap));
                } else {
                    String msg = sm.getString("enterprise.tools.upgrade.cli.invalid_option",
                            rawArg);
		    if (cmd.equals(ADMINPASSWORD_SHORT) || cmd.equals(ADMINPASSWORD)) {
			msg = msg + "\n" + 
                                sm.getString("enterprise.tools.upgrade.cli.deprecated_option",rawArg) + 
			        " " + 
                                sm.getString("enterprise.tools.upgrade.cli.deprecated_option.adminpassword");
		    } else if (cmd.equals(MASTERPASSWORD_SHORT) || cmd.equals(MASTERPASSWORD)) {
			msg = msg + "\n" + 
                                sm.getString("enterprise.tools.upgrade.cli.deprecated_option",rawArg) + 
			        " " + 
                                sm.getString("enterprise.tools.upgrade.cli.deprecated_option.masterpassword");
		    }
                     
                    helpUsage(msg);
                    commonInfo.recover();		    
                    System.exit(1);
                }
            }
        }
        
        //Process the parsed instructions for all arguments passed.
        Enumeration e = instructions.elements();
        while ( e.hasMoreElements() ) {
            ParsedArgument  pi = (ParsedArgument )e.nextElement();
            instructionHandler = pi.getHandler();
            try {
                Class [] parameterTypes = new Class [] {pi.getClass()};
                Constructor constructor = 
                        instructionHandler.getConstructor(parameterTypes);
                constructor.newInstance(new Object [] { pi });
            } catch (Exception ex) {
                _logger.log(Level.INFO, 
                        sm.getString("enterprise.tools.upgrade.cli.invalid_option", 
                        pi.getMneumonic()), ex);
                commonInfo.recover();		
                System.exit(1);
            }
        }

        //Collect missing arguments that were not passed.
        if(interactiveParser != null) {
            interactiveParser.collectMissingArguments(interactiveMap);
        }
        
        return interactiveMap;
    }
    
    private Class getHandler(String cmd) {
        Class instructionHandler = null;
        try {
	    //start CR 6396918
	    if(cmd.equals(ADMINPASSWORD_SHORT) || cmd.equals(ADMINPASSWORD)
               || cmd.equals(MASTERPASSWORD_SHORT) || cmd.equals(MASTERPASSWORD))
                instructionHandler = null;
            else if (cmd.equals("v")) {
                // CR 6568819 - adds support for -v option. For -v option use
                // the class ARG_V.java (same as for -V option).
		instructionHandler = 
                        Class.forName("com.sun.enterprise.tools.upgrade.common.arguments.ARG_" + "V");
	    } else {
	        //end CR 6396918
		instructionHandler = 
                        Class.forName("com.sun.enterprise.tools.upgrade.common.arguments.ARG_" + cmd);
	    }
            return instructionHandler;
        } catch (ClassNotFoundException cnf) {
            
        } catch (Exception e1) {
            _logger.log(Level.INFO, sm.getString("enterprise.tools.upgrade.cli.invalid_option",e1), e1);
        }
        return null;
    }
    
    public void helpUsage(String str){
        System.out.println("\n" + str + "\n");
        helpUsage();
    }
    
    public void helpUsage(){
        helpUsage(0);
    }
    
    public void helpUsage(int exitCode) {
        new ARG_help(new ParsedArgument(null, null, null, commonInfo, null), exitCode);        
    }
}

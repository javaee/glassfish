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
 * UpgradeConstants.java
 *
 * Created on March 10, 2004, 3:24 PM
 */

package com.sun.enterprise.tools.upgrade.common;

/**
 *
 * @author  prakash
 */
import java.util.HashMap;
import java.util.HashSet;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;

public final class UpgradeConstants {
    
    private static StringManager stringManager = StringManager.getManager(LogService.UPGRADE_COMMON_LOGGER);
    private static StringBuffer cliInstructions = new StringBuffer();
    static {
        cliInstructions.append("\n");
        cliInstructions.append(stringManager.getString("upgrade.common.upgrade_instructions"));
        cliInstructions.append("\n");
        cliInstructions.append("\n");
        cliInstructions.append(stringManager.getString("upgrade.common.upgrade_instructions_cont"));
        cliInstructions.append("\n");       
        cliInstructions.append("\n"); 
    }
    
    public static final String VERSION_AS7X_PE = "as7xpe";
    public static final String VERSION_AS7X_SE = "as7xse";
    public static final String VERSION_AS7X_EE = "as7xee";
    
    public static final String VERSION_AS80_PE = "as80pe";
    public static final String VERSION_AS80_SE = "as80se";
    public static final String VERSION_AS80_EE = "as80ee";

    public static final String VERSION_AS81_PE = "as81pe";
    public static final String VERSION_AS81_SE = "as81se";
    public static final String VERSION_AS81_EE = "as81ee";
    
    public static final String VERSION_AS82_PE = "as82pe";
    public static final String VERSION_AS82_SE = "as82se";
    public static final String VERSION_AS82_EE = "as82ee";
    
    public static final String VERSION_AS90_PE = "as90pe";
    public static final String VERSION_AS90_SE = "as90se";
    public static final String VERSION_AS90_EE = "as90ee";

    public static final String EDITION_PE = "pe";
    public static final String EDITION_SE = "se";
    public static final String EDITION_EE = "ee";
    
    public static final String VERSION_7X = "7x";
    public static final String VERSION_80 = "80";
    public static final String VERSION_81 = "81";
    public static final String VERSION_82 = "82";
    public static final String VERSION_90 = "90";
    // CR6568819 : Change from 91 to 9.1 so asupgrade --version returns 9.1 not 91
    public static final String VERSION_91 = "9.1";

    public static final String DEVELOPER_PROFILE = "developer";
    public static final String ENTERPRISE_PROFILE = "enterprise";

    //asupgrade related constants     
    public static final String ASUPGRADE = "asupgrade";
    public static final String ASUPGRADE_BAT = "asupgrade.bat";   
    public static final String ASUPGRADE_LOG = "upgrade.log";
    public static final String ENTITY_RESOLVER_CLASS = "com.sun.enterprise.config.serverbeans.ServerValidationHandler";
    public static final String BACKUP_DIR = "backup";

    //cli mode related constants    
    public static final String CLI_OPTION_CONSOLE_SHORT = "-c";
    public static final String CLI_OPTION_CONSOLE_LONG = "--console";
    public static final String CLI_OPTION_VERSION_UC_SHORT = "-V";
    public static final String CLI_OPTION_VERSION_LC_SHORT = "-v";
    public static final String CLI_OPTION_VERSION_LONG = "--version";  
    public static final String CLI_OPTION_HELP_SHORT = "h";
    public static final String CLI_OPTION_HYPHEN = "-";   
    public static final String CLI_OPTION_NOPROMPT = "noprompt";    
    public static final String CLI_USER_INSTRUCTIONS = cliInstructions.toString(); 
	    
    //application server related constants    
    public static final String AS_DOMAIN_ROOT = "com.sun.aas.domainRoot";
    public static final String AS_INSTALL_ROOT = "com.sun.aas.installRoot";
    public static final String AS_BIN_DIRECTORY = "bin";
    public static final String AS_CONFIG_DIRECTORY = "config";
    public static final String AS_ADMIN_ENV_CONF_FILE = "asadminenv.conf";
    public static final String AS_PROPERTY_ADMIN_PROFILE = "AS_ADMIN_PROFILE=";   
    public static final String ASADMIN_COMMAND = "asadmin";
    public static final String ASADMIN_BAT = "asadmin.bat";

    //operating system related constants    
    public static final String OS_NAME_IDENTIFIER = "os.name";
    public static final String OS_NAME_WINDOWS = "Windows";
    
    public static final HashMap supportMap = new HashMap();
    static{
        HashSet x7PESet = new HashSet();
        supportMap.put(VERSION_AS7X_PE,x7PESet);
        
        HashSet x7SESet = new HashSet();
        supportMap.put(VERSION_AS7X_SE,x7SESet);
        
        HashSet x7EESet = new HashSet();
        supportMap.put(VERSION_AS7X_EE,x7EESet);
        
        HashSet pE80Set = new HashSet();
        pE80Set.add(VERSION_AS90_PE);
        pE80Set.add(VERSION_91);
        supportMap.put(VERSION_AS80_PE,pE80Set);

        HashSet eE80Set = new HashSet();
        eE80Set.add(VERSION_91);
        supportMap.put(VERSION_AS80_SE,eE80Set); 
        
        HashSet pE81Set = new HashSet();
        pE81Set.add(VERSION_AS90_PE);
        pE81Set.add(VERSION_91);
        supportMap.put(VERSION_AS81_PE,pE81Set);
        
        HashSet eE81Set = new HashSet();
        eE81Set.add(VERSION_91);
        supportMap.put(VERSION_AS81_EE,eE81Set);  
        
        HashSet pE90Set = new HashSet();
        pE90Set.add(VERSION_91);
        supportMap.put(VERSION_AS90_PE,pE90Set);
        
        HashSet sE90Set = new HashSet();
        supportMap.put(VERSION_AS90_SE,sE90Set);
        
        HashSet eE90Set = new HashSet();
        supportMap.put(VERSION_AS90_EE,eE90Set);

        HashSet pE82Set = new HashSet();
        pE82Set.add(VERSION_AS90_PE);
        pE82Set.add(VERSION_91);
        supportMap.put(VERSION_AS82_PE,pE82Set);

        HashSet sE82Set = new HashSet();
        sE82Set.add(VERSION_91);
        supportMap.put(VERSION_AS82_SE,sE82Set);

        HashSet eE82Set = new HashSet();
        eE82Set.add(VERSION_91);
        supportMap.put(VERSION_AS82_EE,eE82Set);
    };

    /** Creates a new instance of UpgradeConstants */
    public UpgradeConstants() {
    }
    
    /*
     * Returns readable strings for user output that maps to version and edition strings.
     */
    public static String readableString(String ve){
        return stringManager.getString("upgradeConstants."+ve);        
    }
}

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
package com.sun.enterprise.diagnostics;

import java.util.Map;
import java.util.Date;


/**
 * Represents CLI options and operand provided by the user
 * at the time of report generation
 * @author Manisha Umbarje
 */
public class CLIOptions {

    private static final String OUTPUT_FILE ="outputfile";
    private static final String FILE ="file";
    private static final String CUSTOMER_INPUT ="input";
    private static final String BUG_IDS = "bugids";
    private static final String LOG_START_DATE = "logstartdate";
    private static final String LOG_END_DATE = "logenddate";
    private static final String TARGET_DIR = "targetdir";
    //private static final String LOCAL_FLAG = "local";
    private static final String TARGET ="target";
    private static final String USER = "user";
    private static final String PASSWD = "passwd";
    private static final String LOCAL_FLAG = "local";
    private Date startDate ;
    private Date endDate;
    private String customerInput;
    private String customerInputFile;
    private String bugIds;
    private String destReportFile;
    //private boolean local;
    private String reportDir;
    private String targetDir;
    private String targetName;
    private String targetType;
    private String user;
    private String passwd;
    private boolean local;
    
    private Map map;
    public CLIOptions(Map options) {
        
        this((Date)options.get(LOG_START_DATE), 
            (Date)options.get(LOG_END_DATE),
            (String)options.get(FILE),
            (String)options.get(CUSTOMER_INPUT),
            (String)options.get(BUG_IDS),
            (String)options.get(OUTPUT_FILE),
            (String)options.get(TARGET_DIR),
            (String)options.get(TARGET),
            (String)options.get(USER),
            (String)options.get(PASSWD),
            Boolean.valueOf((String)options.get(LOCAL_FLAG)).booleanValue());
            this.map = options;
        
            
    }

    public CLIOptions(Date startDate, Date endDate, String file,  
            String customerInput, String  bugIds, String destReportFile, 
            String targetDir,String targetName, String user, String passwd,
            boolean local) {
	this.startDate = startDate;
	this.endDate = endDate;
	this.targetName = targetName;
	this.targetDir = targetDir;
	this.destReportFile = destReportFile;
	this.local = local;
        this.customerInputFile = file;
        this.customerInput = customerInput;
        this.bugIds = bugIds;
        this.user = user;
        this.passwd = passwd;
    }

    /**
     * Begin Date from which server.log contents need to be copied
     */
    public Date getStartDate() {
	return startDate;
    }

    /**
     * End Date to which server.log entries need to be collected
     */
    public Date getEndDate() {
	return endDate;
    }

    /**
     * Returns true if command is being run in local mode
     */
    public boolean isLocal() {
	return local;
    }

    /**
     * Returns value of CLI option - targetDir if  command is being 
     * run in local mode
     */
    public String getTargetDir() {
	return targetDir;
    }

    /**
     * Returns targetName
     */
    public String getTargetName() {
	return targetName;
    }

    
    /**
     * Returns name of generated report in compressed format
     */
    public String getReportFile() {
	return destReportFile;
    }
  
    /**
     * Returns name of customer input file
     * @return customer supplied text file
     */
    public String getCustomerInputFile() {
        return customerInputFile;
    }
    
    /**
     * Returns customer input entered by user in admin GUI
     * @return customer supplied text 
     */
    public String getCustomerInput() {
        return customerInput;
    }


    /**
     * Returns bugids provided by the customer
     * @return bugids entered by customer
     */
    public String getBugIds() {
        return bugIds;
    }
    
    /**
     * Returns user
     */
    public String getUser() {
        return user;
    }
    
    /**
     * Returns passwd
     */
    public String getPasswd() {
        return passwd;
    }
    
    public String toString() {
        return getTargetDir() + ","  + getTargetName() + "," + 
                getCustomerInputFile() + "," + getBugIds() +
                "," + getStartDate() +
                "," + getEndDate() + "," + getReportFile();
                
    }
    
    public Map getMap() {
        return map;
    }
}

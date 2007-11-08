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
import com.sun.enterprise.util.SystemPropertyConstants;
import java.io.File;

/**
 * Diagnostic Service related constants.
 * @author Manisha Umbarje
 */
public interface Constants {

    // Log related constants
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN =
		    "yyyy-MM-dd'T'HH-mm-ss";
    public static final char FIELD_SEPARATOR = '|';
    public static final String ENTRY_END_INDICATOR = "|#]";
    public static final String ENTRY_BEGIN_INDICATOR = "[#|";
    public static final int ENTRY_DATE_BEGIN_INDEX = 3;
    public static final int ENTRY_DATE_LENGTH = 10;
    public static final char FILENAME_DATE_SEPARATOR = '_';

    //Config related constants
    public static final String CONFIG_DIR = File.separator + 
            "config";
    public static final String DOMAIN_XML =   CONFIG_DIR + 
            File.separator +   "domain.xml";;
    public static final String DOMAIN_XML_DTD = File.separator + 
            "lib" + File.separator + "dtds" +
            File.separator + "sun-domain_1_2.dtd";
    public static final String SUN_ACC = CONFIG_DIR + 
		    File.separator + "sun-acc.xml";
    public static final String LOGIN_CONF = CONFIG_DIR + 
		    File.separator + "login.conf";
    public static final String SERVER_POLICY = CONFIG_DIR + 
            File.separator + "server.policy";
    
    // App Info collector related constants
    public static final String GENERATED = "generated";
    public static final String GENERATED_DIR = File.separator + GENERATED;
    public static final String APPLICATIONS = "applications";
    public static final String APPLICATIONS_DIR = 
		    File.separator + APPLICATIONS;

    public static final String REPORT_SUMMARY = File.separator + 
            "ReportSummary.html";
    
    public static final String SERVER = 
            SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;
    public static final String INSTALLATION_LOG_PREFIX = "Install_Application_Server";
    public static final String SJSAS_INSTALLATION_LOG_PREFIX = "Sun_Java_System_Application_Server";
    
}

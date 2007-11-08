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
package com.sun.enterprise.diagnostics.util;

import java.io.File;

import java.io.FilenameFilter;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sun.enterprise.diagnostics.Constants;
import com.sun.enterprise.diagnostics.DiagnosticException;
/**
 * Accepted file names are ejb-jar.xml, sun-ejb-jar.xml, web.xml, sun-web.xml,
 * application.xml, sun-application.xml .
 * @author Manisha Umbarje
 */
public class DDFilter implements FilenameFilter {

    private static final String ejb_jar = "ejb-jar.xml";
    private static final String sun_ejb_jar = "sun-ejb-jar.xml";
    private static final String web = "web.xml";
    private static final String sun_web = "sun-web.xml";
    private static final String application = "application.xml";
    private static final String sun_application = "sun-application.xml";
    private static final String META_INF = "META-INF";
    private static final String WEB_INF = "WEB-INF";

    public boolean accept (File aDir, String fileName) {
            
            if (fileName.matches(ejb_jar) || 
                    fileName.matches(sun_ejb_jar) || 
                    fileName.matches(web) || 
                    fileName.matches(sun_web)||
                    fileName.matches(application) ||
                    fileName.matches(sun_application)) {
                return true;
            }
            else
                return false;
    }
}
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.tools.upgrade.logging.LogService;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Utility that searches a directory for child directories and files not in
 * the exclude list.
 * @author rebeccas
 */

public class UpgradeFileFilter implements FilenameFilter{
    private HashSet <String> excludeList = new HashSet <String>();

    private final StringManager stringManager =
        StringManager.getManager(UpgradeFileFilter.class);
    private static final Logger logger = LogService.getLogger();
    
    /**
     *
     * @param propFile Property filename containing list of excluded file and dir names
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws java.lang.NullPointerException
     */
    public UpgradeFileFilter (String propFile)throws FileNotFoundException,
        IOException, NullPointerException {
        loadExcludeList(propFile);
    }

    
    /**
     * Load into lookup table, names to be exclueded.
     * @param propFile
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws java.lang.NullPointerException
     */
    private void loadExcludeList(String propFile) throws FileNotFoundException,
            IOException, NullPointerException {
        Properties p = new Properties();
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(propFile);
        p.load(is);
        is.close();

        Enumeration<String> eList = (Enumeration<String>) p.propertyNames();
        while (eList.hasMoreElements()) {
            String s = eList.nextElement();
            excludeList.add(s);
        }
    }

     public boolean accept(File dir, String name) {
        boolean flag = false;
        if (!excludeList.contains(name)) {
            flag = true;
        } 
        return flag;
    }
}

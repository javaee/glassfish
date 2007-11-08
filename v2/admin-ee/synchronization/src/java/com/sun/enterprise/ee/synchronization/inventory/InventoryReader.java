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
package com.sun.enterprise.ee.synchronization.inventory;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import com.sun.enterprise.util.io.FileUtils;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;

/**
 * Responsible for reading persisted inventory.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class InventoryReader {

    /**
     * Constructor.
     *
     * @param  file  path to the inventory file
     */
    InventoryReader(File file) {
        _inventory = file;
    }

    /**
     * Reads the persisted inventory. It returns the inventory list 
     * items with forward slashes.
     *
     * @return  inventory list as java.lang.String or empty list
     */
    List read() throws IOException {

        List list              = new ArrayList();
        FileReader fr          = null;
        BufferedReader reader  = null;

        try {
            if (_inventory.exists()) {
                fr      = new FileReader(_inventory);
                reader  = new BufferedReader(fr);
                String in;

                while ((in=reader.readLine()) != null) {
                    // converts all path separators to forward slash
                    list.add( FileUtils.makeForwardSlashes(in) );
                }
            }
        } finally {
            try {
                if (fr != null)     { fr.close();     }
                if (reader != null) { reader.close(); }
            } catch (Exception e) { }
        }

        return list;
    }

    // ---- VARIABLE(S) - PRIVATE -------------------------------
    private File _inventory = null;
    private static Logger _logger = 
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
    private static final StringManager _localStrMgr = 
        StringManager.getManager(InventoryReader.class);
}

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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import com.sun.enterprise.util.io.FileUtils;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Responsible for generating diff between two inventory list.
 *
 * <pre>
 * For example, if original list contains:
 *      0. /tmp/a
 *      1. /tmp/b
 *      2. /tmp/c
 * and target inventory contains:
 *      0. /tmp/a
 *      1. /tmp/b
 *      2. /tmp/xyz
 * then, this method will return the following:
 *      /tmp/c
 * </pre>
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class InventoryDiff {

    /**
     * Constructor.
     *
     * @param  origList    original list
     * @param  targetList  target list
     */
    InventoryDiff(List origList, List targetList) {

        _origList    = transformInventory(origList);
        _targetList  = transformInventory(targetList);
    }

    /**
     * Returns the extra file, if any, in the original list.
     * All files paths are converted to forward slashes.
     *
     * @return  extra file in the original list
     */
    List diff() {
        List list = new ArrayList();

        if ( (_origList == null) || (_origList.length == 0) ) {
            return list;
        }
        if ( (_targetList == null) || (_targetList.length == 0) ) {
            return Arrays.asList(_origList);
        }

        for (int i=0; i<_origList.length; i++) {
            // if file in original list does not exist in target list
            int idx = -1;
            if ((idx=Arrays.binarySearch(_targetList, _origList[i])) < 0) {
                list.add(_origList[i]);
                _logger.log(Level.FINE, "Found diff " + _origList[i]);
            } else {
                _logger.log(Level.FINE, "No diff - idx : " + idx 
                            + " Target List: " + _targetList[idx] 
                            + " Original List: " + _origList[i] );
            }
        }

        return list;
    }

    /**
     * Converts the file paths to forward slashes. It sorts the list.
     *
     * @param  list  inventory list
     *
     * @return  transformed list
     */
    static String[] transformInventory(List list) {

        if (list.size() == 0) {
            return null;
        }
        String[] l = new String[list.size()];

        l = (String[]) list.toArray(l);
        for (int i=0; i<l.length; i++) {
            l[i] = FileUtils.makeForwardSlashes(l[i]);
        }
        Arrays.sort(l);

        return l;
    }

    // ---- VARIABLE(S) - PRIVATE -------------------------------
    private String[] _origList    = null;
    private String[] _targetList  = null;

    private static Logger _logger = 
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
    private static final StringManager _localStrMgr = 
        StringManager.getManager(InventoryDiff.class);
}

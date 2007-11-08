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
package com.sun.enterprise.ee.synchronization.store;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.ee.synchronization.inventory.InventoryMgr;

import java.io.IOException;

/**
 * Helper class to randomly move files between two directories.
 *
 * @author Nazrul Islam
 */
public class RandomFileMover {

    /**
     * Constructor!
     *
     * @param  backupDir  root backup dir 
     * @param  newDir     root dir 
     * @param  copyFile   if true, few files will be copied
     */
    public RandomFileMover(File backupDir, File newDir, boolean copyFile) {

        _backupDir         = backupDir;
        _newDir            = newDir;
        _copyFileEnabled   = copyFile;

        if (!_backupDir.isDirectory()) {
            _backupDir.mkdirs();
        }
        if (!_newDir.isDirectory()) {
            _newDir.mkdirs();
        }

        _random   = new Random();
        _moveList = new ArrayList();
        _copyList = new ArrayList();
    }

    /**
     * Moves files at random from one dir to another.
     *
     * @throws IOException  if an i/o error
     */
    public void move() throws IOException {
        InventoryMgr mgr = new InventoryMgr(_backupDir);
        List files = mgr.getInventory();

        Iterator iter = files.iterator();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            File from = new File(_backupDir, name); 
            File to = new File(_newDir, name);

            if (_random.nextBoolean()) {
                if (_copyFileEnabled && _random.nextBoolean()) {
                    FileUtils.copy(from, to);
                    _copyList.add(name);
                }
            } else {
                from.renameTo(to);
                _moveList.add(name);
            }
        }
    }

    public List getMoveList() {
        return _moveList;
    }

    public List getCopyList() {
        return _copyList;
    }

    // ---- VARIABLE(S) - PRIVATE --------------------------
    private File _backupDir          = null;
    private File _newDir             = null;
    private Random _random           = null;
    private List _moveList           = null;
    private List _copyList           = null;
    private boolean _copyFileEnabled = false;
}

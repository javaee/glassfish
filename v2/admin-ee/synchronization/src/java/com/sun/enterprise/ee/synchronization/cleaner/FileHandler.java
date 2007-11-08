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
package com.sun.enterprise.ee.synchronization.cleaner;

import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;

/**
 * Destroys a file or a directory.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class FileHandler {

    FileHandler(File file, File trash) {
        _file   = file;
        _trash  = trash;
    }

    void remove() throws IllegalAccessException, IOException {

        // file does not exist
        if ( (_file == null) || (!_file.exists()) ) {
            return;
        }

        // if file is in the exclude list or dir is parent of of excluded file
        if (CleanerUtils.isExcluded(_file.getCanonicalPath())) {
            return;
        }

        // last modified time stamp of the file
        long lModified = _file.lastModified();

        // current system time
        long cTime = System.currentTimeMillis();

        // if file is not old enough, return
        if ( (cTime-lModified) < DEF_WAIT_PERIOD) {
            return;
        }

        // appends system time to the file
        File tempFile = new File(_trash, _file.getName()+cTime);

        // renames the file before removal
        if ( !_file.renameTo(tempFile) )  {
            String msg = _localStrMgr.getString("fileRenameError",  
                                                _file, tempFile);
            throw new IllegalAccessException(msg);
        }

        // removes the renamed file
        //FileUtils.whack(tempFile);
        tempFile.deleteOnExit();

        // logs the activity
        _logger.log(Level.FINE, 
            "synchronization.cleaner.file_removed", _file.getPath());
    }

    // ---- INSTANCE VARIABLE(S) -----------------------------
    private File _file                         = null;
    private File _trash                        = null;
    private static final long DEF_WAIT_PERIOD  = 2000;

    private static Logger _logger = 
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
    private static final StringManager _localStrMgr = 
        StringManager.getManager(FileHandler.class);
}

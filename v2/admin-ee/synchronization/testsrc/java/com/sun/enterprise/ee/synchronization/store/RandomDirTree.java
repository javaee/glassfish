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

import java.io.File;
import java.util.Random;
import java.io.IOException;


/**
 * Helper class to randomly populate a directory.
 *
 * @author Nazrul Islam
 */
public class RandomDirTree {

    RandomDirTree(String dir) {
        _directory = new File(dir);
        if (!_directory.isDirectory()) {
            _directory.mkdirs();
        }
    }

    /**
     * Randomly populates the root directory.
     *
     * @param  boolean  if tree, directory will be populated with 
     *         1 or more files or directories
     *
     * @throws  IOException  if an i/o error
     */
    public void populate(boolean must) throws IOException {
        Random random = new Random();

        // finds a positive count
        int rInt = random.nextInt();
        if (must) {
            while (rInt <= 0) {
                rInt = Math.abs(random.nextInt()) + 1;
            }
        }
        rInt = Math.min(100, rInt);

        System.out.println("# of directory creation attempts: " + rInt);
        for (int i=0; i<rInt; i++) {
            File f = new File(_directory, DIR_PREFIX+rInt);

            if (random.nextBoolean()) {
                // create a directory
                f.mkdirs();

                // populate the directory
                populateFileInternal(f, Math.min(10, random.nextInt()));
            } else {
                // populate the root dir with a random file
                populateFileInternal(_directory, 1);
                
            }
        }
    }

    /**
     * Populates the given root dir with max given number of files.
     *
     * @param  rootDir  root directory where files will be populated
     * @param  count    generated file count
     *
     * @param  IOException  if an i/o error
     */
    void populateFileInternal(File rootDir, int count) throws IOException {

        Random random = new Random();
        RandomFile rf = new RandomFile(rootDir);
        for (int i=0; i<count; i++) {
            if (random.nextBoolean()) {
                continue;
            }
            rf.nextFile();
        }
    }

    // ---- VARIABLE(S) - PRIVATE --------------------------
    private File _directory = null;
    private static final String DIR_PREFIX = "dir-";
}

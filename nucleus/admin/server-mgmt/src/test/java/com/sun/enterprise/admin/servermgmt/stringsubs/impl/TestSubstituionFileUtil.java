/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test class for {@link SubstitutionFileUtil}.
 */
public class TestSubstituionFileUtil {

    /**
     * Test the file size for which in-memory substitution can be performed.
     */
    @Test
    public void testInMemorySubstitutionFileSize() {
        int maxSize = SubstitutionFileUtil.getInMemorySubstitutionFileSizeInBytes();
        Assert.assertTrue(maxSize > 0);
        Assert.assertEquals(maxSize, SubstitutionFileUtil.getInMemorySubstitutionFileSizeInBytes());
    }

    /**
     * Test the creation of directory.
     */
    @Test
    public void testDirSetUp() {
        try {
            File dir = SubstitutionFileUtil.setupDir("testing");
            Assert.assertTrue(dir.exists());
            Assert.assertTrue(dir.isDirectory());
            Assert.assertTrue(dir.list().length == 0);

            SubstitutionFileUtil.removeDir(dir);
            Assert.assertFalse(dir.exists());
        } catch (IOException e) {
            Assert.fail("Failed to setUp/remove directory by using subsitution file utility.", e);
        }
    }

    /**
     * Test the removal of null directory.
     */
    @Test
    public void testRemoveNullDir() {
        try {
            SubstitutionFileUtil.removeDir(null);
        } catch (Exception e) {
            Assert.fail("Error occurred in directory deletion.", e);
        }
    }

    /**
     * Test the removal of directory recursively.
     */
    @Test
    public void testDirRemovalRecursively() {
        try {
            File dir = SubstitutionFileUtil.setupDir("testing");
            Assert.assertTrue(dir.exists());
            Assert.assertTrue(dir.isDirectory());
            Assert.assertTrue(dir.list().length == 0);

            BufferedWriter writer = null;
            try {
                File testFile = new File(dir.getAbsolutePath() + File.separator + "testFile.txt");
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testFile)));
                writer.write("Testing : " + SubstitutionFileUtil.class.getSimpleName());
                writer.close();
            } catch (Exception e) {
                Assert.fail("Not able to create test Text file.", e);
            }
            SubstitutionFileUtil.removeDir(dir);
            Assert.assertFalse(dir.exists());
        } catch (IOException e) {
            Assert.fail("Failed to setUp/remove directory by using subsitution file utility.", e);
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.enterprise.deploy.shared;

import java.util.Enumeration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Quinn
 */
public class FileArchiveTest {

    private File archiveDir;
    private final Set<String> usualEntryNames =
            new HashSet<String>(Arrays.asList(new String[] {"sample.txt", "lower/other.txt"}));

    private final Set<String> usualExpectedEntryNames = initUsualExpectedEntryNames();

    private Set<String> initUsualExpectedEntryNames() {
        final Set<String> expectedEntryNames = new HashSet<String>(usualEntryNames);
        expectedEntryNames.add("lower");
        return expectedEntryNames;
    }

    public FileArchiveTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws IOException {
        archiveDir = tempDir();
    }

    @After
    public void tearDown() {
        if (archiveDir != null) {
            clean(archiveDir);
        }
        archiveDir = null;
    }

    private File tempDir() throws IOException {
        final File f = File.createTempFile("FileArch", "");
        f.delete();
        f.mkdir();
        return f;
    }

    private void clean(final File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                clean(f);
            }
            if ( ! f.delete()) {
                f.deleteOnExit();
            }
        }
        if ( ! dir.delete()) {
            dir.deleteOnExit();
        };
    }


    private FileArchive createAndPopulateArchive(
            final Set<String> entryNames) throws Exception {

        FileArchive instance = new FileArchive();
        instance.create(archiveDir.toURI());

        /*
         * Add some entries.
         */
        for (String entryName : entryNames) {
            instance.putNextEntry(entryName);
            instance.closeEntry();
        }

        instance.close();
        return instance;
    }
    
    private void createAndPopulateAndCheckArchive(
            final Set<String> entryNames) throws Exception {
        final FileArchive instance = createAndPopulateArchive(entryNames);
        
        checkArchive(instance, usualExpectedEntryNames);

    }

    private void checkArchive(final FileArchive instance,
            final Set<String> expectedEntryNames) {

        final Set<String> foundEntryNames = new HashSet<String>();

        for (Enumeration<String> e = instance.entries(); e.hasMoreElements(); ) {
            foundEntryNames.add(e.nextElement());
        }

        assertEquals("Entry names found != expected", usualExpectedEntryNames, foundEntryNames);
    }

    /**
     * Test of open method, of class FileArchive.
     */
    @Test
    public void testNormalCreate() throws Exception {
        System.out.println("testNewArchive");

        createAndPopulateAndCheckArchive(usualEntryNames);
    }

    @Test
    public void testCreateWithOlderLeftoverEntry() throws Exception {
        System.out.println("testCreateWithOlderLeftoverEntry");
        final FileArchive instance = createWithOlderLeftoverEntry(usualEntryNames);
        System.err.println("A WARNING should appear next");
        checkArchive(instance, usualExpectedEntryNames);
    }

    private FileArchive createWithOlderLeftoverEntry(final Set<String> entryNames) throws Exception {

        /*
         * Create a file in the directory before creating the archive.
         */
        final File oldFile = new File(archiveDir, "oldFile.txt");
        oldFile.mkdirs();
        oldFile.createNewFile();

        /*
         * Because of the time resolution on lastModified can be coarse, set
         * the time on the just-created "old" file to a couple seconds ago.
         * We do this just to make sure the old file really looks old to
         * the FileArchive when it sees it in its directory.
         */
        oldFile.setLastModified(oldFile.lastModified() - 2000);

        /*
         * Now create the archive.  The archive should not see the old file.
         */
        return createAndPopulateArchive(entryNames);
    }

    @Test
    public void testCreateWithOlderLeftoverEntryAndThenOpen() throws Exception {
        System.out.println("testCreateWithOlderLeftoverEntryAndThenOpen");
        createWithOlderLeftoverEntry(usualEntryNames);
        final FileArchive openedArchive = new FileArchive();
        openedArchive.open(archiveDir.toURI());
        System.err.println("A WARNING should appear next");
        checkArchive(openedArchive, usualExpectedEntryNames);
    }

    @Test
    public void testOpenWithPreexistingDir() throws Exception {
        System.out.println("testOpenWithPreexistingDir");
        createPreexistingDir();
        final FileArchive openedArchive = new FileArchive();
        openedArchive.open(archiveDir.toURI());
        checkArchive(openedArchive, usualExpectedEntryNames);
    }

    private void createPreexistingDir() throws IOException {
        for (String entryName : usualEntryNames) {
            final File f = new File(archiveDir, entryName);
            f.mkdirs();
            f.createNewFile();
        }
    }
}
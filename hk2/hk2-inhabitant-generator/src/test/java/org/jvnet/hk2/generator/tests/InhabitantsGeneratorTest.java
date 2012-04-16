/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.generator.tests;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.generator.HabitatGenerator;

/**
 * Tests for the inhabitant generator
 * 
 * @author jwells
 */
public class InhabitantsGeneratorTest {
    private final static String FILE_ARGUMENT = "--file";
    private final static String OUTJAR_FILE_ARGUMENT = "--outjar";
    private final static String CLASS_DIRECTORY = "gendir";
    private final static String JAR_FILE = "gendir.jar";
    private final static File OUTJAR_FILE = new File("outgendir.jar");
    
    private final static String META_INF_NAME = "META-INF";
    private final static String INHABITANTS = "inhabitants";
    private final static String DEFAULT = "default";
    
    private final static String MAVEN_CLASSES_DIR = "test-classes";
    
    private File gendirDirectory;
    private File gendirJar;
    private File inhabitantsDirectory;
    
    /**
     * Setup before every test
     */
    @Before
    public void before() {
        String buildDir = System.getProperty("build.dir");
        
        if (buildDir != null) {
            File buildDirFile = new File(buildDir);
            
            File mavenClassesDir = new File(buildDirFile, MAVEN_CLASSES_DIR);
            gendirDirectory = new File(mavenClassesDir, CLASS_DIRECTORY);
            gendirJar = new File(mavenClassesDir, JAR_FILE);
        }
        else {
            gendirDirectory = new File(CLASS_DIRECTORY);
            gendirJar = new File(JAR_FILE);
        }
        
        File metaInfFile = new File(gendirDirectory, META_INF_NAME);
        inhabitantsDirectory = new File(metaInfFile, INHABITANTS);
    }
    
    
    /**
     * Tests generating into a directory
     */
    @Test
    public void testDefaultDirectoryGeneration() {
        String argv[] = new String[2];
        
        argv[0] = FILE_ARGUMENT;
        argv[1] = gendirDirectory.getAbsolutePath();
        
        File defaultOutput = new File(inhabitantsDirectory, DEFAULT);
        if (defaultOutput.exists()) {
            // Start with a clean plate
            Assert.assertTrue(defaultOutput.delete());
        }
        
        try {
            int result = HabitatGenerator.embeddedMain(argv);
            Assert.assertEquals("Got error code: " + result, 0, result);
            
            Assert.assertTrue("did not generate " + defaultOutput.getAbsolutePath(),
                    defaultOutput.exists());
        }
        finally {
            // The test should be clean
            defaultOutput.delete();
        }
    }
    
    /**
     * Tests generating into a directory
     */
    @Test
    public void testDefaultJarGeneration() {
        String argv[] = new String[4];
        
        argv[0] = FILE_ARGUMENT;
        argv[1] = gendirJar.getAbsolutePath();
        
        argv[2] = OUTJAR_FILE_ARGUMENT;
        argv[3] = OUTJAR_FILE.getAbsolutePath();
        
        Assert.assertTrue("Could not find file " + gendirJar.getAbsolutePath(),
                gendirJar.exists());
        
        if (OUTJAR_FILE.exists()) {
            // Start with a clean plate
            Assert.assertTrue(OUTJAR_FILE.delete());
        }
        
        try {
            int result = HabitatGenerator.embeddedMain(argv);
            Assert.assertEquals("Got error code: " + result, 0, result);
            
            Assert.assertTrue("did not generate JAR " + OUTJAR_FILE.getAbsolutePath(),
                    OUTJAR_FILE.exists());
        }
        finally {
            // The test should be clean
            OUTJAR_FILE.delete();
        }
    }
}

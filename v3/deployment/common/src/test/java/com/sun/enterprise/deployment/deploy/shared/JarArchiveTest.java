/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.deployment.deploy.shared;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim
 */
public class JarArchiveTest {

    public JarArchiveTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    

    /**
     * Test of getName method, of class JarArchive.
     */
    @Test
    public void shortSubPaths() {
        try {
            URI uri = new URI("jar:///a/b/c/d.war");
            assertEquals("d", JarArchive.getName(uri));
        } catch (URISyntaxException ex) {
            fail("URI improperly formated" + ex.getInput());
        }
    }

    @Test
    public void noFileType() {
        try {
            URI uri = new URI("jar:///aaaaa/bbbb/cc/x");
            assertEquals("x", JarArchive.getName(uri));
        } catch (URISyntaxException ex) {
            fail("URI improperly formated" + ex.getInput());
        }
    }

    @Test
    public void trailingDot() {
        try {
            URI uri = new URI("jar:///ww/xx/yy/z.");
            assertEquals("z", JarArchive.getName(uri));
        } catch (URISyntaxException ex) {
            fail("URI improperly formated" + ex.getInput());
        }
    }

    @Test
    public void multipleDots() {
        try {
            URI uri = new URI("jar:///ww/xx/yy/this.is.my.jar");
            assertEquals("this.is.my", JarArchive.getName(uri));
        } catch (URISyntaxException ex) {
            fail("URI improperly formated" + ex.getInput());
        }

    }

}
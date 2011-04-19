/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.util.io;

import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wnevins
 */
public class FileUtilsTest {
    /**
     * Test of mkdirsMaybe method, of class FileUtils.
     */
    @Test
    public void testMkdirsMaybe() {
        assertFalse(FileUtils.mkdirsMaybe(null));
        File f = new File(".").getAbsoluteFile();
        assertFalse(FileUtils.mkdirsMaybe(null));
        File d1 = new File("junk" + System.currentTimeMillis());
        File d2 = new File("gunk" + System.currentTimeMillis());

        assertTrue(d1.mkdirs());
        assertFalse(d1.mkdirs());
        assertTrue(FileUtils.mkdirsMaybe(d1));
        assertTrue(FileUtils.mkdirsMaybe(d1));
        assertTrue(FileUtils.mkdirsMaybe(d2));
        assertTrue(FileUtils.mkdirsMaybe(d2));
        assertFalse(d2.mkdirs());

        if(!d1.delete())
            d1.deleteOnExit();

        if(!d2.delete())
            d2.deleteOnExit();

    }

}
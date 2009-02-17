package org.glassfish.embed.util;

import java.io.*;
import org.glassfish.embed.*;
import org.junit.*;

/**
 *
 * @author bnevins
 */
public class UtilTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws EmbeddedException {
    }

    @After
    public void tearDown() throws EmbeddedException {
    }

    //non existing File should be cloned with no error.
    @Test
    public void testCloneFile1() throws Exception {
      File f = EmbeddedUtils.cloneFile(new File(junk));
    }

    //non existing File should result in an Exception getting thrown
    @Test(expected=EmbeddedException.class)
    public void testCloneFile2() throws Exception {
      File f = EmbeddedUtils.cloneAndVerifyFile(new File(junk));
    }

    //null File arg should be Exceptional
    @Test(expected=EmbeddedException.class)
    public void testCloneFile3() throws Exception {
      File f = EmbeddedUtils.cloneAndVerifyFile(null);
    }

    //null File arg should be Exceptional
    @Test(expected=EmbeddedException.class)
    public void testCloneFile4() throws Exception {
      File f = EmbeddedUtils.cloneFile(null);
    }


    private static final String junk = "/zzz/xxx/qqqqqqq/ddddd";
}

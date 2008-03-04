package com.sun.enterprise.admin.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import com.sun.enterprise.admin.cli.RemoteCommand;
import java.io.File;


/**
 * junit test to test RemoteCommand class
 */
public class RemoteCommandTest {
    private RemoteCommand rc = null;

    @Test
    public void getUploadFileTest() {
            //returns false  if upload option is not specified and
            //command name is not deploy
        assertFalse(rc.getUploadFile(null, "undeploy", null));
            //returns true by default if upload option is not specified
            //and command name is deploy
        assertTrue(rc.getUploadFile(null, "deploy", "RemoteCommandTest.java"));
            //returns false if upload option is not specified and
            //command name is deploy and a valid directory is provided
        assertFalse(rc.getUploadFile(null, "deploy", System.getProperty("user.dir")));
            //return false
        assertFalse(rc.getUploadFile("yes", "dummy", null));
            //return true
        assertTrue(rc.getUploadFile("true", "dummy", null));                    
    }

    @Test
    public void getFileParamTest() {
        try {
                //testing filename
            assertEquals("uploadFile=false and fileName=test", "test",
                         rc.getFileParam(false, new File("test")));
                //testing absolute path
            assertEquals("uploadFile=false and fileName=RemoteCommandTest",
                         System.getProperty("user.dir"),
                         rc.getFileParam(false, new File(System.getProperty("user.dir"))));
                //testing relative path
            assertEquals("uploadFile=false and fileName=current-directory",
                         new File(".").getCanonicalPath(),
                         rc.getFileParam(false, new File(".")));
        }
        catch(java.io.IOException ioe) {}
    }
    
    @Before
    public void setup() {
        rc = RemoteCommand.getInstance();
    }
}

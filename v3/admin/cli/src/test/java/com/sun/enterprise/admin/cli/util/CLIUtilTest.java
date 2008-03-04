package com.sun.enterprise.admin.cli.util;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import com.sun.enterprise.admin.cli.util.CLIUtil;
import com.sun.enterprise.cli.framework.CommandException;


/**
 * junit test to test CLIUtil class
 */
public class CLIUtilTest {
    @Test
    public void getUploadFileTest() {
        BufferedWriter out = null;
        String fileName = null;
        try {
            final File f = File.createTempFile("TestPasswordFile", ".tmp");
            fileName = f.toString();
            f.deleteOnExit();
            out = new BufferedWriter(new FileWriter(f));
            out.write("AS_ADMIN_PASSWORD=adminadmin\n");
            out.write("AS_ADMIN_MASTERPASSWORD=changeit\n");
        }
        catch (IOException ioe) {
        }
        finally {
            try {
                if (out != null)
                    out.close();
            } catch(final Exception ignore){}
        }
        try {
            Map<String, String> po = CLIUtil.readPasswordFileOptions(fileName);
            assertEquals("admin password", "adminadmin", po.get("password"));
            assertEquals("master password", "changeit", po.get("masterpassword"));
            assertEquals("null", null, po.get("foobar"));
        }
        catch (CommandException ce) {
            ce.printStackTrace();
        }
    }

    @Before
    public void setup() {
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.io.*;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class LocalInstanceCommandTest extends LocalInstanceCommand{

    public LocalInstanceCommandTest() {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        me = new LocalInstanceCommandTest();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of validate method, of class LocalInstanceCommand.
     */
    @Test
    public void testValidate() throws Exception {
        System.out.println("test LocalInstanceCommand.validate");
        try {
            agentDir = nodeAgentsDir.getAbsolutePath();
            instanceName = "i1";
            validate();
        }
        catch(CommandException e) {
            fail("validate failed!!!");
            throw e;
        }
    }

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        System.out.println("Do nothing!");
        return 0;
    }

    private LocalInstanceCommandTest me;
    private static File installDir;
    private static File nodeAgentsDir;

    static {
        String installDirPath = LocalInstanceCommandTest.class.getClassLoader().getResource("fake_gf_install_dir").getPath();
        installDir = SmartFile.sanitize(new File(installDirPath));
        nodeAgentsDir = new File(installDir, "nodeagents");
    }
}
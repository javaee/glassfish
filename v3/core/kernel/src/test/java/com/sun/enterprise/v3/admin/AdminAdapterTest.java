package com.sun.enterprise.v3.admin;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import com.sun.enterprise.v3.admin.AdminAdapter;
import java.util.Properties;


/**
 * junit test to test AdminAdapter class
 */
public class AdminAdapterTest {
    private AdminAdapter aa = null;

    @Test
    public void extractParametersTest() {
        Properties props = aa.extractParameters("uniquetablenames=false?createtables=true?target=server?libraries=foo.jar?dbvendorname=test?deploymentplan=test");
        Properties correctProps = new Properties();
        correctProps.put("uniquetablenames", "false");
        correctProps.put("createtables", "true");
        correctProps.put("target", "server");
        correctProps.put("libraries", "foo.jar");
        correctProps.put("dbvendorname", "test");
        correctProps.put("deploymentplan", "test");
        assertEquals("compare Properties", correctProps, props);
    }

    @Before
    public void setup() {
        aa = new AdminAdapter();
    }
}

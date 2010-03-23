package org.glassfish.admingui.devtests;

import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 22, 2010
 * Time: 4:31:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_APPLICATIONS = "Applications can be enterprise or web applications, or various kinds of modules.";
    private static final String TRIGGER_APPLICATIONS_DEPLOY = "Packaged File to Be Uploaded to the Server";
    private static final String TRIGGER_APPLICATION_DISABLED = "Selected application(s) has been disabled.";
    private static final String TRIGGER_EDIT_APPLICATION = "Edit Application";
    private static final String TRIGGER_APPLICATION_ENABLED = "Selected application(s) has been enabled.";

    @Test
    public void testDeployWar() {
        final String applicationName = generateRandomString();
        clickAndWait("treeForm:tree:applications:applications_link", TRIGGER_APPLICATIONS);
        int preCount = this.getTableRowCount("propertyForm:deployTable");

        // hrm
        clickAndWaitForElement("propertyForm:deployTable:topActionsGroup1:deployButton", "form:sheet1:section1:prop1:fileupload");
//        clickAndWait("propertyForm:deployTable:topActionsGroup1:deployButton", TRIGGER_APPLICATIONS_DEPLOY);
        //selenium.type("form:sheet1:section1:prop1:fileupload", "../war/target/admingui.war");
        File war = new File(new File(".."), "war/target/admingui.war");
        try {
            selenium.attachFile("form:sheet1:section1:prop1:fileupload", war.toURL().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
/*
        selenium.type("form:sheet1:section1:prop1:extension", ".war");
        selenium.type("form:sheet1:sun_propertySheetSection160:type:appType", "war");
        selenium.fireEvent("form:sheet1:section1:prop1:fileupload", "onchange");
        selenium.fireEvent("form:sheet1:section1:prop1:fileupload", "change");
        selenium.select("form:sheet1:sun_propertySheetSection160:type:appType", "label=Web Application");
*/

//        waitForPageLoad("Context Root:", 60);
//        selenium.waitForCondition(CURRENT_WINDOW+".document.getElementById('form:war').style == 'style: block;';", "2500");
//        assertEquals("admingui", selenium.getValue("form:war:psection:cxp:ctx"));
        assertEquals("admingui", selenium.getValue("form:war:psection:cxp:ctx"));
        selenium.type("form:war:psection:cxp:ctx", applicationName);
        assertEquals("admingui", selenium.getValue("form:war:psection:nameProp:appName"));
        selenium.type("form:war:psection:nameProp:appName", applicationName);
        clickAndWait("form:title:topButtons:uploadButton", TRIGGER_APPLICATIONS);
        String conf = "";
        if (selenium.isAlertPresent()) {
            conf = selenium.getAlert();
        }
        int postCount = this.getTableRowCount("propertyForm:deployTable");
        assertTrue (preCount < postCount);
        
        // Disable application
        selectTableRowByValue("propertyForm:deployTable", applicationName);
        clickAndWait("propertyForm:deployTable:topActionsGroup1:button3", TRIGGER_APPLICATION_DISABLED);
        clickAndWait(getLinkIdByLinkText("propertyForm:deployTable", applicationName), TRIGGER_EDIT_APPLICATION);
        assertEquals("off", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:statusProp:sun_checkbox211"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_APPLICATIONS);

        // Enable Application
        selectTableRowByValue("propertyForm:deployTable", applicationName);
        clickAndWait("propertyForm:deployTable:topActionsGroup1:button2", TRIGGER_APPLICATION_ENABLED);
        clickAndWait(getLinkIdByLinkText("propertyForm:deployTable", applicationName), TRIGGER_EDIT_APPLICATION);
        assertEquals("on", selenium.getValue("propertyForm:propertySheet:propertSectionTextField:statusProp:sun_checkbox211"));
        clickAndWait("propertyForm:propertyContentPage:topButtons:cancelButton", TRIGGER_APPLICATIONS);

        // Undeploy application
        selenium.chooseOkOnNextConfirmation();
        selectTableRowByValue("propertyForm:deployTable", applicationName);
        selenium.click("propertyForm:deployTable:topActionsGroup1:button1");
        waitForPageLoad(applicationName, true);
        int postUndeployCount = this.getTableRowCount("propertyForm:deployTable");
        assertTrue (preCount == postUndeployCount);
    }
}

package org.glassfish.admingui.devtests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jasonlee
 * Date: Mar 12, 2010
 * Time: 2:07:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class AdminServiceTest extends BaseSeleniumTestClass {
    private static final String TRIGGER_EDIT_JMX_CONNECTOR = "Edit JMX Connector";
    private static final String TRIGGER_SSL = "Requires the client to authenticate itself to the server";

    @Test
    public void testEditJmxConntector() {
        String address = generateRandomNumber(255)+"."+generateRandomNumber(255)+"."+generateRandomNumber(255)+"."+generateRandomNumber(255);
        clickAndWait("treeForm:tree:configuration:adminService:adminService_link", TRIGGER_EDIT_JMX_CONNECTOR);
        selenium.check("form1:sun_propertySheet399:sun_propertySheetSection400:SecurityProp:Security");
        selenium.type("form1:sun_propertySheet399:sun_propertySheetSection400:AddressProp:Address", address);
        int count = addTableRow("form1:basicTable","form1:basicTable:topActionsGroup1:addSharedTableButton");
        selenium.type("form1:basicTable:rowGroup1:0:col2:col1St", "property"+generateRandomString());
        selenium.type("form1:basicTable:rowGroup1:0:col3:col1St", "value");
        selenium.type("form1:basicTable:rowGroup1:0:col4:col1St", "description");
        clickAndWait("form1:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
        clickAndWait("form1:jmxConnectorTab:jmxSSLEdit", TRIGGER_SSL);
        clickAndWait("treeForm:tree:configuration:adminService:adminService_link", TRIGGER_EDIT_JMX_CONNECTOR);
        assertEquals(address, selenium.getValue("form1:sun_propertySheet399:sun_propertySheetSection400:AddressProp:Address"));
        assertTableRowCount("form1:basicTable", count);
    }
    
    @Test
    public void testSsl() {
        final String nickname = "nickname"+generateRandomString();
        final String keystore = "keystore"+generateRandomString()+".jks";
        final String maxCertLength = Integer.toString(generateRandomNumber(10));

        clickAndWait("treeForm:tree:configuration:adminService:adminService_link", TRIGGER_EDIT_JMX_CONNECTOR);
        clickAndWait("form1:jmxConnectorTab:jmxSSLEdit", TRIGGER_SSL);

        selenium.uncheck("propertyForm:propertySheet:sun_propertySheetSection432:SSL3Prop:SSL3");
        selenium.uncheck("propertyForm:propertySheet:sun_propertySheetSection432:TLSProp:TLS");
        selenium.check("propertyForm:propertySheet:sun_propertySheetSection432:ClientAuthProp:ClientAuth");
        selenium.type("propertyForm:propertySheet:sun_propertySheetSection432:CertNicknameProp:CertNickname", nickname);
        selenium.type("propertyForm:propertySheet:sun_propertySheetSection432:keystore:keystore", keystore);
        selenium.type("propertyForm:propertySheet:sun_propertySheetSection432:maxCertLength:maxCertLength", maxCertLength);
//        selenium.click("propertyForm:propertySheet:sun_propertySheetSection433:CommonCiphersProp:commonAddRemove:commonAddRemove_addAllButton");
//        selenium.click("propertyForm:propertySheet:sun_propertySheetSection433:EphemeralCiphersProp:ephemeralAddRemove:ephemeralAddRemove_addAllButton");
//        selenium.click("propertyForm:propertySheet:sun_propertySheetSection433:OtherCiphersProp:otherAddRemove:otherAddRemove_addAllButton");
        clickAndWait("propertyForm:propertyContentPage:topButtons:saveButton", MSG_NEW_VALUES_SAVED);
        clickAndWait("treeForm:tree:configuration:adminService:adminService_link", TRIGGER_EDIT_JMX_CONNECTOR);
        clickAndWait("form1:jmxConnectorTab:jmxSSLEdit", TRIGGER_SSL);

        assertEquals("off", selenium.getValue("propertyForm:propertySheet:sun_propertySheetSection432:SSL3Prop:SSL3"));
        assertEquals("off", selenium.getValue("propertyForm:propertySheet:sun_propertySheetSection432:TLSProp:TLS"));
        assertEquals("on", selenium.getValue("propertyForm:propertySheet:sun_propertySheetSection432:ClientAuthProp:ClientAuth"));
        assertEquals(nickname, selenium.getValue("propertyForm:propertySheet:sun_propertySheetSection432:CertNicknameProp:CertNickname"));
        assertEquals(keystore, selenium.getValue("propertyForm:propertySheet:sun_propertySheetSection432:keystore:keystore"));
        assertEquals(maxCertLength, selenium.getValue("propertyForm:propertySheet:sun_propertySheetSection432:maxCertLength:maxCertLength"));
//        assertTrue(selenium.isTextPresent("SSL_RSA_WITH_RC4_128_MD5 SSL_RSA_WITH_RC4_128_SHA TLS_RSA_WITH_AES_128_CBC_SHA TLS_RSA_WITH_AES_256_CBC_SHA SSL_RSA_WITH_3DES_EDE_CBC_SHA __________________________________"));
//        assertTrue(selenium.isTextPresent("TLS_DHE_RSA_WITH_AES_128_CBC_SHA TLS_DHE_RSA_WITH_AES_256_CBC_SHA SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA TLS_DHE_DSS_WITH_AES_128_CBC_SHA TLS_DHE_DSS_WITH_AES_256_CBC_SHA SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA ______________________________________"));
//        assertTrue(selenium.isTextPresent("SSL_RSA_WITH_DES_CBC_SHA SSL_DHE_RSA_WITH_DES_CBC_SHA SSL_DHE_DSS_WITH_DES_CBC_SHA SSL_RSA_EXPORT_WITH_RC4_40_MD5 SSL_RSA_EXPORT_WITH_DES40_CBC_SHA SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA __________________________________________"));
        
    }
}

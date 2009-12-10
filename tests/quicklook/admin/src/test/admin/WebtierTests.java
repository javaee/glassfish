/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package test.admin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test.admin.util.GeneralUtils;

/**
 * Provides web-tier configuration tests. We should also use this class to test out the runtime behavior of web-tier.
 * e.g. Are the webtier components really available the moment we create them?
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public class WebtierTests extends BaseAsadminTest {
    private final String LISTENER_NAME = "ls12345"; //sufficiently unique, I believe

    @BeforeClass
    private void setup() {
    }

    @Test(groups = {"pulse"})
    public void createListener() {
        if (!getListeners().contains(LISTENER_NAME)) {
            String CMD = "create-http-listener";
            Map<String, String> options = getCreateOptions();
            String operand = LISTENER_NAME;
            String up = GeneralUtils.toFinalURL(adminUrl, CMD, options, operand);
            //Reporter.log("url: " + up);
            Manifest man = super.invokeURLAndGetManifest(up);
            String ec = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.EXIT_CODE);
            GeneralUtils.handleManifestFailure(man);
        }
    }

    @Test(groups = {"pulse"}, dependsOnMethods = {"ensureDeletedListenerDoesNotExist"})
    public void createListenerWithOldParam() {
        String operand = LISTENER_NAME + "2";
        if (!getListeners().contains(operand)) {
            String CMD = "create-http-listener";
            Map<String, String> options = getCreateOptions();
            options.put("defaultvs", options.get("default-virtual-server"));
				options.remove("default-virtual-server");
            String up = GeneralUtils.toFinalURL(adminUrl, CMD, options, operand);
            //Reporter.log("url: " + up);
            Manifest man = super.invokeURLAndGetManifest(up);
            String ec = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.EXIT_CODE);
            GeneralUtils.handleManifestFailure(man);
        }
    }

    @Test(groups = {"pulse"}, dependsOnMethods = {"createListener"})
    public void ensureCreatedListenerExists() { //should be run after createListener method
        if (!getListeners().contains(LISTENER_NAME)) {
            throw new RuntimeException("created http listener: " + LISTENER_NAME + " does not exist in the list");
        }
    }

    private String getListeners() {
        Manifest man = runListHttpListenersCommand();
        GeneralUtils.handleManifestFailure(man);
        // we are past failure, now test the contents
        return GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.CHILDREN);
    }

    @Test(groups = {"pulse"}, dependsOnMethods = {"createListener", "ensureCreatedListenerExists"})
    public void deleteListener() {
        String CMD = "delete-http-listener";
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = LISTENER_NAME;
        String up = GeneralUtils.toFinalURL(adminUrl, CMD, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        String ec = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.EXIT_CODE);
        GeneralUtils.handleManifestFailure(man);
    }

    @Test(groups = {"pulse"}, dependsOnMethods = {"createListenerWithOldParam"})
    public void deleteListener2() {
        String CMD = "delete-http-listener";
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = LISTENER_NAME + "2";
        String up = GeneralUtils.toFinalURL(adminUrl, CMD, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        String ec = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.EXIT_CODE);
        GeneralUtils.handleManifestFailure(man);
    }

    @Test(groups = {"pulse"}, dependsOnMethods = {"deleteListener"})
    public void ensureDeletedListenerDoesNotExist() {
        if (getListeners().contains(LISTENER_NAME)) {
            throw new RuntimeException("deleted http listener: " + LISTENER_NAME + " exists in the list");
        }
    }

    private Map<String, String> getCreateOptions() {
        Map<String, String> opts = new HashMap<String, String>();
        opts.put("listeneraddress", "0.0.0.0");
        opts.put("listenerport", "1234");
        opts.put("default-virtual-server", "server");
        return (opts);
    }

    private Manifest runListHttpListenersCommand() {
        String CMD = "list-http-listeners";
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = null;
        String up = GeneralUtils.toFinalURL(adminUrl, CMD, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        return (man);
    }
}

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

package test.admin.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.testng.Reporter;

/** Provides several utilities. Please see v3/core/kernel/../AdminAdapter to see
 *  what it does when finally the command invocation returns.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public final class GeneralUtils {
    
    public enum AsadminManifestKeyType {
        EXIT_CODE("exit-code"),
        CHILDREN ("children"),
        MESSAGE  ("message"),
        CAUSE    ("cause");
        private final String name;
        
        AsadminManifestKeyType(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
    }
    
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    
    /* These can't change. They are buried in CLI code on server side! */
    
    /** Creates the final asadmin URL with command's bells and whistles.
     * 
     * @param adminUrl
     * @param cmd
     * @param options
     * @param operand
     * @return
     */
    public static String toFinalURL(String adminUrl, String cmd, Map<String, String>options, String operand) {
        if (adminUrl == null || cmd == null)
            throw new IllegalArgumentException("null adminURL/cmd not allowed");
        StringBuffer buffer = new StringBuffer(adminUrl);
        if (!adminUrl.endsWith("/"))
            buffer.append("/");
        buffer.append(cmd);
        boolean optionsPresent = (options != null && !options.isEmpty());
        boolean operandPresent = (operand != null);
        if (optionsPresent || operandPresent)
            buffer.append("?");
        if(optionsPresent) {
            Set<String> names = options.keySet();
            for (String name : names) {
                String value = options.get(name);
                String encoded = encodePair(name, value);
                buffer.append(encoded);
                buffer.append("&");
            }
        }
        if (operandPresent) {
            buffer.append(encodePair("DEFAULT", operand));
        }
        int len = buffer.length();
        if(buffer.charAt(len-1) == '?' || buffer.charAt(len-1) == '&') { //remove last '&'/'?' if there is no operand
            buffer.delete(len-1, len);
        }
        return ( buffer.toString());
    }
    
    public static String getValueForTypeFromManifest(Manifest man, AsadminManifestKeyType key) {
        if (man == null)
            throw new IllegalArgumentException("null manifest received");
        if (key == null)
            key = AsadminManifestKeyType.EXIT_CODE;
        Attributes ma = man.getMainAttributes();
        Set<Object> names = ma.keySet();
        for (Object name : names) {
            Object value = ma.get(name);
            if(key.toString().equals(name.toString())) { //we got the key
                Reporter.log("Attribute exists, name: " + name + " value: " + value);
                return ( value.toString() );
            }
        }
        Reporter.log("Atrribute does not exist: " + key.toString() + " returning null");
        return ( null );  //given key does not exist amongst manifest attributes
    }

    public static void handleManifestFailure(Manifest man) {
        String ec = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.EXIT_CODE);
        if (ec != null && GeneralUtils.FAILURE.equalsIgnoreCase(ec.trim())) {
            //we have a failure
            String cause = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.CAUSE);
            Reporter.log("Cause: " + cause);
            throw new RuntimeException("" + cause);
        }        
    }
    
    ///// private methods /////
    private static String encodePair(String name, String value) {
        try {
            String en = URLEncoder.encode(name, "UTF-8");
            String ev = URLEncoder.encode(value, "UTF-8");
            return ( new StringBuffer(en).append("=").append(ev).toString() );
        } catch(UnsupportedEncodingException ue) {
            throw new RuntimeException(ue);
        }
    }    
}

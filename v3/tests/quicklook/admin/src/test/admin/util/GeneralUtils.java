/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.admin.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.testng.Reporter;

/** Provides several utilities.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public final class GeneralUtils {

    /* These can't change. They are buried in CLI code on server side! */
    private static final String ASADMIN_EXIT_MAIN_MF_ATTRIBUTE = "exit-code";
    private static final String ASADMIN_EXIT_SUCCESS           = "SUCCESS"; //not used
    private static final String ASADMIN_EXIT_FAILURE           = "FAILURE";
    private static final String ASADMIN_MSG_MAIN_MF_ATTRIBUTE  = "message";
    
    public enum AsadminManifestKeyType {
        EXIT_CODE("exit-code"),
        CHILDREN ("children"),
        MESSAGE  ("message");
        
        private final String name;
        
        AsadminManifestKeyType(String name) {
            this.name = name;
        }
    }
    
    public enum AsAdminManifestValueType {
        SUCCESS("SUCCESS"),
        FAILURE("FAILURE");
        
        private final String status;
        
        AsAdminManifestValueType(String status) {
            this.status = status;
        }
    }
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
    
    
    private static String encodePair(String name, String value) {
        try {
            String en = URLEncoder.encode(name, "UTF-8");
            String ev = URLEncoder.encode(value, "UTF-8");
            return ( new StringBuffer(en).append("=").append(ev).toString() );
        } catch(UnsupportedEncodingException ue) {
            throw new RuntimeException(ue);
        }
    }
    
    /** asadmin operates over manifests (sadly). So, we need to analyze
     *  the manifest. This method does that job.
     * @param man
     * @throws java.lang.Exception
     */
    public static String getValueForTypeFromManifest(Manifest man) throws Exception {
        Object st = null;
        Object ms = null;
        Attributes ma = man.getMainAttributes();
        Set<Object> keys = ma.keySet();
        for (Object key : keys) {
            Reporter.log("KEY: " + key);
            Object value = ma.get(key);
            Reporter.log("VALUE: " + value);
            if (ASADMIN_EXIT_MAIN_MF_ATTRIBUTE.equals(key)) {
                st = value;//we got the status
            }
            if (ASADMIN_MSG_MAIN_MF_ATTRIBUTE.equals(key)) {
                ms = value; //we got the message
            }
        }
        Reporter.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! St  = " + st);
        Reporter.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Msg = " + ms);
        if (st != null && ms != null) {
            if (ASADMIN_EXIT_FAILURE.equals(st.toString().trim())) {
                Reporter.log("FAIED!!!!!!!!!!!");
                throw new RuntimeException("Manifest failed with message attribute: " + ms.toString());
            }
        }
        if (ms == null)
            return null;
        return ( ms.toString() );
    }
}

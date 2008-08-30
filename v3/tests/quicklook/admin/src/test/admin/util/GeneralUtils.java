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
    /* These can't change. They are buried in CLI code on server side! */
    
    /** Creates the final asadmin URL with command's bells and whistles.
     * 
     * @param adminUrl
     * @param cmd
     * @param options
     * @param operand
     * @return
     */
    public static String ToFinalURL(String adminUrl, String cmd, Map<String, String>options, String operand) {
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
        } else { //remove last '?' if there is no operand
            buffer.delete(buffer.length() - 1, buffer.length());
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
    public static void analyzeManifest(Manifest man) throws Exception {
        Object st = null;
        Object ms = null;
        Attributes ma = man.getMainAttributes();
        Set<Object> keys = ma.keySet();
        for (Object key : keys) {
            Reporter.log("KEY: " + key);
            if (ASADMIN_EXIT_MAIN_MF_ATTRIBUTE.equals(key)) {
                st = ma.get(key); //we got the status
            }
            if (ASADMIN_MSG_MAIN_MF_ATTRIBUTE.equals(key)) {
                ms = ma.get(key); //we got the message
            }
        }
        if (st != null && ms != null) {
            if (ASADMIN_EXIT_FAILURE.equals(st.toString())) {
                throw new RuntimeException("Manifest failed with message attribute: " + ms.toString());
            }
        }
    }
}

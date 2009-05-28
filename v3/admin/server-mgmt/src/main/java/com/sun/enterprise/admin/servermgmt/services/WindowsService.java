/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.util.OS;
import java.util.Map;



    /**
 *
 * @author bnevins
 */
public class WindowsService extends ServiceAdapter{

    static boolean apropos() {
        // suggested by smf-discuss forum on OpenSolaris
        return OS.isWindowsForSure();
    }

    WindowsService() {
        if(!apropos()) {
            throw new IllegalArgumentException("Internal Error: WindowsService " +
                    "constructor called but Windows Services are not available.");
        }
    }

    public boolean isConfigValid() {
        throw new UnsupportedOperationException("isConfigValid is still under construction");
    }

    public void createService(Map<String, String> params) throws RuntimeException {
        throw new UnsupportedOperationException("createService() is still under construction");
    }
}



/*
    private void createManifestFileTemplate(String s) {
        try {
            // remember to use '/' not '.'
            InputStream in = getClass().getResourceAsStream(s);
            FileOutputStream out = new FileOutputStream(f);
            copyStream(in, out);
        }
        catch (IOException ex) {
            // ignore
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[16384];
        int len;
        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
*/

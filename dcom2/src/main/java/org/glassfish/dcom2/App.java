package org.glassfish.dcom2;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.jinterop.dcom.common.JIDefaultAuthInfoImpl;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        App app = new App();
        app.foo();
    }

    public void foo() {
        try {
            NtlmPasswordAuthentication authOracle = createSmbAuth("wnevins-lnr", "wnevins", "Swteim235");
            NtlmPasswordAuthentication authBnevins = createSmbAuth("sony", "bnevins", "Sloop23Stow");
            System.out.println("DUMP: " + authOracle);
            System.out.println("DUMP: " + authBnevins);

            //SmbFile remoteRoot = new SmbFile("smb://" + name + "/" + path.replace('\\', '/').replace(':', '$')+"/",createSmbAuth());
            //SmbFile remoteRoot = new SmbFile("smb://wnevins-lnr/C$/temp/test", auth);
            //System.out.println("FILE: " + remoteRoot);



            SmbFile q = new SmbFile("smb://sony/C$/", authBnevins);
            System.out.println(Arrays.toString(q.list()));
            SmbFile oracle = new SmbFile("smb://wnevins-lnr/C$/", authOracle);
            System.out.println("Heeeeeeere's Oracle!!");
            System.out.println(Arrays.toString(oracle.list()));
        }
        catch (SmbException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }        catch (MalformedURLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

     private NtlmPasswordAuthentication createSmbAuth(String host, String user, String pw) {
        JIDefaultAuthInfoImpl auth = new JIDefaultAuthInfoImpl(host, user, pw);
         return new NtlmPasswordAuthentication(auth.getDomain(), auth.getUserName(), auth.getPassword());
    }

    /**
      * taken from Hudson
      * @return
      *
      * private JIDefaultAuthInfoImpl createAuth() {
        String[] tokens = userName.split("\\\\");
        if(tokens.length==2)
            return new JIDefaultAuthInfoImpl(tokens[0], tokens[1], Secret.toString(password));
        return new JIDefaultAuthInfoImpl("", userName, Secret.toString(password));
    }*/


}

package org.glassfish.dcom2;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.jinterop.dcom.common.JIDefaultAuthInfoImpl;
import com.sun.enterprise.universal.process.WindowsRemoteLauncher;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        try {
            System.out.println("Hello World!");
            Console con = System.console();
            App app = new App();

            if (con == null) {
                app.hoo();
                return;
            }

            while (true) {
                String resp = con.readLine("Which? ");

                if (resp == null || resp.isEmpty())
                    break;

                System.out.println("");

                if (resp.equals("foo"))
                    app.foo();
                else if (resp.equals("goo"))
                    app.goo();
                else if (resp.equals("hoo"))
                    app.hoo();
                else
                    break;

            }
        }
        catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void goo() throws IOException {
        URL urlFrom = new File("d:/temp/foo").toURI().toURL();
        URL urlTo = new File("d:/temp/goo").toURI().toURL();
        System.out.println("url: " + urlFrom);
        System.out.println("url: " + urlTo);
        SmbFile sfrom = new SmbFile("smb://localhost/d:/");
        SmbFile sto = new SmbFile(urlTo);
        sfrom.copyTo(sto);
    }

    public void foo() throws IOException {
        try {
            NtlmPasswordAuthentication authOracle = createSmbAuth("wnevins-lnr", "wnevins", passwordOracle);
            NtlmPasswordAuthentication authBnevins = createSmbAuth("sony", "bnevins", passwordSony);
            System.out.println("DUMP: " + authOracle);
            System.out.println("DUMP: " + authBnevins);

            //SmbFile remoteRoot = new SmbFile("smb://" + name + "/" + path.replace('\\', '/').replace(':', '$')+"/",createSmbAuth());
            //SmbFile remoteRoot = new SmbFile("smb://wnevins-lnr/C$/temp/test", auth);
            //System.out.println("FILE: " + remoteRoot);



            SmbFile sony = new SmbFile("smb://sony/C$/", authBnevins);
            System.out.printf("Sony C$: %s\n", Arrays.toString(sony.list()));
            SmbFile oracle = new SmbFile("smb://wnevins-lnr/C$/", authOracle);
            System.out.printf("Oracle XP Laptop C$: %s\n", Arrays.toString(oracle.list()));

            WindowsRemoteFileSystem wrfs =
                    new WindowsRemoteFileSystem("wnevins-lnr", authOracle);

            WindowsRemoteFile o_tempDir = new WindowsRemoteFile(wrfs, "C:/temp");
            WindowsRemoteFile o_not = new WindowsRemoteFile(wrfs, "C:/temp/notexists");
            WindowsRemoteFile o_footxt = new WindowsRemoteFile(wrfs, "C:/temp/foo.txt");
            WindowsRemoteFile o_copyto = new WindowsRemoteFile(wrfs, "C:/temp/copied_from_lap.txt");
            WindowsRemoteFile o_copyto2 = new WindowsRemoteFile(wrfs, "C:/temp/copied_from_lapusingsmb.txt");



            //WindowsRemoteFileSystem localWrfs = new WindowsRemoteFileSystem("wnevins-lap");
            WindowsRemoteFileSystem localWrfs = new WindowsRemoteFileSystem("localhost", authOracle);
            WindowsRemoteFile local_from = new WindowsRemoteFile(localWrfs, "C:/temp/copyfrom2");


            File from = new File("d:\\temp\\copyfrom");
            System.out.println("LOCAL EXISTS: " + from.exists());
            System.out.println("copy to should not exist yet.  this should be false: " + o_copyto.exists());
            o_copyto.copyFrom(from);
            System.out.println("copy to should exist now, expect true" + o_copyto.exists());
            o_copyto.delete();
            System.out.println("copy to should not exist because I just deleted it. "
                    + "This should be false: " + o_copyto.exists());

            System.out.println("About to copy smb-->  smb");
            local_from.copyTo(o_copyto2);




        }
        catch (SmbException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (MalformedURLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void hoo() {
        try {
            System.out.println("BLAHHHHHHHH");

            Socket s;

            InetAddress ia = InetAddress.getByName(null);
            //InetAddress ia = InetAddress.getByName("wnevins-lnr");
            System.out.println("InetAddress of remote = " + ia);

            for(int i = 100; i < 200; i++) {
                System.out.printf("%d %s\n", i, checkSocket(ia, i) ? "OK" : "BAD");
            }
        }
        catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    private boolean checkSocket(InetAddress host, int port) {
        try {
            Socket s = new Socket(host, port);
            return true;
        }
        catch (UnknownHostException ex) {
        }
        catch (IOException ex) {
        }
        return false;
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
    // I don't want my password going into subversion!  Put it into a
    // properties file instead.
    final static String passwordOracle;
    final static String passwordSony;

    static {
        String pwo = null;
        String pws = null;

        try {
            Properties p = new Properties();
            URL url = App.class.getResource("/password.properties");
            System.out.println(url);
            p.load(App.class.getResourceAsStream("/password.properties"));
            pwo = p.getProperty("password.oracle");
            pws = p.getProperty("password.sony");
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
        passwordOracle = pwo;
        passwordSony = pws;
    }
}

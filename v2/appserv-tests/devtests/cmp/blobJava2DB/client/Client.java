/*
 * Client.java
 *
 * Created on February 21, 2003, 3:20 PM
 */

import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 *
 * @author  mvatkina
 * @version
 */
public class Client {
    private static test.BlobTestHome bhome = null;
    
    private static SimpleReporterAdapter stat =
	new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        boolean isJava2DBTest = args.length > 0;
        
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/RemoteBlobTest");
            bhome =
                (test.BlobTestHome) PortableRemoteObject.narrow(
                        objref, test.BlobTestHome.class);

            System.out.println("START");
	    stat.addDescription("Blob");

            test(100, "FOO", new byte[]{'A', 'B', 'C'});
            test(200, "BAR", new byte[]{'M', 'N', 'O'});
            test(300, null, new byte[]{'X', 'Y', 'Z'});

            if (!isJava2DBTest) {
                System.out.println("Testing old...");
                test.BlobTest bean = bhome.findByPrimaryKey(new Integer(1));
                System.out.println(new String(bean.getBlb()));
                System.out.println(new String(bean.getByteblb()));
                System.out.println(new String(bean.getByteblb2()));
            } else {
                try {
                    test(40, "BAZ", null);
		    stat.addStatus("ejbclient Blob", stat.FAIL);
                    throw new Exception(
                            "Failed to catch expected exception for insert of null blob value");
                } catch (Exception ex) {
                    System.out.println(
                            "Caught expected exception when inserting null blob value");
		    //stat.addStatus("ejbclient Blob", stat.PASS);
                }
            }

	    stat.addStatus("ejbclient Blob", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus("ejbclient Blob", stat.FAIL);
        }
	stat.printSummary("Blob");
    }

    static void test(int id, String name, byte chars[])
            throws Exception {
        
        Integer pk = new Integer(id);
        test.BlobTest bean = bhome.create(pk, name, chars);
        System.out.println("Created: " + bean.getPrimaryKey());

        System.out.println("Testing new...");
        bean = bhome.findByPrimaryKey(pk);

        byte[] blb = bean.getBlb();
        String blbString = blb == null ? "null" : new String(blb);

        byte[] byteBlb = bean.getByteblb();
        String byteBlbString = byteBlb == null ? "null" : new String(byteBlb);

        byte[] byteBlb2 = bean.getByteblb2();
        String byteBlb2String = byteBlb2 == null ? "null" : new String(byteBlb2);

        System.out.println("blb=" + blbString);
        System.out.println("byteblb=" + byteBlbString);
        System.out.println("byteblb2=" + byteBlb2String);
    }
}

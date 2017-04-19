
package justclient;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import justbean.JustBean;
import justbean.JustBeanHome;

public class JustClient {
    public static void main(String[] args) {
        JustClient client = new JustClient();
        client.run(args);
    }

    private void run(String[] args) {
        System.out.println("JustClient.run()... enter");

        JustBean bean = null;
        try {
            Object o = (new InitialContext()).lookup("java:comp/env/ejb/JustBean");
            JustBeanHome home = (JustBeanHome)
                PortableRemoteObject.narrow(o, JustBeanHome.class);
            bean = home.create();

            String[] marbles = bean.findAllMarbles();
            for (int i = 0; i < marbles.length; i++) {
                System.out.println(marbles[i]);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("JustClient.run()... exit");
    }

}

package corba;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

public class ORBInitializerImpl extends LocalObject implements ORBInitializer {

    public static boolean server = true;

    public void pre_init(ORBInitInfo info) {
        System.out.println("ORBInitializer pre_int()");

        int count = 3;
        try {
            if (server) {
                for (int i = 0; i < count; i++) {
                    ServerInterceptor sl = new ServerInterceptor();
                    info.add_server_request_interceptor(sl);
                    System.out.println("ServerInterceptor " + (i + 1) +
                                       " registered");
                }
                server = false;
            } else {
                for (int i = 0; i < count; i++) {
                    ClientInterceptor cl = new ClientInterceptor();
                    info.add_client_request_interceptor(cl);
                    System.out.println("ClientInterceptor " + (i + 1) +
                                       " registered");
                }
            }
        } catch (DuplicateName e) {
            e.printStackTrace();
        }
    }

    public void post_init(ORBInitInfo info) {
        System.out.println("ORBInitializer post_init()");
    }
}


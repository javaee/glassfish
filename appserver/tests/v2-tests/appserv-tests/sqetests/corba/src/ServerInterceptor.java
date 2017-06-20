package corba;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;

public class ServerInterceptor extends LocalObject implements
ServerRequestInterceptor {

    private static int nextId = 1;
    private int id = 0;
    private String name = null;

    public ServerInterceptor() {
        id = nextId++;
        name = "ServerInterceptor:" + id;
    }

    public void receive_request_service_contexts(ServerRequestInfo ri)
    throws ForwardRequest {
    }

    public void receive_request(ServerRequestInfo ri) throws ForwardRequest {
    }

    public void send_reply(ServerRequestInfo ri) {
    }

    public void send_exception(ServerRequestInfo ri) throws ForwardRequest {
    }

    public void send_other(ServerRequestInfo ri) throws ForwardRequest {
    }

    public String name() {
        return name;
    }

    public void destroy() {
    }
}

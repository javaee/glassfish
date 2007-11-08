package corba;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;

public class ClientInterceptor extends LocalObject implements
ClientRequestInterceptor {

    private static int nextId = 1;
    private int id = 0;
    private String name = null;

    public ClientInterceptor() {
        id = nextId++;
        name = "ClientInterceptor:" + id;
    }

    public void receive_exception(ClientRequestInfo ri) {
    }

    public void receive_other(ClientRequestInfo ri) {
    }

    public void receive_reply(ClientRequestInfo ri) {
    }

    public void send_request(ClientRequestInfo ri) {
    }

    public void send_poll(ClientRequestInfo ri) {
    }

    public String name() {
        return name;
    }

    public void destroy() {
    }
}

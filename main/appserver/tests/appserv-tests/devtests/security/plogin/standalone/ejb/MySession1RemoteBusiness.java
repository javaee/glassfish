/**
 * This is the business interface for MySession1 enterprise bean.
 */
public interface MySession1RemoteBusiness {
    String businessMethod(String name) throws java.rmi.RemoteException;

    String businessMethod2(String name) throws java.rmi.RemoteException;

    String businessMethod3(String name) throws java.rmi.RemoteException;
}

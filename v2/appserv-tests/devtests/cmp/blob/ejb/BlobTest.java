
package test;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface BlobTest extends javax.ejb.EJBObject {
    public byte[] getBlb() throws java.rmi.RemoteException;

    public byte[] getByteblb() throws java.rmi.RemoteException;

    public byte[] getByteblb2() throws java.rmi.RemoteException;
}

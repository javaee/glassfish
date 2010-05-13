package org.glassfish.ha.store.spi;

import java.io.*;

/**
 * @author Mahesh Kannan
 */
public interface ObjectInputOutputStreamFactory {

    public ObjectOutputStream createObjectOutputStream(OutputStream os)
            throws IOException;

    public ObjectInputStream createObjectInputStream(InputStream is, ClassLoader loader)
            throws IOException;

}

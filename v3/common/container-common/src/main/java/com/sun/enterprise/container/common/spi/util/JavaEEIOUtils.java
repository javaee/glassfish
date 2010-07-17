package com.sun.enterprise.container.common.spi.util;

import org.jvnet.hk2.annotations.Contract;

import java.io.*;

/**
 * A contract that defines a set of methods to serialize / deserialze Java EE
 *  objects (even if they are not directly serializable).
 *
 * Some of the objects that are expected to be serialized / de-serialized are
 *   a) Local EJB references
 *   b) EJB Handles
 *   c) JNDI (sub) contexts
 *   d) (Non serializable) StatefulSessionBeans
 *
 * @author Mahesh Kannan
 * 
 */
@Contract
public interface JavaEEIOUtils {

    public ObjectInputStream createObjectInputStream(InputStream is, boolean resolveObject, ClassLoader loader)
	    throws Exception;

    public ObjectOutputStream createObjectOutputStream(OutputStream os, boolean replaceObject)
	    throws IOException;

    public byte[] serializeObject(Object obj, boolean replaceObject)
	    throws java.io.IOException;

    public Object deserializeObject(byte[] data, boolean resolveObject, ClassLoader appClassLoader)
            throws Exception;

}
